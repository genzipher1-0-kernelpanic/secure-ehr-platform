package com.example.gateway.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RequestLogFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestLogFilter.class);
    private static final String START_TIME_ATTR = "requestStartTime";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        exchange.getAttributes().put(START_TIME_ATTR, System.currentTimeMillis());

        return chain.filter(exchange)
                .doFinally(signalType -> {
                    Long startTime = exchange.getAttribute(START_TIME_ATTR);
                    long durationMs = startTime == null ? -1 : System.currentTimeMillis() - startTime;
                    HttpStatusCode status = exchange.getResponse().getStatusCode();
                    int statusValue = status == null ? 200 : status.value();
                    String method = exchange.getRequest().getMethod() == null
                            ? "UNKNOWN"
                            : exchange.getRequest().getMethod().name();
                    String path = exchange.getRequest().getURI().getRawPath();
                    String correlationId = exchange.getRequest().getHeaders()
                            .getFirst(CorrelationIdFilter.CORRELATION_ID_HEADER);

                    log.info("request method={} path={} status={} durationMs={} correlationId={}",
                            method, path, statusValue, durationMs, correlationId);
                });
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
