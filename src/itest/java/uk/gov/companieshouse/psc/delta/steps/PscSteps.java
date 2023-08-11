package uk.gov.companieshouse.psc.delta.steps;

import com.github.tomakehurst.wiremock.WireMockServer;
import consumer.matcher.RequestMatcher;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.psc.delta.data.TestData;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

public class PscSteps {

    private static WireMockServer wireMockServer;
    private String output;

    @Autowired
    private Logger logger;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public KafkaConsumer<String, Object> kafkaConsumer;

    @Value("${pscs.delta.topic}")
    private String topic;

    @Value("${wiremock.server.port}")
    private String port;

    public void sendMsgToKafkaTopic(String data) {
        kafkaTemplate.send(topic, data);
    }

    private void configureWireMock() {
        wireMockServer = new WireMockServer(Integer.parseInt(port));
        wireMockServer.start();
        configureFor("localhost", Integer.parseInt(port));
    }

    @Given("the application is running")
    public void theApplicationRunning() {
        assertThat(kafkaTemplate).isNotNull();
    }

    @When("the consumer receives a message")
    public void the_consumer_receives_a_message()  throws Exception {
        configureWireMock();
        stubPutStatement(200);
        ChsDelta delta = new ChsDelta(TestData.getCompanyDelta(), 1, "123456789", false);
        kafkaTemplate.send(topic, delta);
        countDown();
    }

    @Then("a PUT request is sent to the psc api with the transformed data")
    public void aPutRequestIsSent() {
        output = TestData.getOutputData();
        verify(1, requestMadeFor(new RequestMatcher(logger, output,
                "/company/34777772/persons-with-significant-control/ZfTs9WeeqpXTqf6dc6FZ4C0H0ZF",
                List.of("data.etag", "deltaAt"))));
    }

    private void stubPutStatement(int responseCode) {
        stubFor(put(urlEqualTo(
                "/company/34777772/persons-with-significant-control/ZfTs9WeeqpXTqf6dc6FZ4C0H0ZF"))
                .willReturn(aResponse().withStatus(responseCode)));
    }

    private void countDown() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await(5, TimeUnit.SECONDS);
    }
}