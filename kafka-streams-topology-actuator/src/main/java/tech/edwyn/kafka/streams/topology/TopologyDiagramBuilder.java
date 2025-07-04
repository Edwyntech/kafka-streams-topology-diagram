package tech.edwyn.kafka.streams.topology;

import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyDescription;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.apache.kafka.streams.TopologyDescription.*;
import static tech.edwyn.kafka.streams.topology.TopologyDiagram.Node.NodeType;
import static tech.edwyn.kafka.streams.topology.TopologyDiagram.Node.NodeType.*;

/**
 * Builder class for creating a {@link TopologyDiagram} from a Kafka Streams {@link Topology}.
 * This class parses the topology description and creates a diagram representation that can be
 * rendered using Mermaid.js.
 */
public class TopologyDiagramBuilder {
  private final Map<String, TopologyDiagram.Node> nodesByName = new HashMap<>();
  private final Map<Integer, TopologyDiagram.Subgraph> subgraphsById = new HashMap<>();
  private TopologyDiagram topologyDiagram;

  /**
   * Builds a {@link TopologyDiagram} from the given Kafka Streams {@link Topology}.
   *
   * @param topology the Kafka Streams topology to build a diagram from
   * @return a diagram representation of the topology
   * @throws IllegalArgumentException if the topology is null
   */
  public TopologyDiagram buildFrom(@NotNull Topology topology) {
    topologyDiagram = new TopologyDiagram();
    TopologyDescription topologyDescription = topology.describe();
    parse(topologyDescription);

    return topologyDiagram;
  }

  /**
   * Parses the topology description to build the diagram.
   *
   * @param topologyDescription the topology description to parse
   */
  private void parse(@NotNull TopologyDescription topologyDescription) {
    // First pass: create all nodes
    topologyDescription.subtopologies()
                       .forEach(this::parseNodes);

    // Second pass: create all edges between nodes
    topologyDescription.subtopologies()
                       .forEach(this::parseEdges);
  }

  /**
   * Parses the nodes in a subtopology and creates the corresponding diagram nodes.
   *
   * @param subtopology the subtopology to parse
   */
  private void parseNodes(@NotNull Subtopology subtopology) {
    createSubgraph(subtopology.id());
    subtopology.nodes()
               .forEach(this::parseNodes);
  }

  /**
   * Parses a node in the topology and creates the corresponding diagram nodes.
   *
   * @param node the node to parse
   */
  private void parseNodes(@NotNull Node node) {
    if (node instanceof Source source) {
      createNode(source.name(), SOURCE);

      Optional.ofNullable(source.topicSet())
              .ifPresent(topics -> topics.forEach(t -> createNode(t, TOPIC)));

      Optional.ofNullable(source.topicPattern())
              .ifPresent(pattern -> createNode(pattern.pattern(), TOPIC));
    }

    if (node instanceof Processor processor) {
      createNode(processor.name(), PROCESSOR);

      if (!processor.stores()
                    .isEmpty()) {
        processor.stores()
                 .forEach(s -> createNode(s, STORE));
      }
    }

    if (node instanceof Sink sink) {
      createNode(sink.name(), SINK);

      Optional.ofNullable(sink.topic())
              .ifPresent(s -> createNode(s, TOPIC));
    }
  }

  /**
   * Parses the edges in a subtopology and creates the corresponding diagram edges.
   *
   * @param subtopology the subtopology to parse
   */
  private void parseEdges(@NotNull Subtopology subtopology) {
    subtopology.nodes()
               .forEach(node -> parseEdges(subtopology.id(), node));
  }

  /**
   * Parses the edges for a node in the topology and creates the corresponding diagram edges.
   *
   * @param id   the subtopology ID
   * @param node the node to parse
   */
  private void parseEdges(int id, Node node) {
    // Create edges for predecessors
    if (!node.predecessors()
             .isEmpty()) {
      node.predecessors()
          .forEach(predecessor -> createEdge(id, predecessor.name(), node.name()));
    }

    // Create edges for successors
    if (!node.successors()
             .isEmpty()) {
      node.successors()
          .forEach(successor -> createEdge(id, node.name(), successor.name()));
    }

    // Create edges based on node's type
    if (node instanceof Source source) {
      Optional.ofNullable(source.topicSet())
              .ifPresent(topics -> topics.forEach(t -> createEdge(id, t, source.name())));

      Optional.ofNullable(source.topicPattern())
              .ifPresent(pattern -> createEdge(id, pattern.pattern(), source.name()));
    }

    if (node instanceof Processor processor && !processor.stores()
                                                         .isEmpty()) {
      processor.stores()
               .forEach(name -> createEdge(id, processor.name(), name));
    }

    if (node instanceof Sink sink) {
      Optional.ofNullable(sink.topic())
              .ifPresent(name -> createEdge(id, sink.name(), name));
    }
  }

  /**
   * Creates a subgraph for the given subtopology ID.
   *
   * @param id the subtopology ID
   */
  private void createSubgraph(int id) {
    TopologyDiagram.Subgraph subgraph = subgraphsById.computeIfAbsent(id, TopologyDiagram.Subgraph::new);
    topologyDiagram.subgraphs()
                   .add(subgraph);
  }

  /**
   * Creates a node with the given name and shape.
   *
   * @param name  the node name
   * @param shape the node shape
   */
  private void createNode(String name, NodeType shape) {
    TopologyDiagram.Node node = nodesByName.computeIfAbsent(name, n -> new TopologyDiagram.Node(n, shape));
    topologyDiagram.nodes()
                   .add(node);
  }

  /**
   * Creates an edge between two nodes in the given subtopology.
   *
   * @param id   the subtopology ID
   * @param from the source node name
   * @param to   the target node name
   */
  private void createEdge(int id, String from, String to) {
    TopologyDiagram.Node source = nodesByName.get(from);
    TopologyDiagram.Node target = nodesByName.get(to);

    TopologyDiagram.Subgraph subgraph = subgraphsById.get(id);
    subgraph.edges()
            .add(new TopologyDiagram.Edge(source, target));
  }

}
