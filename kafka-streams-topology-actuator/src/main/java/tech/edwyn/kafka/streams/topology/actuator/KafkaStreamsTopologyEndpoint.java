package tech.edwyn.kafka.streams.topology.actuator;

import org.apache.kafka.streams.Topology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.ui.ModelMap;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import tech.edwyn.kafka.streams.topology.KafkaStreamsTopologyDiagramBuilder;
import tech.edwyn.kafka.streams.topology.TopologyDiagram;

/**
 * Spring Boot Actuator endpoint for displaying Kafka Streams topology diagrams.
 * This endpoint renders a visual representation of the Kafka Streams topology
 * using Mermaid.js.
 */
@WebEndpoint(id = "kafkaStreamsTopology")
public class KafkaStreamsTopologyEndpoint {
  private static final Logger log = LoggerFactory.getLogger(KafkaStreamsTopologyEndpoint.class);

  private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;
  private final KafkaStreamsTopologyDiagramBuilder kafkaStreamsTopologyDiagramBuilder;
  private final KafkaStreamsTopologyProperties properties;

  /**
   * Creates a new KafkaStreamsTopologyEndpoint.
   *
   * @param streamsBuilderFactoryBean          the factory bean that provides access to the Kafka Streams topology
   * @param kafkaStreamsTopologyDiagramBuilder the builder for creating topology diagrams
   * @param properties                         configuration properties for the Kafka Streams Topology Actuator
   */
  public KafkaStreamsTopologyEndpoint(StreamsBuilderFactoryBean streamsBuilderFactoryBean,
                                      KafkaStreamsTopologyDiagramBuilder kafkaStreamsTopologyDiagramBuilder,
                                      KafkaStreamsTopologyProperties properties) {
    this.streamsBuilderFactoryBean = streamsBuilderFactoryBean;
    this.kafkaStreamsTopologyDiagramBuilder = kafkaStreamsTopologyDiagramBuilder;
    this.properties = properties;
    log.debug("Initialized KafkaStreamsTopologyEndpoint with properties: {}", properties);
  }

  /**
   * Renders the Kafka Streams topology diagram.
   *
   * @return a ModelAndView containing the topology diagram
   * @throws ResponseStatusException if the topology cannot be retrieved or rendered
   */
  @ReadOperation
  public ModelAndView topology() {
    log.debug("Rendering Kafka Streams topology diagram with properties: {}", properties);

    try {
      Topology topology = streamsBuilderFactoryBean.getTopology();

      if (topology == null) {
        log.error("Failed to retrieve Kafka Streams topology: topology is null");
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
          "Kafka Streams topology is not available");
      }

      log.debug("Retrieved Kafka Streams topology, building diagram");
      TopologyDiagram topologyDiagram = kafkaStreamsTopologyDiagramBuilder.buildFrom(topology);

      ModelMap model = new ModelMap()
        .addAttribute("topologyDiagram", topologyDiagram)
        .addAttribute("detailed", properties.isDetailed());

      String templateName = properties.getTemplate();
      log.debug("Returning topology diagram view using template: {}", templateName);
      return new ModelAndView(templateName, model);
    } catch (ResponseStatusException e) {
      // Re-throw ResponseStatusException as is
      throw e;
    } catch (Exception e) {
      log.error("Error rendering Kafka Streams topology diagram", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
        "Error rendering Kafka Streams topology diagram", e);
    }
  }
}
