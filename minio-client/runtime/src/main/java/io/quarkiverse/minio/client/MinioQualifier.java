package io.quarkiverse.minio.client;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;

/**
 * Qualifier used to specify which minio client will be injected.
 * kinda of mandatory cause of BeanInfo#124
 */
@Target({ METHOD, FIELD, PARAMETER, TYPE })
@Retention(RUNTIME)
@Documented
@Qualifier
public @interface MinioQualifier {

    String value();

    class MinioQualifierLiteral extends AnnotationLiteral<MinioQualifier> implements MinioQualifier {

        private String name;

        public MinioQualifierLiteral(String name) {
            this.name = name;
        }

        @Override
        public String value() {
            return name;
        }
    }
}
