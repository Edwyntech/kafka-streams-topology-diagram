package tech.edwyn.kafka.streams.topology;

import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyDescription;

public class KafkaStreamsTopologyDiagramBuilder {

  public String buildFor(Topology topology) {
    TopologyDiagramDescription topologyDiagramDescription = new TopologyDiagramDescription();

    TopologyDescription topologyDescription = topology.describe();
    topologyDiagramDescription.parse(topologyDescription);

    return topologyDiagramDescription.toString();
  }

}
