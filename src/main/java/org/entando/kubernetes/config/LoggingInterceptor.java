package org.entando.kubernetes.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
public class LoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        log.info("#### PRE");
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        log.info("#### POST");
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        log.info("#### AFTER");
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }

    //    static Logger LOGGER = LoggerFactory.getLogger(LoggingInterceptor.class);

//    @Override
//    public ClientHttpResponse intercept(HttpRequest req, byte[] reqBody, ClientHttpRequestExecution ex) throws IOException {
//
//        System.out.println("####### AOIAOAO");
//        log.debug("Request body: {}", new String(reqBody, StandardCharsets.UTF_8));
//        ClientHttpResponse response = ex.execute(req, reqBody);
//        InputStreamReader isr = new InputStreamReader(
//          response.getBody(), StandardCharsets.UTF_8);
//        String body = new BufferedReader(isr).lines()
//            .collect(Collectors.joining("\n"));
//        LOGGER.debug("Response body: {}", body);
//        return response;
//    }
}
