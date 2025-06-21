package tech.edwyn.kafka.streams.topoogy.actuator;

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.apache.kafka.common.serialization.Serdes.Integer;
import static org.apache.kafka.common.serialization.Serdes.String;
import static org.apache.kafka.streams.kstream.Consumed.with;
import static org.springframework.http.MediaType.TEXT_HTML;
import static tech.edwyn.kafka.streams.topoogy.actuator.KafkaStreamsTopologyActuatorTest.TestConfig;

@SpringBootTest(classes = TestConfig.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class KafkaStreamsTopologyActuatorTest {

  @Autowired
  private MockMvcTester mvcTest;

  @Test
  void actuatorEndpointIsAccessible() {
    mvcTest.get()
           .uri("/actuator/kafkaStreamsTopology")
           .assertThat()
           .hasStatusOk()
           .hasContentTypeCompatibleWith(TEXT_HTML)
           .hasViewName("topology");
  }

  @SpringBootConfiguration
  @EnableKafkaStreams
  @EnableAutoConfiguration
  public static class TestConfig {

    @Bean
    public KStream<Integer, String> testStream(StreamsBuilder builder) {
      return builder.stream("test",
        with(Integer(), String())
          .withName("test-source")
      );
    }
  }
}
