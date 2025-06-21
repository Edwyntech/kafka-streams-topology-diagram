package tech.edwyn.kafka.streams.topology.actuator;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import tech.edwyn.kafka.streams.topology.KafkaStreamsTopologyDiagramBuilder;

@AutoConfiguration
@ConditionalOnBean(StreamsBuilderFactoryBean.class)
public class KafkaStreamsTopologyActuatorAutoConfiguration {

  @Bean
  public KafkaStreamsTopologyDiagramBuilder kafkaStreamsTopologyDiagramBuilder() {

    return new KafkaStreamsTopologyDiagramBuilder();
  }

  @Bean
  public KafkaStreamsTopologyEndpoint kafkaStreamsTopologyEndpoint(
    StreamsBuilderFactoryBean streamsBuilderFactoryBean,
    KafkaStreamsTopologyDiagramBuilder kafkaStreamsTopologyDiagramBuilder) {

    return new KafkaStreamsTopologyEndpoint(streamsBuilderFactoryBean, kafkaStreamsTopologyDiagramBuilder);
  }
}
