package tech.edwyn.kafka.streams.topology.actuator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static tech.edwyn.kafka.streams.topology.actuator.WithoutKafkaStreamsTest.TestConfig;

@SpringBootTest(classes = TestConfig.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class WithoutKafkaStreamsTest {

  @Autowired
  private MockMvcTester mvcTest;

  @Test
  void actuatorEndpointIsNotAccessibleWithoutKafkaStreams() {
    mvcTest.get()
           .uri("/actuator/kafkaStreamsTopology")
           .assertThat()
           .apply(print())
           .hasStatus(NOT_FOUND);
  }

  @SpringBootConfiguration
  @EnableAutoConfiguration
  public static class TestConfig {
  }
}
