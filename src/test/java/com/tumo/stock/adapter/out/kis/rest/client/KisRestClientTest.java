package com.tumo.stock.adapter.out.kis.rest.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tumo.stock.adapter.out.kis.rest.auth.KisAccessTokenClient;
import com.tumo.stock.adapter.out.kis.config.KisProperties;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class KisRestClientTest {

    private final KisAccessTokenClient accessTokenClient = org.mockito.Mockito.mock(KisAccessTokenClient.class);

    @Test
    void get() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        KisRestClient client = new KisRestClient(
                restClientBuilder,
                accessTokenClient,
                properties("test-app-key", "test-app-secret")
        );
        given(accessTokenClient.getAccessToken()).willReturn("test-access-token");

        server.expect(requestTo("https://kis.example.com/uapi/domestic-stock/v1/quotations/inquire-price?FID_COND_MRKT_DIV_CODE=J&FID_INPUT_ISCD=005930"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-access-token"))
                .andExpect(header("appkey", "test-app-key"))
                .andExpect(header("appsecret", "test-app-secret"))
                .andExpect(header("tr_id", "FHKST01010100"))
                .andExpect(header("custtype", "P"))
                .andRespond(withSuccess("""
                        {
                          "rt_cd": "0",
                          "msg_cd": "MCA00000",
                          "msg1": "정상처리 되었습니다."
                        }
                        """, MediaType.APPLICATION_JSON));

        Map<String, String> queryParameters = new LinkedHashMap<>();
        queryParameters.put("FID_COND_MRKT_DIV_CODE", "J");
        queryParameters.put("FID_INPUT_ISCD", "005930");

        TestKisResponse response = client.get(KisRestRequest.get(
                "/uapi/domestic-stock/v1/quotations/inquire-price",
                "FHKST01010100",
                queryParameters,
                TestKisResponse.class
        ));

        assertThat(response.code()).isEqualTo("0");
        assertThat(response.message()).isEqualTo("정상처리 되었습니다.");
        server.verify();
    }

    @Test
    void getAllowsNullQueryParameters() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        KisRestClient client = new KisRestClient(
                restClientBuilder,
                accessTokenClient,
                properties("test-app-key", "test-app-secret")
        );
        given(accessTokenClient.getAccessToken()).willReturn("test-access-token");

        server.expect(requestTo("https://kis.example.com/uapi/test"))
                .andRespond(withSuccess("""
                        {
                          "rt_cd": "0",
                          "msg_cd": "MCA00000",
                          "msg1": "정상처리 되었습니다."
                        }
                        """, MediaType.APPLICATION_JSON));

        TestKisResponse response = client.get(KisRestRequest.get(
                "/uapi/test",
                "TEST_TR_ID",
                null,
                TestKisResponse.class
        ));

        assertThat(response.code()).isEqualTo("0");
        server.verify();
    }

    @Test
    void getThrowsExceptionWhenRequestIsNull() {
        KisRestClient client = new KisRestClient(
                RestClient.builder(),
                accessTokenClient,
                properties("test-app-key", "test-app-secret")
        );

        assertThatThrownBy(() -> client.get(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("KIS REST API 요청 값은 필수입니다.");
    }

    @Test
    void requestThrowsExceptionWhenPathIsBlank() {
        assertThatThrownBy(() -> KisRestRequest.get(" ", "TEST_TR_ID", Map.of(), TestKisResponse.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("KIS REST API path는 필수입니다.");
    }

    @Test
    void requestThrowsExceptionWhenTransactionIdIsBlank() {
        assertThatThrownBy(() -> KisRestRequest.get("/uapi/test", " ", Map.of(), TestKisResponse.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("KIS REST API transaction id는 필수입니다.");
    }

    @Test
    void requestThrowsExceptionWhenResponseTypeIsNull() {
        assertThatThrownBy(() -> KisRestRequest.get("/uapi/test", "TEST_TR_ID", Map.of(), null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("KIS REST API 응답 타입은 필수입니다.");
    }

    private KisProperties properties(String appKey, String appSecret) {
        return new KisProperties(
                true,
                appKey,
                appSecret,
                "https://kis.example.com",
                "ws://kis.example.com",
                "/tryitout",
                "P",
                "H0STCNT0",
                "H0STASP0"
        );
    }

    private record TestKisResponse(
            @JsonProperty("rt_cd")
            String code,

            @JsonProperty("msg1")
            String message
    ) {
    }
}
