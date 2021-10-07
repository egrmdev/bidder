package com.github.egrmdev.bidder.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration(proxyBeanMethods = false)
public class AuctionRouter {
    @Bean
    public RouterFunction<ServerResponse> route(AuctionHandler bidsHandler) {
        return RouterFunctions.route(
                RequestPredicates.GET("/{id}")
                        .and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
                bidsHandler::getWinningBidContent
        );
    }
}
