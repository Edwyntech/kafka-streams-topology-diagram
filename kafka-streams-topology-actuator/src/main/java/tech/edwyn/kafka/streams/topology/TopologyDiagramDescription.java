package tech.edwyn.kafka.streams.topology;

import org.apache.kafka.streams.TopologyDescription;
import org.apache.kafka.streams.processor.internals.InternalTopologyBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static java.util.Comparator.comparing;

public record TopologyDiagramDescription(
  Collection<TopologyDescription.Source> sources,
  Collection<TopologyDescription.Processor> processors,
  Collection<TopologyDescription.Sink> sinks,
  Collection<String> topics,
  Collection<String> stores,
  Map<String, String> namesToIds,
  SortedMap<TopologyDescription.Subtopology, Collection<Map.Entry<String, String>>> links
) {

  public TopologyDiagramDescription() {
    this(
      new ArrayList<>(),
      new ArrayList<>(),
      new ArrayList<>(),
      new ArrayList<>(),
      new ArrayList<>(),
      new HashMap<>(),
      new TreeMap<>(comparing(TopologyDescription.Subtopology::id)));
  }

  private void register(String name) {
    namesToIds().put(name, name.replaceAll("\\W", "-"));
  }

  public void parse(TopologyDescription topologyDescription) {
    topologyDescription.subtopologies()
                       .forEach(this::parse);
  }

  private void parse(TopologyDescription.Subtopology subtopology) {
    links().put(subtopology, new ArrayList<>());
    subtopology.nodes()
               .forEach(node -> parse(node, subtopology));
  }

  private void parse(TopologyDescription.Node node, TopologyDescription.Subtopology subtopology) {
    register(node.name());
    switch (node) {
      case InternalTopologyBuilder.Source source -> parse(source, subtopology);
      case InternalTopologyBuilder.Processor processor -> parse(processor, subtopology);
      case InternalTopologyBuilder.Sink<?, ?> sink -> parse(sink, subtopology);
      default -> throw new IllegalArgumentException("Unknown node: " + node);
    }
    node.predecessors()
        .forEach(predecessor -> links().get(subtopology)
                                       .add(Map.entry(predecessor.name(), node.name())));
    node.successors()
        .forEach(successor -> links().get(subtopology)
                                     .add(Map.entry(node.name(), successor.name())));
  }

  private void parse(InternalTopologyBuilder.Source source, TopologyDescription.Subtopology subtopology) {
    sources().add(source);
    Optional.ofNullable(source.topicSet())
            .ifPresent(topics -> topics.forEach(topic -> {
              register(topic);
              topics().add(topic);
              links().get(subtopology)
                     .add(Map.entry(topic, source.name()));
            }));
    Optional.ofNullable(source.topicPattern())
            .ifPresent(pattern -> {
              register(pattern.pattern());
              topics().add(pattern.pattern());
              links().get(subtopology)
                     .add(Map.entry(pattern.pattern(), source.name()));
            });
  }

  private void parse(InternalTopologyBuilder.Processor processor, TopologyDescription.Subtopology subtopology) {
    processors().add(processor);
    processor.stores()
             .forEach(store -> {
               register(store);
               stores().add(store);
               links().get(subtopology)
                      .add(Map.entry(processor.name(), store));
             });
  }

  private void parse(InternalTopologyBuilder.Sink<?, ?> sink, TopologyDescription.Subtopology subtopology) {
    sinks().add(sink);
    register(sink.topic());
    topics().add(sink.topic());
    links().get(subtopology)
           .add(Map.entry(sink.name(), sink.topic()));
  }

  private void appendNode(StringBuilder builder, String name, String shape) {
    builder.append("\t")
           .append(namesToIds().get(name))
           .append("@{ shape: ")
           .append(shape)
           .append(", label: '")
           .append(name)
           .append("' }")
           .append("\n");
  }

  @NotNull
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("flowchart TD\n");
    topics().forEach(topic -> appendNode(builder, topic, "das"));
    stores().forEach(store -> appendNode(builder, store, "database"));
    sources().forEach(source -> appendNode(builder, source.name(), "docs"));
    processors().forEach(processor -> appendNode(builder, processor.name(), "subproc"));
    sinks().forEach(sink -> appendNode(builder, sink.name(), "stadium"));
    links().forEach((subtopology, subtopologyLinks) -> {
      builder.append("\tsubgraph Sub-topology: ")
             .append(subtopology.id())
             .append("\n");
      subtopologyLinks.stream()
                      .distinct()
                      .forEach(link -> builder.append("\t\t")
                                              .append(link.getKey())
                                              .append(" --> ")
                                              .append(link.getValue())
                                              .append("\n"));
      builder.append("\tend\n");
    });

    return builder.toString();
  }

}
