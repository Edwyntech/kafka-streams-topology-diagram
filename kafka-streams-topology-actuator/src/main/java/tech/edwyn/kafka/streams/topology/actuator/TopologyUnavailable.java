package tech.edwyn.kafka.streams.topology.actuator;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@ResponseStatus(value = SERVICE_UNAVAILABLE, reason = "No topology available")
public class TopologyUnavailable extends RuntimeException {
}
