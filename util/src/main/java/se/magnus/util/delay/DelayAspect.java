package se.magnus.util.delay;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.magnus.util.delay.DelayService.Layer;

@Aspect
@Component
public class DelayAspect {

  private final DelayService delayService;

  public DelayAspect(DelayService delayService) {
    this.delayService = delayService;
  }

  @Around("@within(org.springframework.web.bind.annotation.RestController)")
  public Object aroundController(ProceedingJoinPoint pjp) throws Throwable {
    return dispatch(pjp, Layer.CONTROLLER);
  }

  @Around("@within(org.springframework.stereotype.Service)")
  public Object aroundService(ProceedingJoinPoint pjp) throws Throwable {
    return dispatch(pjp, Layer.SERVICE);
  }

  @Around("execution(* org.springframework.data.repository.Repository+.*(..))")
  public Object aroundRepository(ProceedingJoinPoint pjp) throws Throwable {
    return dispatch(pjp, Layer.REPOSITORY);
  }

  private Object dispatch(ProceedingJoinPoint pjp, Layer layer) throws Throwable {
    if (!delayService.layerEnabled(layer)) {
      return pjp.proceed();
    }
    Class<?> returnType = ((MethodSignature) pjp.getSignature()).getReturnType();
    if (Mono.class.isAssignableFrom(returnType)) {
      Object result = pjp.proceed();
      if (result == null) {
        return null;
      }
      @SuppressWarnings("unchecked")
      Mono<Object> mono = (Mono<Object>) result;
      return delayService.applyDelay(mono);
    }
    if (Flux.class.isAssignableFrom(returnType)) {
      Object result = pjp.proceed();
      if (result == null) {
        return null;
      }
      @SuppressWarnings("unchecked")
      Flux<Object> flux = (Flux<Object>) result;
      return delayService.applyDelay(flux);
    }
    delayService.sleepRandom();
    return pjp.proceed();
  }
}
