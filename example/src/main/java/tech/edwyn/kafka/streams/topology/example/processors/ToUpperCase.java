package tech.edwyn.kafka.streams.topology.example.processors;

import org.apache.kafka.streams.processor.api.ContextualFixedKeyProcessor;
import org.apache.kafka.streams.processor.api.FixedKeyRecord;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.Optional;

public class ToUpperCase extends ContextualFixedKeyProcessor<Integer, String, String> {

  private final String historyStoreName;

  public ToUpperCase(String historyStoreName) {
    this.historyStoreName = historyStoreName;
  }

  @Override
  public void process(FixedKeyRecord<Integer, String> record) {
    KeyValueStore<String, String> historyStore = context().getStateStore(historyStoreName);
    String updatedValue = Optional.ofNullable(historyStore.get(record.value()))
                                  .orElse(record.value())
                                  .toUpperCase();
    historyStore.putIfAbsent(record.value(), updatedValue);

    FixedKeyRecord<Integer, String> updatedRecord = record.withValue(updatedValue);
    context().forward(updatedRecord);
  }
}
