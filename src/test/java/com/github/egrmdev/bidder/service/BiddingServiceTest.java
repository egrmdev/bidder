package com.github.egrmdev.bidder.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.egrmdev.bidder.configuration.TestBiddersConfiguration;
import com.github.egrmdev.bidder.model.BidRequest;
import com.github.egrmdev.bidder.model.AuctionBid;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.util.Map;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest
@AutoConfigureWebTestClient
@Import(TestBiddersConfiguration.class)
class BiddingServiceTest {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private BiddingService biddingService;

    @RegisterExtension
    static WireMockExtension BIDDER_1 = getConfiguredInstance(8081);

    @RegisterExtension
    static WireMockExtension BIDDER_2 = getConfiguredInstance(8082);

    @RegisterExtension
    static WireMockExtension BIDDER_3 = getConfiguredInstance(8083);

    @ParameterizedTest
    @MethodSource("bidProvider")
    @DisplayName("Auction should return bid response with the highest bid")
    void shouldReturnCorrectAuctionResult(long bidId, long bid1, long bid2, long bid3) {
        BIDDER_1.stubFor(getWireMockStubMapping(new AuctionBid(bidId, bid1, "")));
        BIDDER_2.stubFor(getWireMockStubMapping(new AuctionBid(bidId, bid2, "")));
        BIDDER_3.stubFor(getWireMockStubMapping(new AuctionBid(bidId, bid3, "")));

        assertAuctionResult(bidId, Math.max(Math.max(bid1, bid2), bid3));
    }

    @Test
    @DisplayName("Action should not wait for slow bidders to respond")
    void shouldNotWaitForSlowBidders() {
        final AuctionBid response1 = new AuctionBid(1L, 10, "");
        BIDDER_1.stubFor(getWireMockStubMapping(response1));
        final AuctionBid response2 = new AuctionBid(1L, 100, "");
        BIDDER_2.stubFor(getWireMockStubMapping(response2));
        final AuctionBid response3 = new AuctionBid(1L, 10_000, "");
        BIDDER_3.stubFor(getURLWithHeaderBuilder().willReturn(getResponseDefBuilder(response3)
                .withFixedDelay(600))
        );
        // auction will not wait for the third bidder even though it'd have the highest bid
        // because its delay is higher than auction waiting time for the response
        assertAuctionResult(response1.getId(), response2.getBid());
    }

    @Test
    @DisplayName("Auction should not fail in case bidders return errors")
    void shouldNotFailInCaseOneBidderReturnsError() {
        final AuctionBid response1 = new AuctionBid(1L, 11, "");
        BIDDER_1.stubFor(getWireMockStubMapping(response1));
        BIDDER_2.stubFor(getURLWithHeaderBuilder().willReturn(aResponse().withStatus(404)));
        BIDDER_3.stubFor(getURLWithHeaderBuilder().willReturn(aResponse().withStatus(422)));

        assertAuctionResult(response1.getId(), response1.getBid());
    }

    private static WireMockExtension getConfiguredInstance(int port) {
        return WireMockExtension.newInstance()
                .options(WireMockConfiguration.wireMockConfig().port(port))
                .failOnUnmatchedRequests(true)
                .build();
    }

    static Stream<Arguments> bidProvider() {
        return Stream.of(
                Arguments.arguments(1L, 1, 1, 1),
                Arguments.arguments(2L, 1, 10, 100),
                Arguments.arguments(3L, 99, 100, 101),
                Arguments.arguments(4L, Long.MIN_VALUE, 1, Long.MAX_VALUE),
                Arguments.arguments(5L, -1, 0, 1),
                Arguments.arguments(6L, 1000, 900, 800),
                Arguments.arguments(6L, 100, 150, 50)
        );
    }

    private static MappingBuilder getWireMockStubMapping(AuctionBid response) {
        return getURLWithHeaderBuilder()
                .willReturn(getResponseDefBuilder(response).withUniformRandomDelay(1, 10));
    }

    private static MappingBuilder getURLWithHeaderBuilder() {
        return post(urlPathMatching("/([0-9]*)"))
                .withHeader("Content-Type", equalTo("application/json"));
    }

    private static ResponseDefinitionBuilder getResponseDefBuilder(AuctionBid response) {
        return aResponse()
                .withHeader("Content-Type", "application/json")
                .withJsonBody(OBJECT_MAPPER.valueToTree(response));
    }

    private void assertAuctionResult(long bidId, long winningBid) {
        StepVerifier.create(biddingService.getHighestBid(new BidRequest(bidId, Map.of())))
                .expectNextMatches(bidResponse -> bidResponse.getBid() == winningBid
                        && bidResponse.getId() == bidId)
                .verifyComplete();
    }
}