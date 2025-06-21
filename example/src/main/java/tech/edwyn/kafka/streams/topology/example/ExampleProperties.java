package tech.edwyn.kafka.streams.topology.example;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("example")
public record ExampleProperties(Topics topics, Stores stores) {
  public record Topics(String input, String output) {
  }

  public record Stores(String history) {
  }
}
