A simple bidding system that exposes an API that accepts bid ids and then forwards each request to all the configurable bidders. Bidders respond with bids and desired content. Depending on the chosen winning bidder determination strategy, winning auction bid is selected, processed by substituting `$price$` in the content with an actual bid value, and returned to the API caller.

### Implementation details

- it's implemented as a Spring boot application
- by default it runs on port 8080 and accepts HTTP GET requests on http://localhost:8080/{id}
- bidder URLs are configured in `application.properties` under `bidders` property, which takes a list of URLs

Technologies used:
- Spring Boot
- Spring Web Function for exposing API and non-blocking reactive WebClient for calling bidder APIs
- Wiremock for integration testing

### How to build and run

To run the application:

`./gradlew bootRun`

To build the application

`./gradlew clean build`
