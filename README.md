# Quarkus Minio Extension
<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-4-orange.svg?style=flat-square)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->
[![Build](https://github.com/quarkiverse/quarkus-minio/workflows/Build/badge.svg)](https://github.com/quarkiverse/quarkus-minio/actions?query=workflow%3ABuild)
[![Maven Central](https://img.shields.io/maven-central/v/io.quarkiverse.minio/quarkus-minio-parent.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.quarkiverse.minio/quarkus-minio-parent)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Integrate minio sdk for jdk and native build modes.

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
    <artifactId>quarkus-minio</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

## Usage

An `io.minio.MinioClient` is made available to your application as a CDI bean if configuration is found.

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
Configuration is optional, but if present url has to be a valid http url.
If allow-empty is set to `true` and all other configuration options are empty, `null` is produced instead of the minio client.

```properties
quarkus.minio.url=https://minio.acme
quarkus.minio.access-key=DUMMY-ACCESS-KEY
quarkus.minio.secret-key=DUMMY-SECRET-KEY
quarkus.minio.allow-empty=true/false (Boolean type, default value is false)
```

## Contributors ✨

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tbody>
    <tr>
      <td align="center"><a href="https://github.com/jtama"><img src="https://avatars0.githubusercontent.com/u/39991688?v=4?s=100" width="100px;" alt="jtama"/><br /><sub><b>jtama</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-minio/commits?author=jtama" title="Code">💻</a> <a href="#maintenance-jtama" title="Maintenance">🚧</a></td>
      <td align="center"><a href="https://twitter.com/ppalaga"><img src="https://avatars.githubusercontent.com/u/1826249?v=4?s=100" width="100px;" alt="Peter Palaga"/><br /><sub><b>Peter Palaga</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-minio/commits?author=ppalaga" title="Code">💻</a></td>
      <td align="center"><a href="https://github.com/JiriOndrusek"><img src="https://avatars.githubusercontent.com/u/26897889?v=4?s=100" width="100px;" alt="JiriOndrusek"/><br /><sub><b>JiriOndrusek</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-minio/commits?author=JiriOndrusek" title="Code">💻</a></td>
      <td align="center"><a href="https://github.com/dlucasd"><img src="https://avatars.githubusercontent.com/u/8418431?v=4?s=100" width="100px;" alt="dlucasd"/><br /><sub><b>dlucasd</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-minio/commits?author=dlucasd" title="Code">💻</a> <a href="https://github.com/quarkiverse/quarkus-minio/commits?author=dlucasd" title="Documentation">📖</a></td>
    </tr>
  </tbody>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind welcome!

## WARNING

This project is still in its early stage.

Contributions are always welcome, but this repository is not really ready for external contributions yet, better create an issue
to discuss them prior to any contributions.
