package io.quarkiverse.minio.admin.client.deployment;

import java.util.ArrayList;
import java.util.List;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;

public class MinioAdminClientNativeProcessor {

    @BuildStep
    void addDependencies(BuildProducer<IndexDependencyBuildItem> indexDependency) {
        indexDependency.produce(new IndexDependencyBuildItem("io.minio", "minio-admin"));
    }

    @BuildStep
    void registerForReflection(
            CombinedIndexBuildItem index,
            BuildProducer<ReflectiveClassBuildItem> reflectionClasses) {
        List<String> classes = new ArrayList<>();
        classes.addAll(getClasses(index, "io.minio.admin.messages"));
        classes.addAll(getClasses(index, "io.minio.admin.messages.info"));
        classes.addAll(getClasses(index, "io.minio.admin", ".*Resp$|.*Info$"));
        reflectionClasses
                .produce(ReflectiveClassBuildItem.builder(classes.toArray(new String[0])).fields(true).methods(true).build());
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
