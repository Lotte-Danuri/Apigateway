package com.lotte.danuri.apigateway.filter;

import com.lotte.danuri.apigateway.filter.AuthorizationHeaderFilter.Config;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.server.ServerWebExchange;

@Component
@Slf4j
@CrossOrigin("*")
public class CustomTokenFilter extends AbstractGatewayFilterFactory {

    Environment env;

    public CustomTokenFilter(Environment env) {
        this.env = env;
    }

    @Override
    public GatewayFilter apply(Object config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            String authorizationHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
            String jwt = authorizationHeader.replace("Bearer", "");
            String memberId = getMemberId(jwt);

            ServerHttpRequest newRequest = request.mutate()
                .header("memberId", memberId).build();

            log.info("CustomTokenFilter newRequest = {}", newRequest.getHeaders());
            ServerWebExchange mutateServerWebExchange = exchange.mutate().request(newRequest).build();

            return chain.filter(mutateServerWebExchange);
        });
    }

    public String getMemberId(String token) {
        return Jwts.parser().setSigningKey(env.getProperty("token.secret"))
            .parseClaimsJws(token).getBody().getSubject();
    }


}
