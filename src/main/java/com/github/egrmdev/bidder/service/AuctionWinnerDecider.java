package com.github.egrmdev.bidder.service;

import com.github.egrmdev.bidder.model.AuctionBid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Represent behaviour to determine winning bid, implements Strategy design pattern
 */
@FunctionalInterface
public interface AuctionWinnerDecider {
    Mono<AuctionBid> decideWinner(Flux<AuctionBid>auctionBids);
}



