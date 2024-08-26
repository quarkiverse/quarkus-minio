package io.quarkiverse.minio.client;

import java.util.Optional;

import org.eclipse.microprofile.config.spi.Converter;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.Converters;
import io.smallrye.config.WithConverter;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.minio")
@ConfigGroup
public interface MinioRuntimeConfiguration {

    /**
     * The minio server URL.
     * The url _may_ contains the port, though it's not recommended. If a specific port is needed, `quakus.minio.port` is a
     * better fit.
     * <p>
     * [NOTE]
     * ====
     * Value must start with `http://` or `https://`
     * ====
     *
     * @asciidoclet
     */
    @WithConverter(EmptyConverter.class)
    String url();

    /**
     * The minio server access key
     *
     * @asciidoclet
     */
    @WithConverter(EmptyConverter.class)
    String accessKey();

    /**
     * The minio server secret key
     *
     * @asciidoclet
     */
    @WithConverter(EmptyConverter.class)
    String secretKey();

    /**
     * An optional bucket region
     *
     * @asciidoclet
     */
    Optional<String> region();

    /**
     * An optional port number.
     * Specifically usefull if you want to access bucket that doesn't use standard port (i.e. *80* for HTTP and *443* for HTTPS)
     *
     * @asciidoclet
     */
    Optional<Integer> port();

    /**
     * An optional boolean to enable secure connection.
     * Defaults to `true`
     *
     * @asciidoclet
     */
    @WithDefault("true")
    boolean secure();

    class EmptyConverter implements org.eclipse.microprofile.config.spi.Converter<String> {

        Converter<String> delegate = Converters.newEmptyValueConverter(value -> value, "");

        @Override
        public String convert(String s) throws IllegalArgumentException, NullPointerException {
            return delegate.convert(s);
        }
    }
}
