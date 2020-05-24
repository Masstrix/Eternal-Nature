package me.masstrix.version;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface IVersion {

    /**
     * @return the pattern used for checking if the version is valid.
     */
    String pattern() default "*";
}
