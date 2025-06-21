package tech.edwyn.kafka.streams.topology.actuator;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;

@AutoConfiguration
@ConditionalOnBean(StreamsBuilderFactoryBean.class)
public class KafkaStreamsTopologyActuatorAutoConfiguration {

  @Bean
  public KafkaStreamsTopologyEndpoint kafkaStreamsTopologyEndpoint() {
    return new KafkaStreamsTopologyEndpoint();
  }
}
