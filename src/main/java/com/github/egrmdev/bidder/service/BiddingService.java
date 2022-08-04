package com.github.egrmdev.bidder.service;

import com.github.egrmdev.bidder.configuration.BiddersConfiguration;
import com.github.egrmdev.bidder.model.AuctionBid;
import com.github.egrmdev.bidder.model.BidRequest;
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

@Slf4j
@Service
public class BiddingService {
    public static final AuctionWinnerDecider HIGHEST_BID = new HighestBid();
    private final List<BidderClient> bidders;
    private AuctionWinnerDecider winnerDecider;

    @Autowired
    public BiddingService(WebClient.Builder webClientBuilder, BiddersConfiguration configuration) {
        log.info("Initializing bidders {}", configuration.getBidders());
        this.bidders = configuration.getBidders().stream()
                .map(host -> new BidderClient(webClientBuilder, host))
                .collect(Collectors.toList());
    }

    public Mono<AuctionBid> getHighestBid(BidRequest bidRequest) {
        if (winnerDecider == null) {
            log.warn("Winning bid decider wasn't set, highest bid decider is used");
            setWinnerDecider(HIGHEST_BID);
        }
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
                .transform(winnerDecider::decideWinner)
                .single();
    }

    public void setWinnerDecider(AuctionWinnerDecider winnerDecider) {
        this.winnerDecider = winnerDecider;
    }
}
