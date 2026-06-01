package com.tumo.stock.adapter.out.kis.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.tumo.stock.adapter.out.kis.config.KisProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class KisAccessTokenClientTest {

    @Test
    void getAccessToken() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        KisAccessTokenClient client = new KisAccessTokenClient(
                restClientBuilder,
                properties("test-app-key", "test-app-secret")
        );

        server.expect(requestTo("https://kis.example.com/oauth2/tokenP"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {
                          "grant_type": "client_credentials",
                          "appkey": "test-app-key",
                          "appsecret": "test-app-secret"
                        }
                        """))
                .andRespond(withSuccess("""
                        {
                          "access_token": "test-access-token",
                          "access_token_token_expired": "2099-12-31 23:59:59",
                          "expires_in": 86400
                        }
                        """, MediaType.APPLICATION_JSON));

        String accessToken = client.getAccessToken();

        assertThat(accessToken).isEqualTo("test-access-token");
        server.verify();
    }

    @Test
    void getAccessTokenReusesCachedToken() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        KisAccessTokenClient client = new KisAccessTokenClient(
                restClientBuilder,
                properties("test-app-key", "test-app-secret")
        );

        server.expect(requestTo("https://kis.example.com/oauth2/tokenP"))
                .andRespond(withSuccess("""
                        {
                          "access_token": "test-access-token",
                          "access_token_token_expired": "2099-12-31 23:59:59",
                          "expires_in": 86400
                        }
                        """, MediaType.APPLICATION_JSON));

        String firstAccessToken = client.getAccessToken();
        String secondAccessToken = client.getAccessToken();

        assertThat(firstAccessToken).isEqualTo("test-access-token");
        assertThat(secondAccessToken).isEqualTo("test-access-token");
        server.verify();
    }

    @Test
    void getAccessTokenThrowsExceptionWhenAppKeyIsBlank() {
        KisAccessTokenClient client = new KisAccessTokenClient(RestClient.builder(), properties(" ", "test-app-secret"));

        assertThatThrownBy(client::getAccessToken)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("KIS app key가 설정되지 않았습니다.");
    }

    @Test
    void getAccessTokenThrowsExceptionWhenAppSecretIsBlank() {
        KisAccessTokenClient client = new KisAccessTokenClient(RestClient.builder(), properties("test-app-key", " "));

        assertThatThrownBy(client::getAccessToken)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("KIS app secret이 설정되지 않았습니다.");
    }

    @Test
    void getAccessTokenThrowsExceptionWhenAccessTokenIsBlank() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        KisAccessTokenClient client = new KisAccessTokenClient(
                restClientBuilder,
                properties("test-app-key", "test-app-secret")
        );

        server.expect(requestTo("https://kis.example.com/oauth2/tokenP"))
                .andRespond(withSuccess("""
                        {
                          "access_token": "",
                          "access_token_token_expired": "2099-12-31 23:59:59",
                          "expires_in": 86400
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(client::getAccessToken)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("KIS REST access token 발급 응답이 비어 있습니다.");
        server.verify();
    }

    @Test
    void getAccessTokenThrowsExceptionWhenExpiredAtIsMissing() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        KisAccessTokenClient client = new KisAccessTokenClient(
                restClientBuilder,
                properties("test-app-key", "test-app-secret")
        );

        server.expect(requestTo("https://kis.example.com/oauth2/tokenP"))
                .andRespond(withSuccess("""
                        {
                          "access_token": "test-access-token"
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(client::getAccessToken)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("KIS REST access token 만료 시간이 설정되지 않았습니다.");
        server.verify();
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
}
