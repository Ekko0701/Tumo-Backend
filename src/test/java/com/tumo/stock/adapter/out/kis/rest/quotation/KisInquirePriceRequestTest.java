package com.tumo.stock.adapter.out.kis.rest.quotation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tumo.stock.adapter.out.kis.rest.client.KisRestRequest;
import org.junit.jupiter.api.Test;

class KisInquirePriceRequestTest {

    @Test
    void toKisRestRequest() {
        KisInquirePriceRequest request = new KisInquirePriceRequest("005930");

        KisRestRequest<KisInquirePriceResponse> kisRestRequest = request.toKisRestRequest();

        assertThat(kisRestRequest.path()).isEqualTo("/uapi/domestic-stock/v1/quotations/inquire-price");
        assertThat(kisRestRequest.transactionId()).isEqualTo("FHKST01010100");
        assertThat(kisRestRequest.queryParameters())
                .containsEntry("FID_COND_MRKT_DIV_CODE", "J")
                .containsEntry("FID_INPUT_ISCD", "005930");
        assertThat(kisRestRequest.responseType()).isEqualTo(KisInquirePriceResponse.class);
    }

    @Test
    void throwsExceptionWhenStockCodeIsBlank() {
        assertThatThrownBy(() -> new KisInquirePriceRequest(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("현재가를 조회할 종목 코드는 필수입니다.");
    }
}
