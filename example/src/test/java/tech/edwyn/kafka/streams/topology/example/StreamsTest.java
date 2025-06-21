package tech.edwyn.kafka.streams.topology.example;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class StreamsTest {

  @Autowired
  private StreamsBuilderFactoryBean streamsBuilderFactoryBean;
  @Autowired
  private ExampleProperties.Topics topics;
  @Autowired
  private ExampleProperties.Stores stores;

  private TopologyTestDriver topologyTestDriver;
  private TestInputTopic<Integer, String> testInputTopic;
  private TestOutputTopic<Integer, String> testOutputTopic;

  @BeforeEach
  void setUp() {
    assertThat(streamsBuilderFactoryBean.getTopology()).isNotNull();

    topologyTestDriver = new TopologyTestDriver(
      streamsBuilderFactoryBean.getTopology(),
      streamsBuilderFactoryBean.getStreamsConfiguration());

    try (var keySerde = Serdes.Integer();
         var valueSerde = Serdes.String()) {
      testInputTopic = topologyTestDriver.createInputTopic(
        topics.input(),
        keySerde.serializer(),
        valueSerde.serializer());
      testOutputTopic = topologyTestDriver.createOutputTopic(
        topics.output(),
        keySerde.deserializer(),
        valueSerde.deserializer());
    }
  }

  @AfterEach
  void tearDown() {
    Optional.ofNullable(topologyTestDriver)
            .ifPresent(TopologyTestDriver::close);
  }

  @Test
  void uniqueInput() {
    testInputTopic.pipeInput(1, "test");

    List<String> output = testOutputTopic.readValuesToList();
    assertThat(output).hasSize(1)
                      .contains("TEST");

    KeyValueStore<String, String> historyStore = topologyTestDriver.getKeyValueStore(stores.history());
    assertThat(historyStore.all()).toIterable()
                                  .hasSize(1)
                                  .contains(KeyValue.pair("test", "TEST"));
  }

  @Test
  void sameInputTwice() {
    testInputTopic.pipeInput(1, "test");
    testInputTopic.pipeInput(2, "test");

    List<String> output = testOutputTopic.readValuesToList();
    assertThat(output).hasSize(2)
                      .contains("TEST", "TEST");

    KeyValueStore<String, String> historyStore = topologyTestDriver.getKeyValueStore(stores.history());
    assertThat(historyStore.all()).toIterable()
                                  .hasSize(1)
                                  .contains(KeyValue.pair("test", "TEST"));
  }

  @Test
  void uniqueInputTwice() {
    testInputTopic.pipeInput(1, "first");
    testInputTopic.pipeInput(2, "second");

    List<String> output = testOutputTopic.readValuesToList();
    assertThat(output).hasSize(2)
                      .contains("FIRST", "SECOND");

    KeyValueStore<String, String> historyStore = topologyTestDriver.getKeyValueStore(stores.history());
    assertThat(historyStore.all()).toIterable()
                                  .hasSize(2)
                                  .contains(
                                    KeyValue.pair("first", "FIRST"),
                                    KeyValue.pair("second", "SECOND"));
  }

  @TestConfiguration
  public static class TestConfig {

    @Bean
    public ExampleProperties.Topics topics(ExampleProperties exampleProperties) {
      return exampleProperties.topics();
    }

    @Bean
    public ExampleProperties.Stores stores(ExampleProperties exampleProperties) {
      return exampleProperties.stores();
    }
  }

}
