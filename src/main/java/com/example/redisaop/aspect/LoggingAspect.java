package com.example.redisaop.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Objects;

@Component
@Aspect
public class LoggingAspect {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private ObjectMapper objectMapper;

    public LoggingAspect(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    //    @Around("execution(* com.example.redisaop.controller.*(*))")
    @Around("within(com.example.redisaop.controller.*)")
    public Object log(ProceedingJoinPoint pjp) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();

        String pathParams= getPathParams(request); // request 값 가져오기
        String queryParams= getQueryParams(request); // request 값 가져오기
        String requestBody = getRequestBody(request); // request 값 가져오기
        String requestUri = request.getRequestURI();
        if (!ObjectUtils.isEmpty(request.getQueryString())) {
            requestUri += '?' + request.getQueryString();
        }

        long started = System.currentTimeMillis();
//        LOGGER.info("REQUEST : {}({}): URI: {}, PATH_PARAM: {}, QUERY: {}, BODY: {}", pjp.getSignature().getDeclaringTypeName(), pjp.getSignature().getName(), request.getRequestURI() + '?' + request.getQueryString(), pathParams, queryParams, requestBody);
        LOGGER.info("REQUEST :: {} :: {} :: {}({}), PATH: {}, QUERY: {}, BODY: {}", request.getMethod(), requestUri, pjp.getSignature().getDeclaringTypeName(), pjp.getSignature().getName(), pathParams, queryParams, requestBody);
//        System.out.println("REQUEST :: {} :: {} :: {}({}), PATH: {}, QUERY: {}, BODY: {}", request.getMethod(), requestUri, pjp.getSignature().getDeclaringTypeName(), pjp.getSignature().getName(), pathParams, queryParams, requestBody);

        Object result = pjp.proceed(); // 4

        long ended = System.currentTimeMillis();
        LOGGER.info("RESPONSE :: {} :: {} :: {}({}), {} ({}ms)", request.getMethod(), requestUri, pjp.getSignature().getDeclaringTypeName(), pjp.getSignature().getName(), objectMapper.writeValueAsString(result), ended - started);
//        LOGGER.info("RESPONSE : {}({}): {} ({}ms)", pjp.getSignature().getDeclaringTypeName(), pjp.getSignature().getName(), objectMapper.writeValueAsString(result), ended - started);

        return result;
    }

    private String getQueryParams(HttpServletRequest request) {

        String params = "없음";

        try {
            Map<String, String[]> paramMap = request.getParameterMap();
            params = this.objectMapper.writeValueAsString(paramMap);
        } catch (JsonProcessingException e) {
            params = e.getMessage();
        }

        return params;
    }

    private String getPathParams(HttpServletRequest request) {

        String params = "없음";

        try {
            Map pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            params = this.objectMapper.writeValueAsString(pathVariables);
        } catch (JsonProcessingException e) {
            params = e.getMessage();
        }

        return params;
    }

    private String getRequestBody(HttpServletRequest request) {

        String body = "없음";

        try {
            StringBuilder stringBuilder = new StringBuilder();
//            BufferedReader br = null;
//            InputStream inputStream = request.getInputStream();
//            if (inputStream != null) {
//                br = new BufferedReader(new InputStreamReader(inputStream));
//                //더 읽을 라인이 없을때까지 계속
//                String line;
//                while ((line = br.readLine()) != null) {
//                    stringBuilder.append(line);
//                }
//
//                JsonNode jsonNode = objectMapper.readTree(stringBuilder.toString());
//                body = this.objectMapper.writeValueAsString(jsonNode);
////                body = stringBuilder.toString();
//            }
            BufferedReader br = request.getReader();
            String line;
            while ((line = br.readLine()) != null) {
                stringBuilder.append(line);
            }

            JsonNode jsonNode = objectMapper.readTree(stringBuilder.toString());
            body = this.objectMapper.writeValueAsString(jsonNode);
        } catch (IOException e) {
            body = e.getMessage();
        }

        return body;
    }

//    private String paramMapToString(Map<String, String[]> paramMap) {
//        return paramMap.entrySet().stream()
//                .map(entry -> String.format("%s -> (%s)", entry.getKey(), Joiner.on(",").join(entry.getValue())))
//                .collect(Collectors.joining(", "));
//    }
//
//    // Get request values
//    private String getRequestParams() throws JsonProcessingException {
//
//        String params = "없음";
//
//        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes(); // 3
//
//        if (requestAttributes != null) {
//            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
//
//            Map pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
//            LOGGER.info("Path: {}", this.objectMapper.writeValueAsString(pathVariables));
//
//
//            Map<String, String[]> paramMap = request.getParameterMap();
//            LOGGER.info("Params: {}", this.objectMapper.writeValueAsString(paramMap));
//            if (!paramMap.isEmpty()) {
//                params = " [" + paramMapToString(paramMap) + "]";
//            }
//        }
//
//        return params;
//
//    }
}
