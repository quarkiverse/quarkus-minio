package io.quarkiverse.minio.client;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class MinioAdminClientTest {

    @Test
    public void testAdminClient() {
        String response = given()
                .when().get("/adminClient")
                .then()
                .statusCode(200)
                .extract().body().asString();

        assertThat(response).isNotEmpty();
    }

    @Test
    public void testAdminClientCreateDeleteUser() {
        String response = given()
                .when().get("/adminClient/user")
                .then()
                .statusCode(200)
                .extract().body().asString();

        assertThat(response).isEqualTo("added and deleted user");
    }

}
