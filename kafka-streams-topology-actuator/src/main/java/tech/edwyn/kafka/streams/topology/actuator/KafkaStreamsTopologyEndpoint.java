package tech.edwyn.kafka.streams.topology.actuator;

import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;
import org.springframework.web.servlet.ModelAndView;

@WebEndpoint(id = "kafkaStreamsTopology")
public class KafkaStreamsTopologyEndpoint {

  @ReadOperation
  public ModelAndView topology() {
    return new ModelAndView("topology");
  }
}
