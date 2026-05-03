package se.magnus.util.delay;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class DelayService {

  private static final Logger LOG = LoggerFactory.getLogger(DelayService.class);

  public enum Layer {
    CONTROLLER, SERVICE, REPOSITORY
  }

  private final DelayProperties props;

  public DelayService(DelayProperties props) {
    this.props = props;
  }

  public boolean layerEnabled(Layer layer) {
    if (!props.isEnabled()) {
      return false;
    }
    switch (layer) {
      case CONTROLLER:
        return props.getLayers().isController();
      case SERVICE:
        return props.getLayers().isService();
      case REPOSITORY:
        return props.getLayers().isRepository();
      default:
        return false;
    }
  }

  public long nextDelayMs() {
    long min = Math.max(0L, props.getMinMs());
    long max = Math.max(min, props.getMaxMs());
    if (max == min) {
      return min;
    }
    return ThreadLocalRandom.current().nextLong(min, max + 1L);
  }

  public void sleepRandom() {
    long ms = nextDelayMs();
    if (ms <= 0L) {
      return;
    }
    try {
      Thread.sleep(ms);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      LOG.warn("Sleep interrupted after {} ms", ms);
    }
  }

  public <T> Mono<T> applyDelay(Mono<T> source) {
    long ms = nextDelayMs();
    if (ms <= 0L) {
      return source;
    }
    return source.delayElement(Duration.ofMillis(ms));
  }

  public <T> Flux<T> applyDelay(Flux<T> source) {
    long ms = nextDelayMs();
    if (ms <= 0L) {
      return source;
    }
    return source.delaySequence(Duration.ofMillis(ms));
  }
}
