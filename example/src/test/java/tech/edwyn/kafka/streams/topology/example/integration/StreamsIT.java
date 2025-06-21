package tech.edwyn.kafka.streams.topology.example.integration;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import tech.edwyn.kafka.streams.topology.example.ExampleProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.kafka.test.assertj.KafkaConditions.value;

@EmbeddedKafka(
  topics = {
    "${example.topics.input}",
    "${example.topics.output}"
  }
)
@SpringBootTest
@ActiveProfiles("it")
public class StreamsIT {

  @Autowired
  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  private EmbeddedKafkaBroker embeddedKafkaBroker;
  @Autowired
  private ExampleProperties.Topics topics;
  @Autowired
  private KafkaTemplate<Integer, String> kafkaTemplate;
  @Autowired
  private ConsumerFactory<Integer, String> consumerFactory;

  @Test
  void shouldStreamInput() {
    kafkaTemplate.send(topics.input(), "it");

    Consumer<Integer, String> consumer = consumerFactory.createConsumer();
    embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, topics.output());
    ConsumerRecord<Integer, String> record = KafkaTestUtils.getSingleRecord(consumer, topics.output());

    assertThat(record).has(value("IT"));
  }

  @TestConfiguration
  public static class TestConfig {

    @Bean
    public ExampleProperties.Topics topics(ExampleProperties exampleProperties) {
      return exampleProperties.topics();
    }
  }
}
