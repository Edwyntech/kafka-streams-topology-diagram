package tech.edwyn.kafka.streams.topology;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.joining;

public record TopologyDiagram(SortedSet<Node> nodes, SortedSet<Subgraph> subgraphs) {
  private static final int INDENT_SIZE = 2;

  public TopologyDiagram() {
    this(new TreeSet<>(Node.COMPARATOR), new TreeSet<>(Subgraph.COMPARATOR));
  }

  @Override
  public @NotNull String toString() {
    String nodesListing = nodes().stream()
                                 .map(Objects::toString)
                                 .collect(joining("\n"))
                                 .indent(INDENT_SIZE);
    String subgraphsListing = subgraphs().stream()
                                         .map(Objects::toString)
                                         .collect(joining("\n"))
                                         .indent(INDENT_SIZE);
    return """
      flowchart TD
      %s
      %s
      """
      .formatted(nodesListing, subgraphsListing);
  }

  public record Node(String name, NodeType shape) {
    public static final Comparator<Node> COMPARATOR = Comparator.comparing(Node::shape)
                                                                .thenComparing(Node::name);

    public String id() {
      return name().replaceAll("\\W", "-");
    }

    @Override
    public @NotNull String toString() {
      return "%s@{ shape: %s, label: '%s' }"
        .formatted(id(), shape(), name());
    }

    public enum NodeType {
      TOPIC("das"),
      STORE("database"),
      SOURCE("docs"),
      PROCESSOR("subproc"),
      SINK("stadium");

      private final String shape;

      NodeType(String shape) {
        this.shape = shape;
      }

      public String shape() {
        return shape;
      }

      @Override
      public @NotNull String toString() {
        return shape();
      }
    }
  }

  public record Subgraph(int id, SortedSet<Edge> edges) {
    public static final Comparator<Subgraph> COMPARATOR = comparingInt(Subgraph::id);

    public Subgraph(int id) {
      this(id, new TreeSet<>(Edge.COMPARATOR));
    }

    @Override
    public @NotNull String toString() {
      String edgesListing = edges().stream()
                                   .map(Objects::toString)
                                   .collect(joining("\n"))
                                   .indent(INDENT_SIZE);
      return """
        subgraph Sub-topology: %d
        %s
        end
        """.formatted(id(), edgesListing);
    }
  }

  public record Edge(Node source, Node target) {
    public static final Comparator<Edge> COMPARATOR = Comparator.comparing(Edge::source, Node.COMPARATOR)
                                                                .thenComparing(Edge::target, Node.COMPARATOR);

    @Override
    public @NotNull String toString() {
      return "%s --> %s"
        .formatted(source().id(), target().id());
    }
  }

}
