package com.example.redisaop.filter;

import com.example.redisaop.wrapper.ReadableBodyHttpServletRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import jakarta.servlet.Filter;

import java.io.IOException;

@Component
public class ReadableBodyHttpServletRequestFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        ReadableBodyHttpServletRequest readableBodyRequest = new ReadableBodyHttpServletRequest((HttpServletRequest) request);
        chain.doFilter(readableBodyRequest, response);
    }
}
