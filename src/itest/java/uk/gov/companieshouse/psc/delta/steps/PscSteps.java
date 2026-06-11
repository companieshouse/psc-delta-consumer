package uk.gov.companieshouse.psc.delta.steps;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.requestMadeFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import uk.gov.companieshouse.api.delta.PscDeleteDelta.KindEnum;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.psc.delta.data.TestData;
import uk.gov.companieshouse.psc.delta.matcher.WiremockRequestMatcher;

public class PscSteps {

    private static final String DELTA_AT = "20230724093435661593";

    private static WireMockServer wireMockServer;

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaConsumer<String, Object> kafkaConsumer;
    private final KafkaConsumer<String, Object> invalidTopicConsumer;
    private final KafkaConsumer<String, Object> retryTopicConsumer;
    private final KafkaConsumer<String, Object> errorTopicConsumer;
    private final String topic;
    private final String port;
    private final String contextId = "123456789";

    public PscSteps(final KafkaTemplate<String, Object> kafkaTemplate,
            @Qualifier("mainTopicConsumer") final KafkaConsumer<String, Object> kafkaConsumer,
            @Qualifier("invalidTopicConsumer") final KafkaConsumer<String, Object> invalidTopicConsumer,
            @Qualifier("retryTopicConsumer") final KafkaConsumer<String, Object> retryTopicConsumer,
            @Qualifier("errorTopicConsumer") final KafkaConsumer<String, Object> errorTopicConsumer,
            @Value("${pscs.delta.topic}") final String topic,
            @Value("${wiremock.server.port:8888}") final String port) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaConsumer = kafkaConsumer;
        this.invalidTopicConsumer = invalidTopicConsumer;
        this.retryTopicConsumer = retryTopicConsumer;
        this.errorTopicConsumer = errorTopicConsumer;
        this.topic = topic;
        this.port = port;
    }

    /**
     * Reset Kafka consumer state before each scenario to ensure test isolation.
     * Seeks to the end of all subscribed partitions so previous messages don't pollute the current scenario.
     */
    @Before
    public void resetKafkaConsumerState() {
        // Poll briefly to ensure assignment, then seek to end of all partitions
        kafkaConsumer.poll(Duration.ofMillis(100));
        final var partitions = kafkaConsumer.assignment();
        if (!partitions.isEmpty()) {
            kafkaConsumer.seekToEnd(partitions);
            // Commit the new offsets so subsequent polls start from here
            kafkaConsumer.commitSync();
        }
    }

    public void sendMsgToKafkaTopic(final String data) {
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

    @When("the consumer receives a message of kind {string} for company {string} with psc id {string}")
    public void theConsumerReceivesAMessage(final String pscKind, final String companyNumber, final String pscId) throws Exception {
        configureWireMock();
        stubPutStatement(companyNumber, pscId, 200);
        final var delta = new ChsDelta(TestData.getCompanyDelta(pscKind + "_psc_delta.json"), 1, contextId, false);
        kafkaTemplate.send(topic, delta);
        countDown();
    }

    @When("the consumer receives a delete payload with {string}")
    public void theConsumerReceivesDelete(final String kind) throws Exception {
        configureWireMock();
        stubDeleteStatement(kind, 200);
        final var delta = new ChsDelta(TestData.getDeleteData(kind), 1, "1", true);
        kafkaTemplate.send(topic, delta);
        countDown();
    }

    @When("the consumer receives an invalid delete payload")
    public void theConsumerReceivesInvalidDelete() throws Exception {
        configureWireMock();
        final var delta = new ChsDelta("invalid", 1, "1", true);
        kafkaTemplate.send(topic, delta);

        countDown();
    }

    @When("an invalid avro message is sent")
    public void invalidAvroMessageIsSent() throws Exception {
        kafkaTemplate.send(topic, "InvalidData");

        countDown();
    }

    @When("a message with invalid data is sent")
    public void messageWithInvalidDataIsSent() throws Exception {
        final var delta = new ChsDelta("InvalidData", 1, "1", false);
        kafkaTemplate.send(topic, delta);

        countDown();
    }

    @When("the consumer receives a message for company {string} with notification id {string} but the api returns a {int}")
    public void theConsumerReceivesMessageButDataApiReturns(final String companyNumber, final String notificationId, final int responseCode)
            throws Exception {
        configureWireMock();
        stubPutStatement(companyNumber, notificationId, responseCode);
        final var delta = new ChsDelta(TestData.getCompanyDelta("individual_psc_delta.json"), 1, contextId, false);
        kafkaTemplate.send(topic, delta);

        countDown();
    }

    @When("^the consumer receives a delete message but the data api returns a (\\d*)$")
    public void theConsumerReceivesDeleteMessageButDataApiReturns(final int responseCode) throws Exception {
        configureWireMock();
        stubDeleteStatement(KindEnum.INDIVIDUAL.getValue(), responseCode);
        final var delta = new ChsDelta(TestData.getDeleteData(KindEnum.INDIVIDUAL.getValue()), 1, "1", true);
        kafkaTemplate.send(topic, delta);

        countDown();
    }

    @Then("a PUT request is sent to the psc api with the transformed data for psc of kind {string} for company {string} with id {string}")
    public void aPutRequestIsSent(final String pscKind, final String companyNumber, final String pscId) {
        final String output = TestData.getOutputData(pscKind + "_psc_expected_output.json");

        verify(1, requestMadeFor(
                new WiremockRequestMatcher(output,
                        "/company/" + companyNumber + "/persons-with-significant-control/" + pscId + "/full_record",
                        List.of("external_data.data.etag",
                            "external_data.data.identityVerificationDetails",
                            "internal_data.delta_at"))));
    }

    @Then("^the message should be moved to topic (.*)$")
    public void theMessageShouldBeMovedToTopic(final String topic) {
        // Use dedicated consumer for invalid topic
        final ConsumerRecord<String, Object> singleRecord =
            KafkaTestUtils.getSingleRecord(invalidTopicConsumer, topic);
        assertThat(singleRecord.value()).isNotNull();
    }

    @Then("^the message should retry (\\d*) times and then error$")
    public void theMessageShouldRetryAndError(final int retries) {
        // Get records from dedicated retry and error consumers
        final ConsumerRecords<String, Object> retryRecords = KafkaTestUtils.getRecords(retryTopicConsumer);
        final ConsumerRecords<String, Object> errorRecords = KafkaTestUtils.getRecords(errorTopicConsumer);

        final int actualRetries = retryRecords.count();
        final int errors = errorRecords.count();

        assertThat(actualRetries).isEqualTo(retries);
        assertThat(errors).isEqualTo(1);
    }

    @Then("a DELETE request is sent to the psc data api with the {string}")
    public void deleteRequestIsSent(final String kind) {
        verify(1, deleteRequestedFor(urlMatching(
                "/company/OE623672/persons-with-significant-control/lXgouUAR16hSIwxdJSpbr_dhyT8/full_record"))
                .withHeader("X-KIND", containing(kind))
                .withHeader("X-DELTA-AT", containing(DELTA_AT)));
    }

    @After
    public void shutdownWiremock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    private void stubPutStatement(final String companyNumber, final String notificationId, final int responseCode) {
        stubFor(put(urlEqualTo(
                "/company/" + companyNumber + "/persons-with-significant-control/" + notificationId + "/full_record"))
                .willReturn(aResponse().withStatus(responseCode)));
    }

    private void stubDeleteStatement(final String kind, final int responseCode) {
        stubFor(delete(urlEqualTo(
                "/company/OE623672/persons-with-significant-control/lXgouUAR16hSIwxdJSpbr_dhyT8/full_record"))
                .withHeader("X-KIND", containing(kind))
                .withHeader("X-DELTA-AT", containing(DELTA_AT))
                .willReturn(aResponse().withStatus(responseCode)));
    }

    private void countDown() throws Exception {
        final var countDownLatch = new CountDownLatch(1);
        countDownLatch.await(5, TimeUnit.SECONDS);
    }
}