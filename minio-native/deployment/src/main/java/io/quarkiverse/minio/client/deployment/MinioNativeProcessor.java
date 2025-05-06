package io.quarkiverse.minio.client.deployment;

import java.util.ArrayList;
import java.util.List;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;

import io.minio.BaseArgs;
import io.minio.UploadSnowballObjectsArgs;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.ExtensionSslNativeSupportBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.builditem.nativeimage.RuntimeInitializedClassBuildItem;

class MinioNativeProcessor {

    private static final String FEATURE = "minio-client-native";

    @BuildStep
    public void build(BuildProducer<ExtensionSslNativeSupportBuildItem> extensionSslNativeSupport) {
        // Indicates that this extension would like the SSL support to be enabled
        extensionSslNativeSupport.produce(new ExtensionSslNativeSupportBuildItem(FEATURE));
    }

    @BuildStep
    void addDependencies(BuildProducer<IndexDependencyBuildItem> indexDependency) {
        indexDependency.produce(new IndexDependencyBuildItem("io.minio", "minio"));
        indexDependency.produce(new IndexDependencyBuildItem("com.carrotsearch.thirdparty", "simple-xml-safe"));
    }

    @BuildStep
    RuntimeInitializedClassBuildItem randomConfiguration() {
        return new RuntimeInitializedClassBuildItem(UploadSnowballObjectsArgs.class.getCanonicalName());
    }

    @BuildStep
    void registerForReflection(
            CombinedIndexBuildItem index,
            BuildProducer<ReflectiveClassBuildItem> reflectionClasses) {
        List<String> classes = new ArrayList<>();
        classes.addAll(getClasses(index, "io.minio.messages"));
        classes.addAll(getClasses(index, "org.simpleframework.xml.core", ".*Label$|.*Parameter$"));
        classes.addAll(index.getIndex().getAllKnownSubclasses(DotName.createSimple(BaseArgs.class.getName())).stream()
                .map(ClassInfo::name)
                .map(DotName::toString)
                .toList());
        reflectionClasses
                .produce(ReflectiveClassBuildItem.builder(classes.toArray(new String[0])).constructors(true).fields(true)
                        .methods(true).build());
    }

    private List<String> getClasses(CombinedIndexBuildItem index, String packageName) {
        return getClasses(index, packageName, ".*");
    }

    private List<String> getClasses(CombinedIndexBuildItem index, String packageName, String regexp) {
        return index.getIndex().getClassesInPackage(packageName).stream()
                .filter(c -> c.name().toString().matches(regexp))
                .map(c -> c.name().toString())
                .toList();
    }
}
