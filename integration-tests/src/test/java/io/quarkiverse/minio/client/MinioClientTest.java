package io.quarkiverse.minio.client;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.utility.DockerImageName;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class MinioClientTest {

    private static GenericContainer<?> MINIO_CONTAINER;

    @BeforeAll
    public static void startMinio() {
        MINIO_CONTAINER = new GenericContainer(DockerImageName.parse("minio/minio:RELEASE.2020-11-10T21-02-24Z"))
                .withExposedPorts(9000)
                .withEnv("MINIO_ACCESS_KEY", "minioaccess")
                .withEnv("MINIO_SECRET_KEY", "miniosecret")
                .withCommand("server /data")
                .waitingFor(new HttpWaitStrategy().forPort(9000).forPath("/minio/health/live"));
        List<String> portBindings = new ArrayList<>();
        portBindings.add("9000:9000");
        MINIO_CONTAINER.setPortBindings(portBindings);
        MINIO_CONTAINER.start();
    }

    @Test
    public void testInsert() throws IOException {
        String response = given()
                .when().post("/minio?name=dummy.txt")
                .then()
                .statusCode(200)
                .extract().body().asString();
        assertThat(response).isEqualTo("test/dummy.txt");
    }

}
