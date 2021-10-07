package com.github.egrmdev.bidder.service;

import com.github.egrmdev.bidder.configuration.BiddersConfiguration;
import com.github.egrmdev.bidder.model.AuctionBid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import com.github.egrmdev.bidder.model.BidRequest;

@Slf4j
@Service
public class BiddingService {

    private final List<BidderClient> bidders;

    @Autowired
    public BiddingService(WebClient.Builder webClientBuilder, BiddersConfiguration configuration) {
        this.bidders = configuration.getBidders().stream()
                .map(host -> new BidderClient(webClientBuilder, host))
                .collect(Collectors.toList());
    }

    public Mono<AuctionBid> getHighestBid(BidRequest bidRequest) {
        return Flux.fromIterable(bidders)
                .parallel()
                .runOn(Schedulers.boundedElastic())
                .flatMap(client ->
                        client.getAuctionBid(bidRequest)
                                .timeout(Duration.ofMillis(500L)) // since auctions are supposed to be real-time, it cannot wait for slow bidders
                                .onErrorResume(e -> {
                                    if (e instanceof TimeoutException) {
                                        log.error("Bidder {} failed to respond within given time", client.getBidderUrl());
                                    } else {
                                        log.error("Bidder {} failed with {}", client.getBidderUrl(), e.getMessage());
                                    }
                                    return Mono.empty();
                                })
                )
                .filter(bidResponse -> bidResponse.getId() == bidRequest.getId()) // safe-guard against misbehaving bidders
                .filter(bidResponse -> bidResponse.getBid() > 0) // non-positive bids don't make sense
                .sequential()
                .doOnNext(r -> log.debug("{}", r))
                .reduce((bidResponse, bidResponse2) ->
                        // in case both bids are equal, there is no tiebreaker and the one winning is not deterministic
                        bidResponse.getBid() >= bidResponse2.getBid() ? bidResponse : bidResponse2
                );
    }
}
