package tech.edwyn.kafka.streams.topology.actuator;

import org.apache.kafka.streams.Topology;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.ui.ModelMap;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import tech.edwyn.kafka.streams.topology.TopologyDiagram;
import tech.edwyn.kafka.streams.topology.TopologyDiagramBuilder;

import java.util.Optional;

/**
 * Spring Boot Actuator endpoint for displaying Kafka Streams topology diagrams.
 * This endpoint renders a visual representation of the Kafka Streams topology
 * using Mermaid.js.
 */
@WebEndpoint(id = "kafkaStreamsTopology")
public class TopologyEndpoint {
  private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;
  private final TopologyDiagramBuilder topologyDiagramBuilder;

  /**
   * Creates a new KafkaStreamsTopologyEndpoint.
   *
   * @param streamsBuilderFactoryBean the factory bean that provides access to the Kafka Streams topology
   * @param topologyDiagramBuilder    the builder for creating topology diagrams
   */
  public TopologyEndpoint(StreamsBuilderFactoryBean streamsBuilderFactoryBean,
                          TopologyDiagramBuilder topologyDiagramBuilder) {
    this.streamsBuilderFactoryBean = streamsBuilderFactoryBean;
    this.topologyDiagramBuilder = topologyDiagramBuilder;
  }

  /**
   * Renders the Kafka Streams topology diagram.
   *
   * @return a ModelAndView containing the topology diagram
   * @throws ResponseStatusException if the topology cannot be retrieved or rendered
   */
  @ReadOperation
  public ModelAndView topology() {
    Topology topology = Optional.ofNullable(streamsBuilderFactoryBean.getTopology())
                                .orElseThrow(TopologyUnavailable::new);
    TopologyDiagram topologyDiagram = topologyDiagramBuilder.buildFrom(topology);

    ModelMap model = new ModelMap()
      .addAttribute("topologyDiagram", topologyDiagram);

    return new ModelAndView("topology", model);
  }
}
