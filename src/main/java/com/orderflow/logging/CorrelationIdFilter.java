package com.orderflow.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final Logger log =
            LoggerFactory.getLogger(CorrelationIdFilter.class);

    private static final String HEADER_NAME = "X-Correlation-ID";
    private static final String MDC_KEY = "correlationId";

    private static final Pattern VALID_ID =
            Pattern.compile("^[a-zA-Z0-9._-]{1,64}$");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String incomingId = request.getHeader(HEADER_NAME);

        String correlationId =
                incomingId != null && VALID_ID.matcher(incomingId).matches()
                        ? incomingId
                        : UUID.randomUUID().toString();

        String previousCorrelationId = MDC.get(MDC_KEY);

        MDC.put(MDC_KEY, correlationId);
        response.setHeader(HEADER_NAME, correlationId);

        long startTime = System.nanoTime();
        int status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        boolean requestFailed = false;

        try {
            filterChain.doFilter(request, response);
            status = response.getStatus();

        } catch (IOException | ServletException | RuntimeException ex) {
            requestFailed = true;
            throw ex;

        } finally {
            long durationMs =
                    (System.nanoTime() - startTime) / 1_000_000;

            if (requestFailed) {
                status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            }

            try {
                if (status >= 500) {
                    log.error(
                            "HTTP request completed method={} path={} status={} durationMs={}",
                            request.getMethod(),
                            request.getRequestURI(),
                            status,
                            durationMs
                    );
                } else if (status >= 400) {
                    log.warn(
                            "HTTP request completed method={} path={} status={} durationMs={}",
                            request.getMethod(),
                            request.getRequestURI(),
                            status,
                            durationMs
                    );
                } else {
                    log.info(
                            "HTTP request completed method={} path={} status={} durationMs={}",
                            request.getMethod(),
                            request.getRequestURI(),
                            status,
                            durationMs
                    );
                }
            } finally {
                if (previousCorrelationId == null) {
                    MDC.remove(MDC_KEY);
                } else {
                    MDC.put(MDC_KEY, previousCorrelationId);
                }
            }
        }
    }
}

/*filter banayenge jo har HTTP request ko unique ID dega,
response header mein ID return karega,
aur MDC ke through JSON logs mein ID include karega.*/