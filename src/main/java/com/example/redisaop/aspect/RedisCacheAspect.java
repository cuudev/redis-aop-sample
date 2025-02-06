package com.example.redisaop.aspect;

import com.example.redisaop.annotation.RedisCacheable;
import com.example.redisaop.annotation.RedisCacheEvict;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RequiredArgsConstructor
@Component
@Aspect
public class RedisCacheAspect {
    private final Logger LOGGER = LoggerFactory.getLogger(RedisCacheAspect.class);

    private final HttpServletRequest request;
    private  final HttpServletResponse response;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Around("@annotation(redisCacheable)")
    public Object caching(ProceedingJoinPoint joinPoint, RedisCacheable redisCacheable) throws JsonProcessingException {

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Class returnType = methodSignature.getReturnType();

        Object result = null;

        LOGGER.info("RedisCacheable: {}, {}", redisCacheable.prefix(), redisCacheable.expire());
        LOGGER.info("RedisCacheable.args: {}", StringUtils.arrayToCommaDelimitedString(joinPoint.getArgs()));

        try {
            int expire = redisCacheable.expire();
            String key = generateKey(redisCacheable.prefix(), joinPoint);
            if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
                String value = (String)redisTemplate.opsForValue().get(key);
                setHeader();
                return objectMapper.readValue(value, returnType);
            }

            // 실제 동작하는 함수
            result = joinPoint.proceed();
            LOGGER.info("RedisCacheable: {}", result);
            if (expire < 0) {
                String value = objectMapper.writeValueAsString(result);
                redisTemplate.opsForValue().set(key, value);
            } else {
                redisTemplate.opsForValue().set(key, result, expire, TimeUnit.SECONDS);
            }
            setHeader();
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return result;
    }

    @Around("@annotation(redisCacheEvict)")
    public Object deleteCache(ProceedingJoinPoint joinPoint, RedisCacheEvict redisCacheEvict) {

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        String methodName = method.getName();

        Object result = null;
        try {
            result = joinPoint.proceed();
            String key = redisCacheEvict.prefix();
            boolean clearAll = redisCacheEvict.clearAll();
            if (clearAll) {
                Collection<String> keys = redisTemplate.keys(key + "*");
                if (keys != null) {
                    redisTemplate.delete(keys);
                }
            } else {
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return result;
    }

    private String generateKey(String prefix, ProceedingJoinPoint joinPoint) {
        String simpleName = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
//        String args = StringUtils.arrayToCommaDelimitedString(joinPoint.getArgs());
        String args = StringUtils.arrayToDelimitedString(joinPoint.getArgs(), "-");
//        return String.format("%s::%s.%s(%s)", prefix, simpleName, methodName, args);
        return String.format("%s_%s_%s_%s", prefix, simpleName.replace("Controller", ""), methodName, args);
    }

    private void setHeader() {
//        ServletRequestAttributes requestAttributes =
//            (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
//        HttpServletResponse response =requestAttributes.getResponse();
        LOGGER.info("setHeader: {}", response);
        if (response != null) {
            response.setHeader("X-Cache", "foobar");
        }
    }
}
