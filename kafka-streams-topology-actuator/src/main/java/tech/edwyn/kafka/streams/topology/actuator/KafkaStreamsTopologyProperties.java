package tech.edwyn.kafka.streams.topology.actuator;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the Kafka Streams Topology Actuator.
 */
@ConfigurationProperties(prefix = "management.endpoint.kafka.streams.topology")
public class KafkaStreamsTopologyProperties {

  /**
   * Whether the Kafka Streams Topology endpoint is enabled.
   */
  private boolean enabled = true;

  /**
   * The template to use for rendering the topology diagram.
   */
  private String template = "topology";

  /**
   * Whether to include detailed information in the topology diagram.
   */
  private boolean detailed = false;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getTemplate() {
    return template;
  }

  public void setTemplate(String template) {
    this.template = template;
  }

  public boolean isDetailed() {
    return detailed;
  }

  public void setDetailed(boolean detailed) {
    this.detailed = detailed;
  }

  @Override
  public String toString() {
    return "KafkaStreamsTopologyProperties{" +
      "enabled=" + enabled +
      ", template='" + template + '\'' +
      ", detailed=" + detailed +
      '}';
  }
}
