package tech.edwyn.kafka.streams.topology.example.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaAdmin;
import tech.edwyn.kafka.streams.topology.example.ExampleProperties;

@Configuration
@Profile("local")
public class LocalConfiguration {
  private final ExampleProperties.Topics topics;

  public LocalConfiguration(ExampleProperties exampleProperties) {
    this.topics = exampleProperties.topics();
  }

  @Bean
  @Profile("local")
  public KafkaAdmin.NewTopics createTopics() {
    return new KafkaAdmin.NewTopics(
      new NewTopic(topics.input(), 1, (short) 1),
      new NewTopic(topics.output(), 1, (short) 1)
    );
  }

}
