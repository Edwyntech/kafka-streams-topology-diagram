package tech.edwyn.kafka.streams.topoogy.actuator.integration;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.junit.UsePlaywright;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.apache.kafka.common.serialization.Serdes.Integer;
import static org.apache.kafka.common.serialization.Serdes.String;
import static org.apache.kafka.streams.kstream.Consumed.with;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@EmbeddedKafka(topics = "it")
@SpringBootTest(classes = KafkaStreamsTopologyEndpointIT.TestConfig.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("it")
@AutoConfigureMockMvc
@UsePlaywright
class KafkaStreamsTopologyEndpointIT {

  @LocalServerPort
  private int port;

  @Test
  void actuatorEndpointDisplaysTopologyDiagram(Page page) {
    page.navigate("http://localhost:%d/actuator/kafkaStreamsTopology".formatted(port));
    assertThat(page).hasTitle("Topology");

    Locator diagram = page.locator("css=.mermaid > svg");
    assertThat(diagram).isVisible();
  }

  @SpringBootConfiguration
  @EnableKafkaStreams
  @EnableAutoConfiguration
  public static class TestConfig {

    @Bean
    public KStream<Integer, String> testStream(StreamsBuilder builder) {
      return builder.stream("it",
        with(Integer(), String())
          .withName("it-source")
      );
    }
  }
}
