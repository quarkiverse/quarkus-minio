package io.quarkiverse.minio.client;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "minio", phase = ConfigPhase.BUILD_AND_RUN_TIME_FIXED)
public class MiniosBuildTimeConfiguration {

    public static final String DEFAULT_MINIOCLIENT_NAME = "<default>";

    /**
     * The default client
     */
    @ConfigItem(name = ConfigItem.PARENT)
    public MinioBuildTimeConfiguration minio;

    /**
     * Additional named client
     */
    @ConfigItem(name = ConfigItem.PARENT)
    public Map<String, MinioBuildTimeConfiguration> namedMinioClients;

    public Map<String, MinioBuildTimeConfiguration> getMinioClients() {
        var minioClients = new HashMap<String, MinioBuildTimeConfiguration>();
        if (minio == null) {
            minio = new MinioBuildTimeConfiguration();
        }
        if (minio.enabled) {
            minioClients.put(DEFAULT_MINIOCLIENT_NAME, minio);
        }
        minioClients.putAll(namedMinioClients.entrySet().stream()
                .filter(entry -> entry.getValue().enabled)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        return minioClients;
    }

    public static boolean isDefault(String minioClientName) {
        return minioClientName.equals(MiniosBuildTimeConfiguration.DEFAULT_MINIOCLIENT_NAME);
    }
}
