package tech.edwyn.kafka.streams.topology.actuator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import tech.edwyn.kafka.streams.topology.TopologyDiagramBuilder;

/**
 * Autoconfiguration for the Kafka Streams Topology Actuator.
 * This configuration is activated when a {@link StreamsBuilderFactoryBean} is present
 * in the application context and the actuator is enabled via configuration.
 */
@AutoConfiguration
@ConditionalOnBean(StreamsBuilderFactoryBean.class)
public class TopologyActuatorAutoConfiguration {
  private static final Logger log = LoggerFactory.getLogger(TopologyActuatorAutoConfiguration.class);

  /**
   * Creates a {@link TopologyDiagramBuilder} bean.
   *
   * @return a new KafkaStreamsTopologyDiagramBuilder
   */
  @Bean
  public TopologyDiagramBuilder kafkaStreamsTopologyDiagramBuilder() {
    log.debug("Creating KafkaStreamsTopologyDiagramBuilder bean");
    return new TopologyDiagramBuilder();
  }

  /**
   * Creates a {@link TopologyEndpoint} bean.
   *
   * @param streamsBuilderFactoryBean          the factory bean that provides access to the Kafka Streams topology
   * @param topologyDiagramBuilder the builder for creating topology diagrams
   * @return a new KafkaStreamsTopologyEndpoint
   */
  @Bean
  public TopologyEndpoint kafkaStreamsTopologyEndpoint(
    StreamsBuilderFactoryBean streamsBuilderFactoryBean,
    TopologyDiagramBuilder topologyDiagramBuilder) {

    return new TopologyEndpoint(streamsBuilderFactoryBean, topologyDiagramBuilder);
  }
}
