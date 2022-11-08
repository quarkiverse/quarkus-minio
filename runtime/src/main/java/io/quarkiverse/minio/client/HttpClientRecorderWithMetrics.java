package io.quarkiverse.minio.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.okhttp3.OkHttpMetricsEventListener;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;

public class HttpClientRecorderWithMetrics {

    protected static final long DEFAULT_CONNECTION_TIMEOUT = 5;

    private Instance<MeterRegistry> meterRegistry;

    private MinioConfiguration configuration;

    public HttpClientRecorderWithMetrics(Instance<MeterRegistry> meterRegistry, MinioConfiguration configuration) {
        this.meterRegistry = meterRegistry;
        this.configuration = configuration;
    }

    @Produces
    public Optional<OkHttpClient> httpClient() {
        if (!configuration.produceMetrics()) {
            return Optional.empty();
        }
        return Optional.of(getHttpClientWithInterceptor(meterRegistry.get()));
    }

    private OkHttpClient getHttpClientWithInterceptor(MeterRegistry meterRegistry) {
        //Copy from io.minio.MinioClient#2860
        var httpClient = new OkHttpClient()
                .newBuilder()
                .connectTimeout(DEFAULT_CONNECTION_TIMEOUT, TimeUnit.MINUTES)
                .writeTimeout(DEFAULT_CONNECTION_TIMEOUT, TimeUnit.MINUTES)
                .readTimeout(DEFAULT_CONNECTION_TIMEOUT, TimeUnit.MINUTES)
                .protocols(List.of(Protocol.HTTP_1_1))
                .eventListener(
                        OkHttpMetricsEventListener.builder(meterRegistry, "minio.client")
                                .uriMapper(request -> String.join("/", request.url().pathSegments()))
                                .build())
                .build();
        String filename = System.getenv("SSL_CERT_FILE");
        if (filename != null && !filename.isEmpty()) {
            try {
                httpClient = enableExternalCertificates(httpClient, filename);
            } catch (GeneralSecurityException | IOException e) {
                throw new RuntimeException(e);
            }
        }
        return httpClient;
    }

    /**
     * //Copy from io.minio.MinioClient#enableExternalCertificates
     * copied logic from
     * https://github.com/square/okhttp/blob/master/samples/guide/src/main/java/okhttp3/recipes/CustomTrust.java
     */
    private OkHttpClient enableExternalCertificates(OkHttpClient httpClient, String filename)
            throws GeneralSecurityException, IOException {
        Collection<? extends Certificate> certificates = null;
        try (FileInputStream fis = new FileInputStream(filename)) {
            certificates = CertificateFactory.getInstance("X.509").generateCertificates(fis);
        }

        if (certificates == null || certificates.isEmpty()) {
            throw new IllegalArgumentException("expected non-empty set of trusted certificates");
        }

        char[] password = "password".toCharArray(); // Any password will work.

        // Put the certificates a key store.
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        // By convention, 'null' creates an empty key store.
        keyStore.load(null, password);

        int index = 0;
        for (Certificate certificate : certificates) {
            String certificateAlias = Integer.toString(index++);
            keyStore.setCertificateEntry(certificateAlias, certificate);
        }

        // Use it to build an X509 trust manager.
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);

        final KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();
        final TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagers, trustManagers, null);
        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

        return httpClient
                .newBuilder()
                .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustManagers[0])
                .build();
    }
}
