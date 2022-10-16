package io.quarkiverse.minio.client;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class MinioClientTest {

    @Test
    public void testInsert() {
        String response = given()
                .when().post("/minio?name=dummy.txt")
                .then()
                .statusCode(200)
                .extract().body().asString();
        assertThat(response).isEqualTo("test/dummy.txt");
    }

}
