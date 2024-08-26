package io.quarkiverse.minio.client.deployment.devui;

import io.quarkiverse.minio.client.MiniosBuildTimeConfiguration;
import io.quarkiverse.minio.client.deployment.devservices.MinioBuildTimeConfig;
import io.quarkiverse.minio.client.devui.MinioJsonRPCService;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.devui.spi.JsonRPCProvidersBuildItem;
import io.quarkus.devui.spi.page.CardPageBuildItem;
import io.quarkus.devui.spi.page.Page;

public class MinioClientDevUIProcessor {

    @BuildStep(onlyIf = IsDevelopment.class)
    void createCard(BuildProducer<CardPageBuildItem> cardProducer, MinioBuildTimeConfig devServiceConfig,
            MiniosBuildTimeConfiguration configuration) {

        final CardPageBuildItem card = new CardPageBuildItem();

        card.addBuildTimeData("enabled", devServiceConfig.devservices().enabled());

        card.addPage(Page.externalPageBuilder("Min.io console")
                .icon("font-awesome-solid:signs-post")
                .dynamicUrlJsonRPCMethodName("getMinioConsoleUrl")
                .doNotEmbed());

        card.setCustomCard("qwc-minio-card.js");

        cardProducer.produce(card);
    }

    @BuildStep(onlyIf = IsDevelopment.class)
    JsonRPCProvidersBuildItem createJsonRPCServiceForCache() {
        return new JsonRPCProvidersBuildItem(MinioJsonRPCService.class);
    }

}
