package my.payservice.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import my.payservice.exception.CommonException;
import my.payservice.exception.ErrorCode;
import my.payservice.redisson.RedissonLockService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
public class RedisLockAspect {

    private final RedissonLockService lockService;

    @Around("@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        String rawKey = distributedLock.key();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();

        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }

        Expression expression = parser.parseExpression(rawKey);
        String key = expression.getValue(context, String.class);

        String lockKey = "product:" + key;

        boolean acquired = lockService.tryLock(lockKey, distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit());

        if (!acquired) {
            log.error("락 획득 실패: 키={}", lockKey);
            throw new CommonException(ErrorCode.LOCK_ACQUISITION_FAILED);
        }

        try {
            return joinPoint.proceed();
        } finally {
            lockService.unlock(lockKey);
        }
    }
}