package com.github.egrmdev.bidder.service;

import com.github.egrmdev.bidder.model.AuctionBid;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;

import java.util.stream.Stream;

class HighestBidTest {
    private final AuctionWinnerDecider decider = new HighestBid();

    @ParameterizedTest
    @MethodSource("auctionBids")
    @DisplayName("Auction should return bid response with the correct")
    void shouldReturnCorrectAuctionResult(Flux<AuctionBid> auctionBids, long winningBidValue) {
        WinnerDeciderTestUtil.assertWinningBid(decider.decideWinner(auctionBids), winningBidValue, Long.toString(winningBidValue));
    }

    static Stream<Arguments> auctionBids() {
        return Stream.of(
                Arguments.arguments(WinnerDeciderTestUtil.createAuctionBids(1L, 1, 1, 1), 1),
                Arguments.arguments(WinnerDeciderTestUtil.createAuctionBids(2L, 1, 10, 100), 100),
                Arguments.arguments(WinnerDeciderTestUtil.createAuctionBids(3L, 99, 100, 101), 101),
                Arguments.arguments(WinnerDeciderTestUtil.createAuctionBids(4L, Long.MIN_VALUE, 1, Long.MAX_VALUE), Long.MAX_VALUE),
                Arguments.arguments(WinnerDeciderTestUtil.createAuctionBids(5L, -1, 0, 1), 1),
                Arguments.arguments(WinnerDeciderTestUtil.createAuctionBids(6L, 1000, 900, 800), 1000),
                Arguments.arguments(WinnerDeciderTestUtil.createAuctionBids(6L, 100, 150, 50), 150)
        );
    }
}
