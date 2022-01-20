package com.browserstack.junit;

import io.cucumber.junit.CucumberOptions;
import org.apiguardian.api.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@API(status = API.Status.STABLE)
public @interface WebDriverOptions {

    int thread() default 1;

    boolean rerun() default false;

    CucumberOptions cucumberOptions();

   /* boolean dryRun() default false;

    String[] features() default {};

    String[] glue() default {};

    String[] extraGlue() default {};

    String tags() default "";

    String[] plugin() default {};

    boolean publish() default false;

    boolean monochrome() default false;

    String[] name() default {};

    SnippetType snippets() default SnippetType.UNDERSCORE;


    boolean useFileNameCompatibleName() default false;

    boolean stepNotifications() default false;


    Class<? extends io.cucumber.core.backend.ObjectFactory> objectFactory() default NoObjectFactory.class;

    enum SnippetType {
        UNDERSCORE, CAMELCASE
    }*/

}
