package com.github.egrmdev.bidder.service;

import com.github.egrmdev.bidder.model.AuctionBid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;

/**
 * Bidder who placed the highest bid wins but the price paid is the second-highest bid plus one
 */
public class SecondHighestBidPlusOne implements AuctionWinnerDecider {
    @Override
    public Mono<AuctionBid> decideWinner(Flux<AuctionBid> auctionBids) {
        return auctionBids.sort(Comparator.comparing(AuctionBid::getBid).reversed())
                .take(2)
                .collectList()
                .flatMap(bids -> {
                    if (bids.size() >= 2) {
                        if (bids.get(0).getBid() == bids.get(1).getBid()) {
                            return Mono.just(bids.get(0));
                        }
                        return Mono.just(new AuctionBid(bids.get(1).getId(), bids.get(1).getBid() + 1, bids.get(0).getContent()));
                    } else if (bids.isEmpty()) {
                        return Mono.empty();
                    } else {
                        return Mono.just(new AuctionBid(bids.get(0).getId(), bids.get(0).getBid(), bids.get(0).getContent()));
                    }
                });
    }
}

