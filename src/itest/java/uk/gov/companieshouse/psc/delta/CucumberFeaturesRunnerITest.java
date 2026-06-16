package uk.gov.companieshouse.psc.delta;

import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;
import uk.gov.companieshouse.psc.delta.config.AbstractIntegrationTest;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "uk.gov.companieshouse.psc.delta")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty,json:target/cucumber-report.json")
@CucumberContextConfiguration
@SuppressWarnings("java:S2187") // Cucumber will run the scenarios, so no need for JUnit test methods in this class
public class CucumberFeaturesRunnerITest extends AbstractIntegrationTest {

}