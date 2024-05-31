package io.quarkiverse.minio.client.deployment.devservices;

import java.util.Map;
import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigDocMapKey;
import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;

@ConfigGroup
public class MinioDevServicesBuildTimeConfig {

    /**
     * Enable or disable Dev Services explicitly. Dev Services are automatically enabled unless {@code quarkus.minio.url} is
     * set.
     */
    @ConfigItem
    public Optional<Boolean> enabled = Optional.empty();

    /**
     * Optional fixed port the dev service will listen to.
     * <p>
     * If not defined, the port will be chosen randomly.
     */
    @ConfigItem
    public Optional<Integer> port;

    /**
     * The Minio container image to use.
     */
    @ConfigItem(defaultValue = "minio/minio:RELEASE.2022-10-08T20-11-00Z")
    public String imageName;

    /**
     * Indicates if the Minio server managed by Quarkus Dev Services is shared.
     * When shared, Quarkus looks for running containers using label-based service discovery.
     * If a matching container is found, it is used, and so a second one is not started.
     * Otherwise, Dev Services for Minio starts a new container.
     * <p>
     * The discovery uses the {@code quarkus-dev-service-minio} label.
     * The value is configured using the {@code service-name} property.
     * <p>
     * Container sharing is only used in dev mode.
     */
    @ConfigItem(defaultValue = "true")
    public boolean shared;

    /**
     * The value of the {@code quarkus-dev-service-minio} label attached to the started container.
     * This property is used when {@code shared} is set to {@code true}.
     * In this case, before starting a container, Dev Services for Minio looks for a container with the
     * {@code quarkus-dev-service-minio} label
     * set to the configured value. If found, it will use this container instead of starting a new one. Otherwise, it
     * starts a new container with the {@code quarkus-dev-service-minio} label set to the specified value.
     * <p>
     * This property is used when you need multiple shared Minio servers.
     */
    @ConfigItem(defaultValue = "minio")
    public String serviceName;

    /**
     * Minio root username access key.
     */
    @ConfigItem(defaultValue = "minioaccess")
    public String accessKey;

    /**
     * Minio root username secret key.
     */
    @ConfigItem(defaultValue = "miniosecret")
    public String secretKey;

    /**
     * Extra environment variables that will be passed to the devservice.
     */
    @ConfigItem
    @ConfigDocMapKey("environment-variable-name")
    public Map<String, String> containerEnv;
}
