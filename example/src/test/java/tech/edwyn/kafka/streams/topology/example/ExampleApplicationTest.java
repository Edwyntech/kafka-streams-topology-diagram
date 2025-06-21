package tech.edwyn.kafka.streams.topology.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.UseMainMethod.ALWAYS;

@SpringBootTest(useMainMethod = ALWAYS)
class ExampleApplicationTest {

  @Autowired
  private ExampleApplication exampleApplication;

  @Test
  void contextLoads() {
    assertThat(exampleApplication).isNotNull();
  }
}
