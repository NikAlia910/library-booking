package com.mycompany.myapp.cucumber;

import static io.cucumber.junit.platform.engine.Constants.FILTER_TAGS_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

import org.junit.platform.suite.api.*;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("com/mycompany/myapp/cucumber")
@ConfigurationParameters(
    {
        @ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, html:target/cucumber-reports/Cucumber.html"),
        @ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.mycompany.myapp.cucumber"),
        @ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "not @Ignore"),
        @ConfigurationParameter(key = "cucumber.execution.parallel.enabled", value = "false"),
    }
)
class CucumberIT {}
