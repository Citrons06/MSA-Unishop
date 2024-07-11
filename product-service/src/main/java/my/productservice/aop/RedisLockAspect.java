package my.productservice.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.productservice.exception.CommonException;
import my.productservice.exception.ErrorCode;
import my.productservice.util.RedisLock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
public class RedisLockAspect {

    private final RedisLock redisLock;

    @Around("@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();

        String rawKey = distributedLock.key();
        String key = parseKey(rawKey, method, args);

        log.debug("Attempting to acquire lock. Raw key: {}, Parsed key: {}, Method: {}, Args: {}",
                rawKey, key, method.getName(), Arrays.toString(args));

        String lockKey = key;
        String lockValue = UUID.randomUUID().toString();
        long timeout = distributedLock.timeout();

        boolean acquired = redisLock.lock(lockKey, lockValue, timeout, TimeUnit.MILLISECONDS);
        log.info("[RedisLock] 락 획득 시도: {}, key: {}", acquired, lockKey);

        if (!acquired) {
            throw new CommonException(ErrorCode.LOCK_ACQUISITION_FAILED);
        }

        try {
            log.info("[RedisLock] 메서드 실행 시작: {}, args: {}", method.getName(), Arrays.toString(args));
            return joinPoint.proceed();
        } finally {
            redisLock.unlock(lockKey, lockValue);
            log.info("[RedisLock] 락 해제: {}", lockKey);
        }
    }

    private String parseKey(String rawKey, Method method, Object[] args) {
        EvaluationContext context = new StandardEvaluationContext();
        ExpressionParser parser = new SpelExpressionParser();

        for (int i = 0; i < method.getParameters().length; i++) {
            context.setVariable(method.getParameters()[i].getName(), args[i]);
        }

        Expression expr = parser.parseExpression(rawKey);
        String result = expr.getValue(context, String.class);

        log.debug("Parsed key: {}, Raw key: {}, Method: {}, Args: {}", result, rawKey, method.getName(), Arrays.toString(args));

        return result != null ? result : "null";
    }
}