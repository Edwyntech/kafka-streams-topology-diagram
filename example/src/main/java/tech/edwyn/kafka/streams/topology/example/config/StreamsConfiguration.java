package tech.edwyn.kafka.streams.topology.example.config;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import tech.edwyn.kafka.streams.topology.example.ExampleProperties;
import tech.edwyn.kafka.streams.topology.example.processors.ToUpperCase;

@Configuration
@EnableKafkaStreams
public class StreamsConfiguration {

  private final ExampleProperties.Topics topics;
  private final ExampleProperties.Stores stores;

  public StreamsConfiguration(ExampleProperties exampleProperties) {
    this.topics = exampleProperties.topics();
    this.stores = exampleProperties.stores();
  }

  @Bean
  public StoreBuilder<KeyValueStore<String, String>> historyStoreBuilder() {
    return Stores.keyValueStoreBuilder(
      Stores.persistentKeyValueStore(stores.history()),
      Serdes.String(),
      Serdes.String());
  }

  @Bean
  public KStream<Integer, String> toUppercaseStream(StreamsBuilder builder, StoreBuilder<KeyValueStore<String, String>> historyStoreBuilder) {
    builder.addStateStore(historyStoreBuilder);

    KStream<Integer, String> stream = builder

      .stream(topics.input(),
        Consumed.with(Serdes.Integer(), Serdes.String())
                .withName("input-source"))

      .processValues(() -> new ToUpperCase(stores.history()),
        Named.as("ToUpperCase"),
        stores.history());

    stream.to(topics.output(),
      Produced.with(Serdes.Integer(), Serdes.String())
              .withName("output-sink"));

    return stream;
  }

}
