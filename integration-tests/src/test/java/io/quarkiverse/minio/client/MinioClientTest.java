package io.quarkiverse.minio.client;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import org.hamcrest.CoreMatchers;
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
        given()
                .when().post("/q/metrics")
                .then()
                .statusCode(200)
                .body(CoreMatchers.containsString("minio_client_seconds"));
    }

    @Test
    public void testPolicy() {
        given()
                .when().get("/minio/yoooo")
                .then()
                .statusCode(204);
    }

    @Test
    public void testInsertToAnother() {
        String response = given()
                .when().post("/another-minio?name=dummy.txt")
                .then()
                .statusCode(200)
                .extract().body().asString();
        assertThat(response).isEqualTo("another-test/dummy.txt");
        given()
                .when().post("/q/metrics")
                .then()
                .statusCode(200)
                .body(CoreMatchers.containsString("minio_another_client_seconds"));
    }

    @Test
    public void testInsertAsync() {
        String response = given()
                .when().get("/async-minio?name=dummy-bucket")
                .then()
                .statusCode(200)
                .extract().body().asString();

        assertThat(response).isEqualTo("Bucket doesn't exist");
    }
}
