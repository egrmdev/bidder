package com.github.egrmdev.bidder.web;

import com.github.egrmdev.bidder.configuration.TestBiddersConfiguration;
import com.github.egrmdev.bidder.model.BidRequest;
import com.github.egrmdev.bidder.model.AuctionBid;
import com.github.egrmdev.bidder.service.BiddingService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestBiddersConfiguration.class)
class AuctionRouterTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private BiddingService biddingService;

    @Test
    @DisplayName("Auction should return parsed content of the highest bid")
    void testGet() {
        Mockito.when(biddingService.getHighestBid(ArgumentMatchers.any()))
                .thenReturn(Mono.just(new AuctionBid(1L, 700L, "winner bid $price$")));
        webTestClient.get()
                .uri("/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(response -> Assertions.assertThat(response).isEqualTo("winner bid 700"));
        ArgumentCaptor<BidRequest> bidRequestArgument = ArgumentCaptor.forClass(BidRequest.class);
        Mockito.verify(biddingService).getHighestBid(bidRequestArgument.capture());
        Assertions.assertThat(bidRequestArgument.getValue())
                .isEqualTo(new BidRequest(1L, Map.of()));
    }

    @Test
    @DisplayName("Auction should pass query params to bidders correctly")
    void testGetWithQueryParams() {
        Mockito.when(biddingService.getHighestBid(ArgumentMatchers.any()))
                .thenReturn(Mono.just(new AuctionBid(1L, 700L, "winner bid $price$")));
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/1")
                        .queryParam("key", "str")
                        .queryParam("size", 0L)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();
        ArgumentCaptor<BidRequest> bidRequestArgument = ArgumentCaptor.forClass(BidRequest.class);
        Mockito.verify(biddingService).getHighestBid(bidRequestArgument.capture());
        Assertions.assertThat(bidRequestArgument.getValue())
                .isEqualTo(new BidRequest(1L, Map.of("key", "str", "size", "0")));
    }

    @Test
    @DisplayName("Auction should return empty responce if there were no bids")
    void testGetWithEmptyResponse() {
        Mockito.when(biddingService.getHighestBid(ArgumentMatchers.any()))
                .thenReturn(Mono.empty());
        webTestClient.get()
                .uri("/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .isEmpty();
    }

    @Test
    @DisplayName("Auction should return 500 if there was an error which wasn't caught")
    void testGetWithError() {
        Mockito.when(biddingService.getHighestBid(ArgumentMatchers.any()))
                .thenThrow(IllegalStateException.class);
        webTestClient.get()
                .uri("/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError();
    }
}
