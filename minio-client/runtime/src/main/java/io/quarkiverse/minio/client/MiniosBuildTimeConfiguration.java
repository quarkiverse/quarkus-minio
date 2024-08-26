package io.quarkiverse.minio.client;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithParentName;

@ConfigMapping(prefix = "quarkus.minio")
@ConfigRoot(phase = ConfigPhase.BUILD_AND_RUN_TIME_FIXED)
public interface MiniosBuildTimeConfiguration {

    String DEFAULT_MINIOCLIENT_NAME = "<default>";

    /**
     * The default client
     */
    @WithParentName()
    MinioBuildTimeConfiguration minio();

    /**
     * Additional named client
     */
    @WithParentName()
    Map<String, MinioBuildTimeConfiguration> namedMinioClients();

    default Map<String, MinioBuildTimeConfiguration> getMinioClients() {
        var minioClients = new HashMap<String, MinioBuildTimeConfiguration>();
        if (minio() != null && minio().enabled()) {
            minioClients.put(DEFAULT_MINIOCLIENT_NAME, minio());
        }
        minioClients.putAll(namedMinioClients().entrySet().stream()
                .filter(entry -> entry.getValue().enabled())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        return minioClients;
    }

    static boolean isDefault(String minioClientName) {
        return MiniosBuildTimeConfiguration.DEFAULT_MINIOCLIENT_NAME.equals(minioClientName);
    }
}
