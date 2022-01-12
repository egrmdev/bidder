package com.github.egrmdev.bidder.service;

import com.github.egrmdev.bidder.model.AuctionBid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Returns auction bid with the highest bid value
 */
public class HighestBid implements AuctionWinnerDecider {
    @Override
    public Mono<AuctionBid> decideWinner(Flux<AuctionBid> auctionBids) {
        return auctionBids.reduce((bidResponse, bidResponse2) ->
                // in case both bids are equal, there is no tiebreaker and the one winning is not deterministic
                bidResponse.getBid() >= bidResponse2.getBid() ? bidResponse : bidResponse2
        );
    }
}

