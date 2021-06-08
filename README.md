# Quarkus Minio Extension
<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-2-orange.svg?style=flat-square)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->

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

```properties
quarkus.minio.url=https://minio.acme
quarkus.minio.access-key=DUMMY-ACCESS-KEY
quarkus.minio.secret-key=DUMMY-SECRET-KEY
```

## Contributors ✨

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tr>
    <td align="center"><a href="https://github.com/jtama-op"><img src="https://avatars0.githubusercontent.com/u/39991688?v=4?s=100" width="100px;" alt=""/><br /><sub><b>jtama-op</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-minio/commits?author=jtama-op" title="Code">💻</a> <a href="#maintenance-jtama-op" title="Maintenance">🚧</a></td>
    <td align="center"><a href="https://twitter.com/ppalaga"><img src="https://avatars.githubusercontent.com/u/1826249?v=4?s=100" width="100px;" alt=""/><br /><sub><b>Peter Palaga</b></sub></a><br /><a href="https://github.com/quarkiverse/quarkus-minio/commits?author=ppalaga" title="Code">💻</a></td>
  </tr>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind welcome!

## WARNING

This project is still in its early stage.

Contributions are always welcome, but this repository is not really ready for external contributions yet, better create an issue
to discuss them prior to any contributions.
