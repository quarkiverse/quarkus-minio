package io.quarkiverse.minio.client.hosturlmap;

import java.util.regex.Pattern;

import io.smallrye.config.RelocateConfigSourceInterceptor;

public class MinioUrlRelocateInterceptor extends RelocateConfigSourceInterceptor {
    public MinioUrlRelocateInterceptor() {
        super(MinioUrlRelocateInterceptor::keyEvaluator);
    }

    private static Pattern pattern = Pattern.compile("quarkus\\.minio(\\..*)?\\.url");

    private static String keyEvaluator(String key) {
        var matcher = pattern.matcher(key);
        if (matcher.matches()) {
            return "quarkus.minio%s.host".formatted(matcher.group(1) != null ? matcher.group(1) : "");
        }
        return key;
    }
}
