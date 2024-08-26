package io.quarkiverse.minio.client.deployment.devservices;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.jboss.logging.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.utility.DockerImageName;

import io.quarkiverse.minio.client.MiniosBuildTimeConfiguration;
import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CuratedApplicationShutdownBuildItem;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem.RunningDevService;
import io.quarkus.deployment.builditem.DevServicesSharedNetworkBuildItem;
import io.quarkus.deployment.builditem.DockerStatusBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.console.ConsoleInstalledBuildItem;
import io.quarkus.deployment.console.StartupLogCompressor;
import io.quarkus.deployment.dev.devservices.GlobalDevServicesConfig;
import io.quarkus.deployment.logging.LoggingSetupBuildItem;
import io.quarkus.devservices.common.ConfigureUtil;
import io.quarkus.devservices.common.ContainerAddress;
import io.quarkus.devservices.common.ContainerLocator;
import io.quarkus.devservices.common.ContainerShutdownCloseable;
import io.quarkus.runtime.configuration.ConfigUtils;

public class DevServicesMinioProcessor {
    private static final Logger LOGGER = Logger.getLogger(DevServicesMinioProcessor.class);
    private static final String MINIO_URL = "quarkus.minio%s.url";
    private static final String MINIO_PORT = "quarkus.minio%s.port";
    private static final String MINIO_SECURE = "quarkus.minio%s.secure";
    private static final String MINIO_CONSOLE = "quarkus.minio.console";
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
    static volatile RunningDevService devService;
    static volatile MinioDevServiceCfg cfg;
    static volatile boolean first = true;

    @BuildStep(onlyIfNot = IsNormal.class, onlyIf = GlobalDevServicesConfig.Enabled.class)
    public DevServicesResultBuildItem startMinioDevService(
            DockerStatusBuildItem dockerStatusBuildItem,
            LaunchModeBuildItem launchMode,
            MinioBuildTimeConfig minioBuildTimeConfig,
            Optional<ConsoleInstalledBuildItem> consoleInstalledBuildItem,
            CuratedApplicationShutdownBuildItem closeBuildItem,
            LoggingSetupBuildItem loggingSetupBuildItem,
            GlobalDevServicesConfig devServicesConfig,
            MiniosBuildTimeConfiguration buildTimeConfiguration,
            List<DevServicesSharedNetworkBuildItem> devServicesSharedNetworkBuildItem) {

        MinioDevServiceCfg configuration = getConfiguration(minioBuildTimeConfig);

        if (devService != null) {
            boolean shouldShutdownTheBroker = !configuration.equals(cfg);
            if (!shouldShutdownTheBroker) {
                return devService.toBuildItem();
            }
            shutdownServer();
            cfg = null;
        }

        StartupLogCompressor compressor = new StartupLogCompressor(
                (launchMode.isTest() ? "(test) " : "") + "Minio Dev Services Starting:",
                consoleInstalledBuildItem, loggingSetupBuildItem);
        try {
            devService = startMinio(dockerStatusBuildItem, configuration, launchMode, devServicesConfig.timeout,
                    buildTimeConfiguration, !devServicesSharedNetworkBuildItem.isEmpty());
            if (devService == null) {
                compressor.closeAndDumpCaptured();
            } else {
                compressor.close();
            }
        } catch (Throwable t) {
            compressor.closeAndDumpCaptured();
            throw t instanceof RuntimeException ? (RuntimeException) t : new RuntimeException(t);
        }

        if (devService == null) {
            return null;
        }

        if (first) {
            first = false;
            Runnable closeTask = () -> {
                if (devService != null) {
                    shutdownServer();
                }
                first = true;
                devService = null;
                cfg = null;
            };
            closeBuildItem.addCloseTask(closeTask, true);
        }
        cfg = configuration;

        if (devService.isOwner()) {
            LOGGER.infof("Dev Services for Minio started on %s", getMinioUrl());
            LOGGER.infof("Other Quarkus applications in dev mode will find the "
                    + "instance automatically. For Quarkus applications in production mode, you can connect to"
                    + " this by starting your application with -D%s=%s -D%s=%s -D%s=%s",
                    formatPropertyName(MINIO_URL), getMinioUrl(),
                    formatPropertyName(MINIO_SECURE), false,
                    formatPropertyName(MINIO_PORT), DEVSERVICE_MINIO_PORT,
                    formatPropertyName(MINIO_ACCESS_KEY), getMinioAccessKey(),
                    formatPropertyName(MINIO_SECRET_KEY), getMinioSecretKey());
        }

        return devService.toBuildItem();
    }

    public static String getMinioUrl() {
        return devService.getConfig().get(formatPropertyName(MINIO_URL));
    }

    public static String getMinioAccessKey() {
        return devService.getConfig().get(formatPropertyName(MINIO_ACCESS_KEY));
    }

    public static String getMinioSecretKey() {
        return devService.getConfig().get(formatPropertyName(MINIO_SECRET_KEY));
    }

    private void shutdownServer() {
        if (devService != null) {
            try {
                devService.close();
            } catch (Throwable e) {
                LOGGER.error("Failed to stop the Minio server", e);
            } finally {
                devService = null;
            }
        }
    }

    private RunningDevService startMinio(
            DockerStatusBuildItem dockerStatusBuildItem,
            MinioDevServiceCfg config,
            LaunchModeBuildItem launchMode,
            Optional<Duration> timeout,
            MiniosBuildTimeConfiguration buildTimeConfiguration,
            boolean useSharedNetwork) {
        if (!config.devServicesEnabled) {
            // explicitly disabled
            LOGGER.debug("Not starting dev services for Minio, as it has been disabled in the config.");
            return null;
        }

        // Check if quarkus.minio.url is set
        if (ConfigUtils.isPropertyPresent(formatPropertyName(MINIO_URL))) {
            LOGGER.debug("Not starting dev services for Minio, the quarkus.minio.url is configured.");
            return null;
        }

        // Check if quarkus.minio.allow-empty is set to true
        Optional<Boolean> allowEmpty = ConfigUtils.getFirstOptionalValue(List.of(formatPropertyName(MINIO_ALLOW_EMPTY)),
                Boolean.class);
        if (allowEmpty.isPresent() && allowEmpty.get()) {
            LOGGER.debug("Not starting dev services for Minio, the quarkus.minio.allow-empty is set to true.");
            return null;
        }

        if (!dockerStatusBuildItem.isDockerAvailable()) {
            LOGGER.warn(String.format("Docker isn't working, please configure the Minio Url property (%s).",
                    formatPropertyName(MINIO_URL)));
            return null;
        }

        final Optional<ContainerAddress> maybeContainerAddress = minioContainerLocator.locateContainer(config.serviceName,
                config.shared,
                launchMode.getLaunchMode());
        final Optional<Integer> maybeConsolePort = minioContainerLocator.locatePublicPort(config.serviceName,
                config.shared,
                launchMode.getLaunchMode(), 9001);

        // Starting the server
        final Supplier<RunningDevService> defaultMinioBrokerSupplier = () -> {
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

            container.withReuse(true);

            container.start();
            return new RunningDevService(config.serviceName,
                    container.getContainerId(),
                    new ContainerShutdownCloseable(container, config.serviceName),
                    getRunningDevServicesConfig(config, container.getEffectiveHost(), container.getPort(), container.getHost(),
                            Optional.of(container.getConsolePort()),
                            buildTimeConfiguration));
        };

        return maybeContainerAddress
                .map(containerAddress -> new RunningDevService(config.serviceName,
                        containerAddress.getId(),
                        null,
                        getRunningDevServicesConfig(config, containerAddress.getHost(), containerAddress.getPort(),
                                containerAddress.getHost(),
                                maybeConsolePort, buildTimeConfiguration)))
                .orElseGet(defaultMinioBrokerSupplier);
    }

    private Map<String, String> getRunningDevServicesConfig(MinioDevServiceCfg config, String host, int port,
            String consoleHost,
            Optional<Integer> maybeConsolePort, MiniosBuildTimeConfiguration buildTimeConfiguration) {
        var result = new HashMap<String, String>();
        maybeConsolePort
                .ifPresent(consolePort -> result.put(MINIO_CONSOLE, String.format("http://%s:%d", consoleHost, consolePort)));

        buildTimeConfiguration.getMinioClients().entrySet().stream()
                .map(entry -> Map.of(formatPropertyName(MINIO_URL, entry.getKey()), host,
                        formatPropertyName(MINIO_PORT, entry.getKey()), String.valueOf(port),
                        formatPropertyName(MINIO_SECURE, entry.getKey()), "false",
                        formatPropertyName(MINIO_ACCESS_KEY, entry.getKey()), config.accessKey,
                        formatPropertyName(MINIO_SECRET_KEY, entry.getKey()), config.secretKey))
                .forEach(result::putAll);
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
        private final String serviceName;
        private final String accessKey;
        private final String secretKey;
        private final Map<String, String> containerEnv;

        public MinioDevServiceCfg(MinioDevServicesBuildTimeConfig config) {
            this.devServicesEnabled = config.enabled();
            this.imageName = config.imageName();
            this.fixedExposedPort = config.port();
            this.shared = config.shared();
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

    /**
     * Container configuring and starting the Minio server.
     */
    private static final class MinioContainer extends GenericContainer<MinioContainer> {

        private final int port;
        private final boolean useSharedNetwork;

        private String hostName = null;

        private MinioContainer(DockerImageName dockerImageName, int fixedExposedPort, String accessKey, String secretKey,
                Map<String, String> containerEnv, boolean useSharedNetwork) {
            super(dockerImageName);
            this.port = fixedExposedPort;
            this.useSharedNetwork = useSharedNetwork;
            withExposedPorts(DEVSERVICE_MINIO_PORT, DEVSERVICE_MINIO_CONSOLE_PORT);
            withEnv("MINIO_ACCESS_KEY", accessKey);
            withEnv("MINIO_SECRET_KEY", secretKey);
            withEnv(containerEnv);
            withCommand("server", "/data", "--console-address", ":9001");
            waitingFor(new HttpWaitStrategy().forPort(9000).forPath("/minio/health/live"));
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
    }
}
