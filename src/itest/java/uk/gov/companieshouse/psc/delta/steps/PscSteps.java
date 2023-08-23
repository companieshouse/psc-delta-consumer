package uk.gov.companieshouse.psc.delta.steps;

import com.github.tomakehurst.wiremock.WireMockServer;
import consumer.matcher.RequestMatcher;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.psc.delta.data.TestData;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

public class PscSteps {

    private static WireMockServer wireMockServer;

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

    private final String contextId = "123456789";

    public void sendMsgToKafkaTopic(String data) {
        kafkaTemplate.send(topic, data);
    }

    private void configureWireMock() {
        wireMockServer = new WireMockServer(Integer.parseInt(port));
        wireMockServer.start();
        configureFor("localhost", Integer.parseInt(port));
    }

    private void shutdownWireMock(){
        wireMockServer.shutdown();
    }

    @Given("the application is running")
    public void theApplicationRunning() {
        assertThat(kafkaTemplate).isNotNull();
    }

    @When("the consumer receives a message of kind {string} for company {string} with id {string}")
    public void the_consumer_receives_a_message(String pscKind, String companyNumber, String pscId)  throws Exception {
        configureWireMock();
        stubPutStatement(companyNumber, pscId, 200);
        ChsDelta delta = new ChsDelta(TestData.getCompanyDelta(pscKind + "_psc_delta.json"), 1, contextId, false);
        kafkaTemplate.send(topic, delta);
        countDown();
    }

    @Then("a PUT request is sent to the psc api with the transformed data for psc of kind {string} for company {string} with id {string}")
    public void aPutRequestIsSent(String pscKind, String companyNumber, String pscId) {
        String output = TestData.getOutputData(pscKind + "_psc_expected_output.json");
        verify(1, requestMadeFor(new RequestMatcher(logger, output,
                "/company/" + companyNumber + "/persons-with-significant-control/" + pscId + "/full_record",
                List.of("external_data.data.etag", "internal_data.delta_at"))));
        shutdownWireMock();
    }

    @When("an invalid avro message is sent")
    public void invalidAvroMessageIsSent() throws Exception {
        kafkaTemplate.send(topic, "InvalidData");

        countDown();
    }

    @When("a message with invalid data is sent")
    public void messageWithInvalidDataIsSent() throws Exception {
        ChsDelta delta = new ChsDelta("InvalidData", 1, "1", false);
        kafkaTemplate.send(topic, delta);

        countDown();
    }

    @When("the consumer receives a message but the api returns a (\\d*)$")
    public void theConsumerReceivesMessageButDataApiReturns(int responseCode, String companyNumber, String pscId) throws Exception{
        configureWireMock();
        stubPutStatement(companyNumber, pscId, 200);
        ChsDelta delta = new ChsDelta(TestData.getCompanyDelta("invidivual_psc_delta.json"), 1, "1", false);
        kafkaTemplate.send(topic, delta);

        countDown();
    }

    @Then("^the message should be moved to topic (.*)$")
    public void theMessageShouldBeMovedToTopic(String topic) {
        ConsumerRecord<String, Object> singleRecord = KafkaTestUtils.getSingleRecord(kafkaConsumer, topic);

        assertThat(singleRecord.value()).isNotNull();
    }

    @Then("^the message should retry (\\d*) times and then error$")
    public void theMessageShouldRetryAndError(int retries) {
        ConsumerRecords<String, Object> records = KafkaTestUtils.getRecords(kafkaConsumer);
        Iterable<ConsumerRecord<String, Object>> retryRecords =  records.records("company-psc-delta-retry");
        Iterable<ConsumerRecord<String, Object>> errorRecords =  records.records("company-psc-delta-error");

        int actualRetries = (int) StreamSupport.stream(retryRecords.spliterator(), false).count();
        int errors = (int) StreamSupport.stream(errorRecords.spliterator(), false).count();

        assertThat(actualRetries).isEqualTo(retries);
        assertThat(errors).isEqualTo(1);
    }

    private void stubPutStatement(String companyNumber, String pscId, int responseCode) {
        stubFor(put(urlEqualTo(
                "/company/" + companyNumber + "/persons-with-significant-control/" + pscId + "/full_record"))
                .willReturn(aResponse().withStatus(responseCode)));
    }

    private void countDown() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await(5, TimeUnit.SECONDS);
    }

}