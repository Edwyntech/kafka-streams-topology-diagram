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
    TopologyDiagram topologyDiagram = kafkaStreamsTopologyDiagramBuilder.buildFrom(topology);

    assertThat(topologyDiagram.toString())
      .isEqualToIgnoringWhitespace("""
        flowchart TD
        
          test@{ shape: das, label: 'test' }
          test-source@{ shape: docs, label: 'test-source' }
        
          subgraph Sub-topology: 0
            test --> test-source
          end
        """);
  }

  @Test
  void buildsTopologyDiagramWithSingleSourceAndTopicPattern() {
    streamsBuilder.stream(Pattern.compile("test"),
      Consumed.with(Integer(), String())
              .withName("test-source"));

    Topology topology = streamsBuilder.build();
    KafkaStreamsTopologyDiagramBuilder kafkaStreamsTopologyDiagramBuilder = new KafkaStreamsTopologyDiagramBuilder();
    TopologyDiagram topologyDiagram = kafkaStreamsTopologyDiagramBuilder.buildFrom(topology);

    assertThat(topologyDiagram.toString())
      .isEqualToNormalizingWhitespace("""
        flowchart TD
        
          test@{ shape: das, label: 'test' }
          test-source@{ shape: docs, label: 'test-source' }
        
          subgraph Sub-topology: 0
            test --> test-source
          end
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
    TopologyDiagram topologyDiagram = kafkaStreamsTopologyDiagramBuilder.buildFrom(topology);

    assertThat(topologyDiagram.toString())
      .isEqualToNormalizingWhitespace("""
        flowchart TD
        
          first@{ shape: das, label: 'first' }
          second@{ shape: das, label: 'second' }
          first-source@{ shape: docs, label: 'first-source' }
          second-source@{ shape: docs, label: 'second-source' }
        
          subgraph Sub-topology: 0
            first --> first-source
          end
        
          subgraph Sub-topology: 1
            second --> second-source
          end
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
    TopologyDiagram topologyDiagram = kafkaStreamsTopologyDiagramBuilder.buildFrom(topology);

    assertThat(topologyDiagram.toString())
      .isEqualToNormalizingWhitespace("""
        flowchart TD
        
          test@{ shape: das, label: 'test' }
          test-source@{ shape: docs, label: 'test-source' }
          test-processor@{ shape: subproc, label: 'test-processor' }
        
          subgraph Sub-topology: 0
            test --> test-source
            test-source --> test-processor
          end
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
    TopologyDiagram topologyDiagram = kafkaStreamsTopologyDiagramBuilder.buildFrom(topology);

    assertThat(topologyDiagram.toString())
      .isEqualToNormalizingWhitespace("""
        flowchart TD
        
          test@{ shape: das, label: 'test' }
          test-store@{ shape: database, label: 'test-store' }
          test-source@{ shape: docs, label: 'test-source' }
          test-processor@{ shape: subproc, label: 'test-processor' }
        
          subgraph Sub-topology: 0
            test --> test-source
            test-source --> test-processor
            test-processor --> test-store
          end
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
    TopologyDiagram topologyDiagram = kafkaStreamsTopologyDiagramBuilder.buildFrom(topology);

    assertThat(topologyDiagram.toString())
      .isEqualToNormalizingWhitespace("""
        flowchart TD
        
          test-in@{ shape: das, label: 'test-in' }
          test-out@{ shape: das, label: 'test-out' }
          test-source@{ shape: docs, label: 'test-source' }
          test-sink@{ shape: stadium, label: 'test-sink' }
        
          subgraph Sub-topology: 0
            test-in --> test-source
            test-source --> test-sink
            test-sink --> test-out
          end
        """);
  }

  @SpringBootConfiguration
  @EnableKafkaStreams
  @EnableAutoConfiguration
  public static class TestConfig {
  }

}
