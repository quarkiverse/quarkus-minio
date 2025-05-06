package io.quarkiverse.minio.client.deployment.devservices;

import java.util.Map;

import io.quarkus.runtime.annotations.ConfigDocMapKey;
import io.quarkus.runtime.annotations.ConfigGroup;
import io.smallrye.config.WithDefault;

@ConfigGroup
public interface MinioDevServicesBuildTimeConfig {

    /**
     * Enable or disable Dev Services explicitly. Dev Services are automatically enabled unless {@code quarkus.minio.url} is
     * set.
     */
    @WithDefault("true")
    Boolean enabled();

    /**
     * Optional fixed port the dev service will listen to.
     * <p>
     * If not defined, the port will be chosen randomly.
     */
    @WithDefault("0")
    Integer port();

    /**
     * The Minio container image to use.
     */
    @WithDefault("minio/minio:RELEASE.2025-04-22T22-12-26Z")
    String imageName();

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
    @WithDefault("true")
    boolean shared();

    /**
     * Whether to keep Dev Service containers running after a dev mode session or test
     * suite execution to reuse them in the next dev mode session.
     * <p>
     * Enabled by default
     */
    @WithDefault("true")
    boolean reuseEnabled();

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
    @WithDefault("minio")
    String serviceName();

    /**
     * Minio root username access key.
     */
    @WithDefault("minioaccess")
    String accessKey();

    /**
     * Minio root username secret key.
     */
    @WithDefault("miniosecret")
    String secretKey();

    /**
     * Extra environment variables that will be passed to the devservice.
     */
    @ConfigDocMapKey("environment-variable-name")
    Map<String, String> containerEnv();
}
