package com.github.egrmdev.bidder.util;

import com.github.egrmdev.bidder.model.AuctionBid;
import com.github.egrmdev.bidder.util.BidContentParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

class BidContentParserTest {
    @ParameterizedTest
    @DisplayName("Content in bid response is parsed correctly")
    @MethodSource("contentProvider")
    void testContentParsing(AuctionBid bid, String parsedContent) {
        assertThat(BidContentParser.parseContent(bid)).isEqualTo(parsedContent);
    }

    static Stream<Arguments> contentProvider() {
        return Stream.of(
          Arguments.arguments(new AuctionBid(1L, 10L, "no price"), "no price"),
          Arguments.arguments(new AuctionBid(1L, 10L, "$price$"), "10"),
          Arguments.arguments(new AuctionBid(1L, 10L, "a:$price$"), "a:10"),
          Arguments.arguments(new AuctionBid(1L, 10L, "$price$=b"), "10=b"),
          Arguments.arguments(new AuctionBid(1L, 10L, "a$ $price$"), "a$ 10"),
          Arguments.arguments(new AuctionBid(1L, 10L, "$price$ $price$"), "10 10"),
          Arguments.arguments(new AuctionBid(1L, 10L, "a:$price$-$price$, $price$"), "a:10-10, 10"),
          Arguments.arguments(new AuctionBid(1L, 10L, "a\n$price$"), "a\n10")
        );
    }
}
