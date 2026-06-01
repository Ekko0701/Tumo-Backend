package com.tumo.stock.adapter.out.kis.websocket.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import com.tumo.stock.adapter.out.kis.config.KisProperties;

class KisApprovalKeyClientTest {

    @Test
    void issueApprovalKey() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        KisApprovalKeyClient client = new KisApprovalKeyClient(restClientBuilder, properties("test-app-key", "test-app-secret"));

        server.expect(requestTo("https://kis.example.com/oauth2/Approval"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("""
                        {
                          "grant_type": "client_credentials",
                          "appkey": "test-app-key",
                          "secretkey": "test-app-secret"
                        }
                        """))
                .andRespond(withSuccess("""
                        {
                          "approval_key": "test-approval-key"
                        }
                        """, MediaType.APPLICATION_JSON));

        String approvalKey = client.issueApprovalKey();

        assertThat(approvalKey).isEqualTo("test-approval-key");
        server.verify();
    }

    @Test
    void issueApprovalKeyThrowsExceptionWhenAppKeyIsBlank() {
        KisApprovalKeyClient client = new KisApprovalKeyClient(RestClient.builder(), properties(" ", "test-app-secret"));

        assertThatThrownBy(client::issueApprovalKey)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("KIS app key가 설정되지 않았습니다.");
    }

    @Test
    void issueApprovalKeyThrowsExceptionWhenAppSecretIsBlank() {
        KisApprovalKeyClient client = new KisApprovalKeyClient(RestClient.builder(), properties("test-app-key", " "));

        assertThatThrownBy(client::issueApprovalKey)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("KIS app secret이 설정되지 않았습니다.");
    }

    @Test
    void issueApprovalKeyThrowsExceptionWhenApprovalKeyIsBlank() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        KisApprovalKeyClient client = new KisApprovalKeyClient(restClientBuilder, properties("test-app-key", "test-app-secret"));

        server.expect(requestTo("https://kis.example.com/oauth2/Approval"))
                .andRespond(withSuccess("""
                        {
                          "approval_key": ""
                        }
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(client::issueApprovalKey)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("KIS WebSocket approval key 발급 응답이 비어 있습니다.");
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
