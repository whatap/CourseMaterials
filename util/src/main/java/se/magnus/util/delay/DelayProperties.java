package se.magnus.util.delay;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.delay")
public class DelayProperties {

  private boolean enabled = true;

  @Min(0)
  private long minMs = 0L;

  @Min(0)
  private long maxMs = 1000L;

  private final Layers layers = new Layers();

  private final Mysql mysql = new Mysql();

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public long getMinMs() {
    return minMs;
  }

  public void setMinMs(long minMs) {
    this.minMs = minMs;
  }

  public long getMaxMs() {
    return maxMs;
  }

  public void setMaxMs(long maxMs) {
    this.maxMs = maxMs;
  }

  public Layers getLayers() {
    return layers;
  }

  public Mysql getMysql() {
    return mysql;
  }

  public static class Layers {
    private boolean controller = true;
    private boolean service = true;
    private boolean repository = true;

    public boolean isController() {
      return controller;
    }

    public void setController(boolean controller) {
      this.controller = controller;
    }

    public boolean isService() {
      return service;
    }

    public void setService(boolean service) {
      this.service = service;
    }

    public boolean isRepository() {
      return repository;
    }

    public void setRepository(boolean repository) {
      this.repository = repository;
    }
  }

  public static class Mysql {
    private boolean enabled = true;

    @DecimalMin("0.0")
    private double maxSeconds = 1.0;

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public double getMaxSeconds() {
      return maxSeconds;
    }

    public void setMaxSeconds(double maxSeconds) {
      this.maxSeconds = maxSeconds;
    }
  }
}
