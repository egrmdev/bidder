package com.github.egrmdev.bidder.service;

import com.github.egrmdev.bidder.model.AuctionBid;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;

import java.util.stream.Stream;

class SecondHighestBidPlusOneTest {
    private final AuctionWinnerDecider decider = new SecondHighestBidPlusOne();

    @ParameterizedTest
    @MethodSource("auctionBids")
    @DisplayName("Auction should return bid response with the correct")
    void shouldReturnCorrectAuctionResult(Flux<AuctionBid> auctionBids, long winningBidValue, String winnerContent) {
        WinnerDeciderTestUtil.assertWinningBid(decider.decideWinner(auctionBids), winningBidValue, winnerContent);
    }

    static Stream<Arguments> auctionBids() {
        return Stream.of(
                Arguments.arguments(WinnerDeciderTestUtil.createAuctionBids(1L, 1, 1, 1), 1, "1"),
                Arguments.arguments(WinnerDeciderTestUtil.createAuctionBids(2L, 1, 10, 100), 11, "100"),
                Arguments.arguments(WinnerDeciderTestUtil.createAuctionBids(3L, 99, 100, 101), 101, "101"),
                Arguments.arguments(WinnerDeciderTestUtil.createAuctionBids(4L, Long.MIN_VALUE, 1, Long.MAX_VALUE), 2, Long.toString(Long.MAX_VALUE)),
                Arguments.arguments(WinnerDeciderTestUtil.createAuctionBids(5L, -1, 0, 1), 1, "1"),
                Arguments.arguments(WinnerDeciderTestUtil.createAuctionBids(6L, 1000, 900, 800), 901, "1000"),
                Arguments.arguments(WinnerDeciderTestUtil.createAuctionBids(6L, 100, 150, 50), 101, "150")
        );
    }
}
