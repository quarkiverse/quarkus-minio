package io.quarkiverse.minio.client.devui;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import io.smallrye.common.annotation.NonBlocking;
import io.vertx.core.json.JsonObject;

public class MinioJsonRPCService {

    Config config = ConfigProvider.getConfig();

    @NonBlocking
    public JsonObject getLoginDetails() {
        return JsonObject.of(
                "accesskey", config.getOptionalValue("quarkus.minio.access-key", String.class).orElse(null),
                "secretkey", config.getOptionalValue("quarkus.minio.secret-key", String.class).orElse(null));
    }

    @NonBlocking
    public String getConsoleUrl() {
        return config.getOptionalValue("quarkus.minio.console", String.class).orElse(null);
    }
}
