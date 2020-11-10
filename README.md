# Welcome to Quarkiverse!
<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-1-orange.svg?style=flat-square)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->

[![Build](https://github.com/quarkiverse/quarkiverse-minio/workflows/Build/badge.svg)](https://github.com/quarkiverse/quarkiverse-minio/actions?query=workflow%3ABuild)
[![Maven Central](https://img.shields.io/maven-central/v/io.quarkiverse.minio/quarkus-minio-client.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.quarkiverse.minio/quarkus-minio-client)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## Contributors ✨

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tr>
    <td align="center"><a href="https://github.com/jtama-op"><img src="https://avatars0.githubusercontent.com/u/39991688?v=4" width="100px;" alt=""/><br /><sub><b>jtama-op</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkiverse-minio/commits?author=jtama-op" title="Code">💻</a> <a href="#maintenance-jtama-op" title="Maintenance">🚧</a></td>
  </tr>
</table>

<!-- markdownlint-enable -->
<!-- prettier-ignore-end -->
<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind welcome!

## Configuration

After configuring `quarkus BOM`:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-bom</artifactId>
            <version>${insert.newest.quarkus.version.here}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

You can just configure the `quarkus-minio` extension by adding the following dependency:

```xml
<dependency>
    <groupId>io.quarkiverse.minio</groupId>
    <artifactId>quarkus-minio-client</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```
<!--
***NOTE:*** You can bootstrap a new application quickly by using [code.quarkus.io](https://code.quarkus.io) and choosing `quarkus-minio`.
-->

## Usage

An `io.minio.MinioClient` is made available to your application as a CDI bean.

```java
package com.acme.minio;

import io.minio.MinioClient;

import javax.enterprise.context.ApplicationScoped;

import javax.inject.Inject;

@ApplicationScoped
public class SampleService {

    @Inject
    MinioClient minioClient;

    @ConfigProperty(name = "minio.bucket-name")
    String bucketName;

    public String getObject(String name) {
        try (InputStream is = minio.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build());
        ) {
           // Do whatever you want...
        } catch (MinioException e) {
            throw new IllegalStateException(e);
        }
    }

}
```

## Configuration Reference

Configuration is done through standard application.properties mechanism.

```properties
quarkus.minio.url=https://minio.acme
quarkus.minio.access-key=DUMMY-ACCESS-KEY
quarkus.minio.secret-key=DUMMY-SECRET-KEY
```

## WARNING

This project is still in its early stage.

Contributions are always welcome, but this repository is not really ready for external contributions yet, better create an issue
to discuss them prior to any contributions.
