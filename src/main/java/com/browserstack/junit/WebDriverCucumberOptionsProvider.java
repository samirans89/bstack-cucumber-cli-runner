package com.browserstack.junit;

import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.options.CucumberOptionsAnnotationParser;
import io.cucumber.core.snippets.SnippetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


final class WebDriverCucumberOptionsProvider implements CucumberOptionsAnnotationParser.OptionsProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebDriverCucumberOptionsProvider.class);

    @Override
    public CucumberOptionsAnnotationParser.CucumberOptions getOptions(Class<?> clazz) {
        WebDriverOptions annotation = clazz.getAnnotation(WebDriverOptions.class);
        if (annotation != null) {
            return new WebDriverCucumberOptions(annotation);
        }
        LOGGER.warn("WebDriver cucumber options are missing");
        return null;
    }

    private static class WebDriverCucumberOptions implements CucumberOptionsAnnotationParser.CucumberOptions {

        private final WebDriverOptions annotation;

        WebDriverCucumberOptions(WebDriverOptions annotation) {
            this.annotation = annotation;
        }

        @Override
        public boolean dryRun() {
            return annotation.cucumberOptions().dryRun();
        }

        @Override
        public String[] features() {
            return annotation.cucumberOptions().features();
        }

        @Override
        public String[] glue() {
            return annotation.cucumberOptions().glue();
        }

        @Override
        public String[] extraGlue() {
            return annotation.cucumberOptions().extraGlue();
        }

        @Override
        public String tags() {
            return annotation.cucumberOptions().tags();
        }

        @Override
        public String[] plugin() {
            return annotation.cucumberOptions().plugin();
        }

        @Override
        public boolean publish() {
            return annotation.cucumberOptions().publish();
        }

        @Override
        public boolean monochrome() {
            return annotation.cucumberOptions().monochrome();
        }

        @Override
        public String[] name() {
            return annotation.cucumberOptions().name();
        }

        @Override
        public SnippetType snippets() {
            switch (annotation.cucumberOptions().snippets()) {
                case UNDERSCORE:
                    return SnippetType.UNDERSCORE;
                case CAMELCASE:
                    return SnippetType.CAMELCASE;
                default:
                    throw new IllegalArgumentException("" + annotation.cucumberOptions().snippets());
            }
        }

        @Override
        public Class<? extends ObjectFactory> objectFactory() {
            return annotation
                    .cucumberOptions()
                    .objectFactory().getName()
                    .equals("io.cucumber.junit.NoObjectFactory")?null:annotation.cucumberOptions().objectFactory();
        }

    }

}