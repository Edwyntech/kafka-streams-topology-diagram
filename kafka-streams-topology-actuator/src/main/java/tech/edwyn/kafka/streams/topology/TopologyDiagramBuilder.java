package tech.edwyn.kafka.streams.topology;

import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyDescription;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private static final Logger log = LoggerFactory.getLogger(TopologyDiagramBuilder.class);

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
  public TopologyDiagram buildFrom(Topology topology) {
    if (topology == null) {
      throw new IllegalArgumentException("Topology cannot be null");
    }

    log.debug("Building topology diagram from topology: {}", topology);

    try {
      topologyDiagram = new TopologyDiagram();
      TopologyDescription topologyDescription = topology.describe();
      parse(topologyDescription);

      log.debug("Successfully built topology diagram with {} nodes and {} subgraphs",
        topologyDiagram.nodes()
                       .size(),
        topologyDiagram.subgraphs()
                       .size());

      return topologyDiagram;
    } catch (Exception e) {
      log.error("Failed to build topology diagram", e);
      throw new RuntimeException("Failed to build topology diagram", e);
    }
  }

  /**
   * Parses the topology description to build the diagram.
   *
   * @param topologyDescription the topology description to parse
   */
  private void parse(@NotNull TopologyDescription topologyDescription) {
    log.debug("Parsing topology description with {} subtopologies",
      topologyDescription.subtopologies()
                         .size());

    // First pass: create all nodes
    topologyDescription.subtopologies()
                       .forEach(this::parseNodes);

    // Second pass: create all edges between nodes
    topologyDescription.subtopologies()
                       .forEach(this::parseEdges);

    log.debug("Finished parsing topology description");
  }

  /**
   * Parses the nodes in a subtopology and creates the corresponding diagram nodes.
   *
   * @param subtopology the subtopology to parse
   */
  private void parseNodes(@NotNull Subtopology subtopology) {
    log.debug("Parsing nodes for subtopology {}", subtopology.id());
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
    log.debug("Parsing node: {}", node.name());

    switch (node) {
      case Source source -> {
        log.debug("Processing source node: {}", source.name());
        createNode(source.name(), SOURCE);

        Optional.ofNullable(source.topicSet())
                .ifPresent(topics -> {
                  log.debug("Source {} has {} topics", source.name(), topics.size());
                  topics.forEach(t -> createNode(t, TOPIC));
                });

        Optional.ofNullable(source.topicPattern())
                .ifPresent(pattern -> {
                  log.debug("Source {} has topic pattern: {}", source.name(), pattern.pattern());
                  createNode(pattern.pattern(), TOPIC);
                });
      }
      case Processor processor -> {
        log.debug("Processing processor node: {}", processor.name());
        createNode(processor.name(), PROCESSOR);

        if (!processor.stores()
                      .isEmpty()) {
          log.debug("Processor {} has {} stores", processor.name(), processor.stores()
                                                                             .size());
          processor.stores()
                   .forEach(s -> createNode(s, STORE));
        }
      }
      case Sink sink -> {
        log.debug("Processing sink node: {}", sink.name());
        createNode(sink.name(), SINK);

        Optional.ofNullable(sink.topic())
                .ifPresent(s -> {
                  log.debug("Sink {} writes to topic: {}", sink.name(), s);
                  createNode(s, TOPIC);
                });
      }
      default -> log.warn("Unknown node type: {}", node.getClass()
                                              .getName());
    }
  }

  /**
   * Parses the edges in a subtopology and creates the corresponding diagram edges.
   *
   * @param subtopology the subtopology to parse
   */
  private void parseEdges(@NotNull Subtopology subtopology) {
    log.debug("Parsing edges for subtopology {}", subtopology.id());
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
    log.debug("Parsing edges for node: {} in subtopology {}", node.name(), id);

    // Create edges for predecessors
    if (!node.predecessors()
             .isEmpty()) {
      log.debug("Node {} has {} predecessors", node.name(), node.predecessors()
                                                                .size());
      node.predecessors()
          .forEach(predecessor -> createEdge(id, predecessor.name(), node.name()));
    }

    // Create edges for successors
    if (!node.successors()
             .isEmpty()) {
      log.debug("Node {} has {} successors", node.name(), node.successors()
                                                              .size());
      node.successors()
          .forEach(successor -> createEdge(id, node.name(), successor.name()));
    }

    // Create edges based on node's type
    switch (node) {
      case Source source -> {
        log.debug("Processing edges for source node: {}", source.name());

        Optional.ofNullable(source.topicSet())
                .ifPresent(topics -> {
                  log.debug("Creating edges from {} topics to source {}", topics.size(), source.name());
                  topics.forEach(t -> createEdge(id, t, source.name()));
                });

        Optional.ofNullable(source.topicPattern())
                .ifPresent(pattern -> {
                  log.debug("Creating edge from topic pattern {} to source {}", pattern.pattern(), source.name());
                  createEdge(id, pattern.pattern(), source.name());
                });
      }
      case Processor processor -> {
        if (!processor.stores()
                      .isEmpty()) {
          log.debug("Creating edges from processor {} to {} stores", processor.name(), processor.stores()
                                                                                                .size());
          processor.stores()
                   .forEach(name -> createEdge(id, processor.name(), name));
        }
      }
      case Sink sink -> Optional.ofNullable(sink.topic())
                                .ifPresent(name -> {
                                  log.debug("Creating edge from sink {} to topic {}", sink.name(), name);
                                  createEdge(id, sink.name(), name);
                                });
      default -> log.warn("Unknown node type for edge creation: {}", node.getClass()
                                                                .getName());
    }
  }

  /**
   * Creates a subgraph for the given subtopology ID.
   *
   * @param id the subtopology ID
   */
  private void createSubgraph(int id) {
    log.debug("Creating subgraph for subtopology {}", id);
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
    if (name == null || name.isBlank()) {
      log.warn("Attempted to create node with null or blank name, ignoring");
      return;
    }

    log.debug("Creating node '{}' with shape {}", name, shape);
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
    if (from == null || from.isBlank() || to == null || to.isBlank()) {
      log.warn("Attempted to create edge with null or blank node names: from='{}', to='{}'", from, to);
      return;
    }

    TopologyDiagram.Node source = nodesByName.get(from);
    TopologyDiagram.Node target = nodesByName.get(to);

    if (source == null || target == null) {
      log.warn("Cannot create edge from '{}' to '{}': one or both nodes do not exist", from, to);
      return;
    }

    log.debug("Creating edge from '{}' to '{}' in subtopology {}", from, to, id);
    TopologyDiagram.Subgraph subgraph = subgraphsById.get(id);
    if (subgraph == null) {
      log.warn("Cannot create edge: subgraph with ID {} does not exist", id);
      return;
    }

    subgraph.edges()
            .add(new TopologyDiagram.Edge(source, target));
  }

}
