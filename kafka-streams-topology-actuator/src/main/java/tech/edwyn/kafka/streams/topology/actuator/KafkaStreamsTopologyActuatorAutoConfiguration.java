package tech.edwyn.kafka.streams.topology.actuator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import tech.edwyn.kafka.streams.topology.KafkaStreamsTopologyDiagramBuilder;

/**
 * Auto-configuration for the Kafka Streams Topology Actuator.
 * This configuration is activated when a {@link StreamsBuilderFactoryBean} is present
 * in the application context and the actuator is enabled via configuration.
 */
@AutoConfiguration
@ConditionalOnBean(StreamsBuilderFactoryBean.class)
@ConditionalOnProperty(
  prefix = "management.endpoint.kafka.streams.topology",
  name = "enabled",
  havingValue = "true",
  matchIfMissing = true
)
@EnableConfigurationProperties(KafkaStreamsTopologyProperties.class)
public class KafkaStreamsTopologyActuatorAutoConfiguration {
  private static final Logger log = LoggerFactory.getLogger(KafkaStreamsTopologyActuatorAutoConfiguration.class);

  /**
   * Creates a {@link KafkaStreamsTopologyDiagramBuilder} bean.
   *
   * @return a new KafkaStreamsTopologyDiagramBuilder
   */
  @Bean
  public KafkaStreamsTopologyDiagramBuilder kafkaStreamsTopologyDiagramBuilder() {
    log.debug("Creating KafkaStreamsTopologyDiagramBuilder bean");
    return new KafkaStreamsTopologyDiagramBuilder();
  }

  /**
   * Creates a {@link KafkaStreamsTopologyEndpoint} bean.
   *
   * @param streamsBuilderFactoryBean          the factory bean that provides access to the Kafka Streams topology
   * @param kafkaStreamsTopologyDiagramBuilder the builder for creating topology diagrams
   * @param properties                         configuration properties for the Kafka Streams Topology Actuator
   * @return a new KafkaStreamsTopologyEndpoint
   */
  @Bean
  public KafkaStreamsTopologyEndpoint kafkaStreamsTopologyEndpoint(
    StreamsBuilderFactoryBean streamsBuilderFactoryBean,
    KafkaStreamsTopologyDiagramBuilder kafkaStreamsTopologyDiagramBuilder,
    KafkaStreamsTopologyProperties properties) {

    log.debug("Creating KafkaStreamsTopologyEndpoint bean with properties: {}", properties);
    return new KafkaStreamsTopologyEndpoint(streamsBuilderFactoryBean, kafkaStreamsTopologyDiagramBuilder, properties);
  }
}
