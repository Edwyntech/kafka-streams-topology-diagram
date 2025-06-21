package tech.edwyn.kafka.streams.topology;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.processor.api.ContextualFixedKeyProcessor;
import org.apache.kafka.streams.processor.api.FixedKeyRecord;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.regex.Pattern;

import static org.apache.kafka.common.serialization.Serdes.Integer;
import static org.apache.kafka.common.serialization.Serdes.String;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;
import static tech.edwyn.kafka.streams.topology.KafkaStreamsTopologyDiagramDescriptionBuilderTest.TestConfig;

@SpringBootTest(classes = TestConfig.class)
@ActiveProfiles("test")
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
class KafkaStreamsTopologyDiagramDescriptionBuilderTest {

  @Autowired
  private StreamsBuilder streamsBuilder;

  @Test
  void buildsTopologyDiagramWithSingleSource() {
    streamsBuilder.stream("test",
      Consumed.with(Integer(), String())
              .withName("test-source"));

    Topology topology = streamsBuilder.build();
    KafkaStreamsTopologyDiagramBuilder kafkaStreamsTopologyDiagramBuilder = new KafkaStreamsTopologyDiagramBuilder();
    String topologyDiagram = kafkaStreamsTopologyDiagramBuilder.buildFor(topology);

    assertThat(topologyDiagram).hasToString("""
      flowchart TD
      \ttest@{ shape: das, label: 'test' }
      \ttest-source@{ shape: docs, label: 'test-source' }
      \tsubgraph Sub-topology: 0
      \t\ttest --> test-source
      \tend
      """);
  }

  @Test
  void buildsTopologyDiagramWithSingleSourceAndTopicPattern() {
    streamsBuilder.stream(Pattern.compile("test"),
      Consumed.with(Integer(), String())
              .withName("test-source"));

    Topology topology = streamsBuilder.build();
    KafkaStreamsTopologyDiagramBuilder kafkaStreamsTopologyDiagramBuilder = new KafkaStreamsTopologyDiagramBuilder();
    String topologyDiagram = kafkaStreamsTopologyDiagramBuilder.buildFor(topology);

    assertThat(topologyDiagram).hasToString("""
      flowchart TD
      \ttest@{ shape: das, label: 'test' }
      \ttest-source@{ shape: docs, label: 'test-source' }
      \tsubgraph Sub-topology: 0
      \t\ttest --> test-source
      \tend
      """);
  }

  @Test
  void buildsTopologyDiagramWithTwoSources() {
    streamsBuilder.stream("first",
      Consumed.with(Integer(), String())
              .withName("first-source"));
    streamsBuilder.stream("second",
      Consumed.with(Integer(), String())
              .withName("second-source"));

    Topology topology = streamsBuilder.build();
    KafkaStreamsTopologyDiagramBuilder kafkaStreamsTopologyDiagramBuilder = new KafkaStreamsTopologyDiagramBuilder();
    String topologyDiagram = kafkaStreamsTopologyDiagramBuilder.buildFor(topology);

    assertThat(topologyDiagram).hasToString("""
      flowchart TD
      \tfirst@{ shape: das, label: 'first' }
      \tsecond@{ shape: das, label: 'second' }
      \tfirst-source@{ shape: docs, label: 'first-source' }
      \tsecond-source@{ shape: docs, label: 'second-source' }
      \tsubgraph Sub-topology: 0
      \t\tfirst --> first-source
      \tend
      \tsubgraph Sub-topology: 1
      \t\tsecond --> second-source
      \tend
      """);
  }

  @Test
  void buildsTopologyDiagramWithProcessor() {
    streamsBuilder.stream("test",
                    Consumed.with(Integer(), String())
                            .withName("test-source"))

                  .processValues(() -> new ContextualFixedKeyProcessor<Integer, String, String>() {
                      @Override
                      public void process(FixedKeyRecord<Integer, String> record) {
                        context().forward(record);
                      }
                    },
                    Named.as("test-processor"));

    Topology topology = streamsBuilder.build();
    KafkaStreamsTopologyDiagramBuilder kafkaStreamsTopologyDiagramBuilder = new KafkaStreamsTopologyDiagramBuilder();
    String topologyDiagram = kafkaStreamsTopologyDiagramBuilder.buildFor(topology);

    assertThat(topologyDiagram).hasToString("""
      flowchart TD
      \ttest@{ shape: das, label: 'test' }
      \ttest-source@{ shape: docs, label: 'test-source' }
      \ttest-processor@{ shape: subproc, label: 'test-processor' }
      \tsubgraph Sub-topology: 0
      \t\ttest --> test-source
      \t\ttest-source --> test-processor
      \tend
      """);
  }

  @Test
  void buildsTopologyDiagramWithProcessorAndStore() {
    streamsBuilder.addStateStore(Stores.keyValueStoreBuilder(
      Stores.inMemoryKeyValueStore("test-store"),
      Serdes.String(),
      Serdes.String()));
    streamsBuilder.stream("test",
                    Consumed.with(Integer(), String())
                            .withName("test-source"))

                  .processValues(() -> new ContextualFixedKeyProcessor<Integer, String, String>() {
                      @Override
                      public void process(FixedKeyRecord<Integer, String> record) {
                        KeyValueStore<Integer, String> store = context().getStateStore("test-store");
                        store.put(record.key(), record.value());
                        context().forward(record);
                      }
                    },
                    Named.as("test-processor"),
                    "test-store");

    Topology topology = streamsBuilder.build();
    KafkaStreamsTopologyDiagramBuilder kafkaStreamsTopologyDiagramBuilder = new KafkaStreamsTopologyDiagramBuilder();
    String topologyDiagram = kafkaStreamsTopologyDiagramBuilder.buildFor(topology);

    assertThat(topologyDiagram).hasToString("""
      flowchart TD
      \ttest@{ shape: das, label: 'test' }
      \ttest-store@{ shape: database, label: 'test-store' }
      \ttest-source@{ shape: docs, label: 'test-source' }
      \ttest-processor@{ shape: subproc, label: 'test-processor' }
      \tsubgraph Sub-topology: 0
      \t\ttest --> test-source
      \t\ttest-source --> test-processor
      \t\ttest-processor --> test-store
      \tend
      """);
  }

  @Test
  void buildsTopologyDiagramWithSink() {
    KStream<Integer, String> stream = streamsBuilder.stream("test-in",
      Consumed.with(Integer(), String())
              .withName("test-source"));
    stream.to("test-out",
      Produced.with(Integer(), String())
              .withName("test-sink"));

    Topology topology = streamsBuilder.build();
    KafkaStreamsTopologyDiagramBuilder kafkaStreamsTopologyDiagramBuilder = new KafkaStreamsTopologyDiagramBuilder();
    String topologyDiagram = kafkaStreamsTopologyDiagramBuilder.buildFor(topology);

    assertThat(topologyDiagram).hasToString("""
      flowchart TD
      \ttest-in@{ shape: das, label: 'test-in' }
      \ttest-out@{ shape: das, label: 'test-out' }
      \ttest-source@{ shape: docs, label: 'test-source' }
      \ttest-sink@{ shape: stadium, label: 'test-sink' }
      \tsubgraph Sub-topology: 0
      \t\ttest-in --> test-source
      \t\ttest-source --> test-sink
      \t\ttest-sink --> test-out
      \tend
      """);
  }

  @SpringBootConfiguration
  @EnableKafkaStreams
  @EnableAutoConfiguration
  public static class TestConfig {
  }

}
