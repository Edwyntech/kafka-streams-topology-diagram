package tech.edwyn.kafka.streams.topology.actuator;

import org.apache.kafka.streams.Topology;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;
import tech.edwyn.kafka.streams.topology.KafkaStreamsTopologyDiagramBuilder;

import static java.util.Objects.requireNonNull;

@WebEndpoint(id = "kafkaStreamsTopology")
public class KafkaStreamsTopologyEndpoint {

  private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;
  private final KafkaStreamsTopologyDiagramBuilder kafkaStreamsTopologyDiagramBuilder;

  public KafkaStreamsTopologyEndpoint(StreamsBuilderFactoryBean streamsBuilderFactoryBean,
                                      KafkaStreamsTopologyDiagramBuilder kafkaStreamsTopologyDiagramBuilder) {
    this.streamsBuilderFactoryBean = streamsBuilderFactoryBean;
    this.kafkaStreamsTopologyDiagramBuilder = kafkaStreamsTopologyDiagramBuilder;
  }

  @ReadOperation
  public ModelAndView topology() {
    Topology topology = streamsBuilderFactoryBean.getTopology();
    String topologyDiagram = kafkaStreamsTopologyDiagramBuilder.buildFor(requireNonNull(topology));

    ModelMap model = new ModelMap()
      .addAttribute("topologyDiagram", topologyDiagram);

    return new ModelAndView("topology", model);
  }
}
