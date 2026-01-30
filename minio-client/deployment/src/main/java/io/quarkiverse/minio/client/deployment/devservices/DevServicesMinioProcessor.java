package io.quarkiverse.minio.client.deployment.devservices;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.jboss.logging.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.utility.DockerImageName;

import io.quarkiverse.minio.client.MiniosBuildTimeConfiguration;
import io.quarkiverse.minio.client.deployment.MinioClientProcessor;
import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.DevServicesSharedNetworkBuildItem;
import io.quarkus.deployment.builditem.DockerStatusBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.builditem.Startable;
import io.quarkus.deployment.dev.devservices.DevServicesConfig;
import io.quarkus.devservices.common.ConfigureUtil;
import io.quarkus.devservices.common.ContainerAddress;
import io.quarkus.devservices.common.ContainerLocator;
import io.quarkus.runtime.configuration.ConfigUtils;

public class DevServicesMinioProcessor {
    private static final Logger LOGGER = Logger.getLogger(DevServicesMinioProcessor.class);
    public static final String MINIO_CONSOLE = "quarkus.minio.console";
    private static final String MINIO_HOST = "quarkus.minio%s.host";
    private static final String MINIO_PORT = "quarkus.minio%s.port";
    private static final String MINIO_SECURE = "quarkus.minio%s.secure";
    private static final String MINIO_ALLOW_EMPTY = "quarkus.minio%s.allow-empty";
    private static final String MINIO_ACCESS_KEY = "quarkus.minio%s.access-key";
    private static final String MINIO_SECRET_KEY = "quarkus.minio%s.secret-key";

    /**
     * Label to add to shared Dev Service for Minio running in containers.
     * This allows other applications to discover the running service and use it instead of starting a new instance.
     */
    static final String DEV_SERVICE_LABEL = "quarkus-dev-service-minio";
    static final int DEVSERVICE_MINIO_PORT = 9000;
    static final int DEVSERVICE_MINIO_CONSOLE_PORT = 9001;

    private static final ContainerLocator minioContainerLocator = new ContainerLocator(DEV_SERVICE_LABEL,
            DEVSERVICE_MINIO_PORT);

    @BuildStep(onlyIfNot = IsNormal.class, onlyIf = DevServicesConfig.Enabled.class)
    public DevServicesResultBuildItem startMinioDevService(
            DockerStatusBuildItem dockerStatusBuildItem,
            MinioBuildTimeConfig minioBuildTimeConfig,
            LaunchModeBuildItem launchMode,
            DevServicesConfig devServicesConfig,
            MiniosBuildTimeConfiguration buildTimeConfiguration,
            List<DevServicesSharedNetworkBuildItem> devServicesSharedNetworkBuildItem) {

        // If the dev service is disabled, we return null to indicate that no dev service was started.
        MinioDevServiceCfg config = getConfiguration(minioBuildTimeConfig);
        if (devServiceDisabled(dockerStatusBuildItem, minioBuildTimeConfig.devservices())) {
            return null;
        }

        final Optional<ContainerAddress> maybeContainerAddress = minioContainerLocator.locateContainer(config.serviceName,
                config.shared,
                launchMode.getLaunchMode());
        final Optional<Integer> maybeConsolePort = minioContainerLocator.locatePublicPort(config.serviceName,
                config.shared,
                launchMode.getLaunchMode(), 9001);

        return maybeContainerAddress.map(containerAddress -> DevServicesResultBuildItem.discovered()
                //.feature(MinioClientProcessor.FEATURE) FIXME when released
                .name(MinioClientProcessor.FEATURE)
                .containerId(containerAddress.getId())
                .config(getRunningDevServicesConfig(maybeConsolePort, config,
                        containerAddress.getHost(), containerAddress.getPort(), buildTimeConfiguration))
                .build())
                .orElseGet(() -> DevServicesResultBuildItem.owned()
                        .feature(MinioClientProcessor.FEATURE)
                        .serviceName(config.serviceName)
                        .serviceConfig(config)
                        .startable(() -> startMinio(config, devServicesConfig.timeout(),
                                !devServicesSharedNetworkBuildItem.isEmpty()))
                        .postStartHook(
                                minioContainer -> logStarted(minioContainer, config))
                        .configProvider(getRunningDevServicesConfig(config, buildTimeConfiguration))
                        .build());

    }

    private static void logStarted(MinioContainer container, MinioDevServiceCfg config) {
        LOGGER.infof("Dev Services for Minio started on %s", container.getEffectiveHost());

        LOGGER.infof("Console for Minio is available on %s", container.getConsolePort());

        LOGGER.infof("Other Quarkus applications in dev mode will find the "
                + "instance automatically. For Quarkus applications in production mode, you can connect to"
                + " this by starting your application with -D%s=%s -D%s=%s -D%s=%s",
                formatPropertyName(MINIO_HOST), container.getEffectiveHost(),
                formatPropertyName(MINIO_SECURE), false,
                formatPropertyName(MINIO_PORT), DEVSERVICE_MINIO_PORT,
                formatPropertyName(MINIO_ACCESS_KEY), config.accessKey,
                formatPropertyName(MINIO_SECRET_KEY), config.secretKey);
    }

    private boolean devServiceDisabled(DockerStatusBuildItem dockerStatusBuildItem,
            MinioDevServicesBuildTimeConfig config) {
        if (!config.enabled()) {
            // explicitly disabled
            LOGGER.debug("Not starting dev services for Minio, as it has been disabled in the config.");
            return true;
        }

        // Check if quarkus.minio.host is set
        if (ConfigUtils.isPropertyPresent(formatPropertyName(MINIO_HOST))) {
            LOGGER.debug("Not starting dev services for Minio, the quarkus.minio.host is configured.");
            return true;
        }

        // Check if quarkus.minio.allow-empty is set to true
        Optional<Boolean> allowEmpty = ConfigUtils.getFirstOptionalValue(List.of(formatPropertyName(MINIO_ALLOW_EMPTY)),
                Boolean.class);
        if (allowEmpty.isPresent() && allowEmpty.get()) {
            LOGGER.debug("Not starting dev services for Minio, the quarkus.minio.allow-empty is set to true.");
            return true;
        }

        if (!dockerStatusBuildItem.isContainerRuntimeAvailable()) {
            LOGGER.warn(String.format("Docker isn't working, please configure the Minio Url property (%s).",
                    formatPropertyName(MINIO_HOST)));
            return true;
        }
        return false;
    }

    private MinioContainer startMinio(
            MinioDevServiceCfg config,
            Optional<Duration> timeout,
            Boolean useSharedNetwork) {

        MinioContainer container = new MinioContainer(
                DockerImageName.parse(config.imageName),
                config.fixedExposedPort,
                config.accessKey,
                config.secretKey,
                config.containerEnv,
                useSharedNetwork);

        if (config.serviceName != null) {
            container.withLabel(DevServicesMinioProcessor.DEV_SERVICE_LABEL, config.serviceName);
        }

        timeout.ifPresent(container::withStartupTimeout);

        container.withReuse(config.reuseEnabled);

        return container;
    }

    private Map<String, String> getRunningDevServicesConfig(
            Optional<Integer> maybeConsolePort,
            MinioDevServiceCfg config, String host, int port,
            MiniosBuildTimeConfiguration buildTimeConfiguration) {
        var result = new HashMap<String, String>();

        buildTimeConfiguration.getMinioClients().keySet().stream()
                .filter(minioClientName -> !minioClientName.equals("devservices"))
                .map(minioClientName -> Map.of(
                        formatPropertyName(MINIO_HOST, minioClientName), host,
                        formatPropertyName(MINIO_PORT, minioClientName), String.valueOf(port),
                        formatPropertyName(MINIO_SECURE, minioClientName), "false",
                        formatPropertyName(MINIO_ACCESS_KEY, minioClientName), config.accessKey,
                        formatPropertyName(MINIO_SECRET_KEY, minioClientName), config.secretKey))
                .forEach(result::putAll);
        maybeConsolePort.ifPresent(consolePort -> result.put(MINIO_CONSOLE, "http://%s:%s".formatted(host, consolePort)));
        return result;
    }

    private Map<String, Function<MinioContainer, String>> getRunningDevServicesConfig(MinioDevServiceCfg config,
            MiniosBuildTimeConfiguration buildTimeConfiguration) {
        var result = new HashMap<String, Function<MinioContainer, String>>();

        buildTimeConfiguration.getMinioClients().keySet().stream()
                .filter(minioClientName -> !minioClientName.equals("devservices"))
                .map(minioClientName -> Map.<String, Function<MinioContainer, String>> of(
                        formatPropertyName(MINIO_HOST, minioClientName), MinioContainer::getEffectiveHost,
                        formatPropertyName(MINIO_PORT, minioClientName),
                        minioContainer -> String.valueOf(minioContainer.getPort()),
                        formatPropertyName(MINIO_SECURE, minioClientName), minioContainer -> "false",
                        formatPropertyName(MINIO_ACCESS_KEY, minioClientName), minioContainer -> config.accessKey,
                        formatPropertyName(MINIO_SECRET_KEY, minioClientName), minioContainer -> config.secretKey))
                .forEach(result::putAll);
        result.put(MINIO_CONSOLE,
                minioContainer -> "http://%s:%s".formatted(minioContainer.getEffectiveHost(),
                        minioContainer.getConsolePort()));
        return result;
    }

    private static String formatPropertyName(String property) {
        return formatPropertyName(property, null);
    }

    private static String formatPropertyName(String property, String minoClientName) {
        var key = "";
        if (!MiniosBuildTimeConfiguration.isDefault(minoClientName) && minoClientName != null) {
            key = "." + minoClientName;
        }
        return String.format(property, key);
    }

    private MinioDevServiceCfg getConfiguration(MinioBuildTimeConfig cfg) {
        MinioDevServicesBuildTimeConfig devServicesConfig = cfg.devservices();
        return new MinioDevServiceCfg(devServicesConfig);
    }

    private static final class MinioDevServiceCfg {
        private final boolean devServicesEnabled;
        private final String imageName;
        private final Integer fixedExposedPort;
        private final boolean shared;
        private final boolean reuseEnabled;
        private final String serviceName;
        private final String accessKey;
        private final String secretKey;
        private final Map<String, String> containerEnv;

        public MinioDevServiceCfg(MinioDevServicesBuildTimeConfig config) {
            this.devServicesEnabled = config.enabled();
            this.imageName = config.imageName();
            this.fixedExposedPort = config.port();
            this.shared = config.shared();
            this.reuseEnabled = config.reuseEnabled();
            this.serviceName = config.serviceName();
            this.accessKey = config.accessKey();
            this.secretKey = config.secretKey();
            this.containerEnv = config.containerEnv();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            MinioDevServiceCfg that = (MinioDevServiceCfg) o;
            return devServicesEnabled == that.devServicesEnabled && Objects.equals(imageName, that.imageName)
                    && Objects.equals(fixedExposedPort, that.fixedExposedPort);
        }

        @Override
        public int hashCode() {
            return Objects.hash(devServicesEnabled, imageName, fixedExposedPort);
        }
    }

    private static final class MinioContainer extends GenericContainer<MinioContainer> implements Startable {

        private final int port;
        private final boolean useSharedNetwork;

        private String hostName = null;

        private MinioContainer(DockerImageName dockerImageName, int fixedExposedPort, String accessKey, String secretKey,
                Map<String, String> containerEnv, boolean useSharedNetwork) {
            super(dockerImageName);
            this.port = fixedExposedPort;
            this.useSharedNetwork = useSharedNetwork;
            withExposedPorts(DEVSERVICE_MINIO_PORT, DEVSERVICE_MINIO_CONSOLE_PORT)
                    .withEnv("MINIO_ACCESS_KEY", accessKey)
                    .withEnv("MINIO_SECRET_KEY", secretKey)
                    .withEnv(containerEnv)
                    .withCommand("server", "/data", "--console-address", ":9001")
                    .waitingFor(new HttpWaitStrategy().forPort(DEVSERVICE_MINIO_PORT).forPath("/minio/health/live"));
        }

        @Override
        protected void configure() {
            super.configure();

            if (useSharedNetwork) {
                hostName = ConfigureUtil.configureSharedNetwork(this, "minio");
            } else {
                withNetwork(Network.SHARED);
            }

            if (port > 0) {
                addFixedExposedPort(port, DEVSERVICE_MINIO_PORT);
            }
        }

        public String getEffectiveHost() {
            if (useSharedNetwork) {
                return hostName;
            }
            return getHost();
        }

        public int getPort() {
            if (useSharedNetwork) {
                return DEVSERVICE_MINIO_PORT;
            }
            return getMappedPort(DEVSERVICE_MINIO_PORT);
        }

        public int getConsolePort() {
            // console port is meant to be exposed only
            return getMappedPort(DEVSERVICE_MINIO_CONSOLE_PORT);
        }

        @Override
        public String getConnectionInfo() {
            return getHost() + ":" + getPort();
        }

        @Override
        public void close() {
            super.close();
        }
    }
}
