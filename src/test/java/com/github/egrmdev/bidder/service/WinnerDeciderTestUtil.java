package com.github.egrmdev.bidder.service;

import com.github.egrmdev.bidder.model.AuctionBid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class WinnerDeciderTestUtil {
    public static void assertWinningBid(Mono<AuctionBid> actualWinnerBid, long expectedBidValue, String winnerContent) {
        StepVerifier.create(actualWinnerBid)
                .expectNextMatches(winningAuctionBid -> winningAuctionBid.getBid() == expectedBidValue
                && winningAuctionBid.getContent().equals(winnerContent))
                .verifyComplete();
    }

    public static Flux<AuctionBid> createAuctionBids(long bidId, long bid1, long bid2, long bid3) {
        return Flux.just(
                new AuctionBid(bidId, bid1, Long.toString(bid1)),
                new AuctionBid(bidId, bid2, Long.toString(bid2)),
                new AuctionBid(bidId, bid3, Long.toString(bid3))
        );
    }
}

