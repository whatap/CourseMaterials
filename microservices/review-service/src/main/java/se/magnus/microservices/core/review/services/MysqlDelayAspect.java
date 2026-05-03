package se.magnus.microservices.core.review.services;

import java.util.concurrent.ThreadLocalRandom;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import se.magnus.util.delay.DelayProperties;

@Aspect
@Component
public class MysqlDelayAspect {

  private static final Logger LOG = LoggerFactory.getLogger(MysqlDelayAspect.class);

  private final JdbcTemplate jdbcTemplate;

  private final DelayProperties props;

  public MysqlDelayAspect(JdbcTemplate jdbcTemplate, DelayProperties props) {
    this.jdbcTemplate = jdbcTemplate;
    this.props = props;
  }

  @Around("execution(* se.magnus.microservices.core.review.persistence.ReviewRepository.*(..))")
  public Object aroundReviewRepository(ProceedingJoinPoint pjp) throws Throwable {
    if (props.isEnabled() && props.getMysql().isEnabled()) {
      double maxSec = Math.max(0.0, props.getMysql().getMaxSeconds());
      double sec = maxSec == 0.0 ? 0.0 : ThreadLocalRandom.current().nextDouble(0.0, maxSec);
      try {
        jdbcTemplate.queryForObject("SELECT SLEEP(?)", Integer.class, sec);
      } catch (DataAccessException ex) {
        LOG.warn("MySQL SLEEP({}) failed, continuing: {}", sec, ex.getMessage());
      }
    }
    return pjp.proceed();
  }
}
