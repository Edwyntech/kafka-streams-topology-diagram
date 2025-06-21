package tech.edwyn.kafka.streams.topology;

import org.apache.kafka.streams.Topology;

public class KafkaStreamsTopologyDiagramBuilder {
  public String buildFor(Topology topology) {
    return """
      graph TD
      A[Client] --> B[Load Balancer]
      B --> C[Server01]
      B --> D[Server02]
      """;
  }
}
