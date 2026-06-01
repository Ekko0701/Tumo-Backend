package com.tumo.stock.adapter.out.kis.rest.quotation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.tumo.stock.adapter.out.kis.rest.client.KisRestClient;
import com.tumo.stock.adapter.out.kis.rest.client.KisRestRequest;
import com.tumo.stock.domain.price.StockPrice;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class KisStockPriceQueryClientTest {

    private final KisRestClient restClient = org.mockito.Mockito.mock(KisRestClient.class);
    private final KisStockPriceQueryClient client = new KisStockPriceQueryClient(restClient);

    @Test
    void findCurrentPrice() {
        given(restClient.get(any())).willReturn(kisResponse());

        Optional<StockPrice> currentPrice = client.findCurrentPrice("005930");

        assertThat(currentPrice).isPresent();
        assertThat(currentPrice.get().stockCode()).isEqualTo("005930");
        assertThat(currentPrice.get().currentPrice()).isEqualTo(75100L);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<KisRestRequest<KisInquirePriceResponse>> requestCaptor =
                ArgumentCaptor.forClass(KisRestRequest.class);
        verify(restClient).get(requestCaptor.capture());

        KisRestRequest<KisInquirePriceResponse> request = requestCaptor.getValue();
        assertThat(request.path()).isEqualTo("/uapi/domestic-stock/v1/quotations/inquire-price");
        assertThat(request.transactionId()).isEqualTo("FHKST01010100");
        assertThat(request.queryParameters())
                .containsEntry("FID_COND_MRKT_DIV_CODE", "J")
                .containsEntry("FID_INPUT_ISCD", "005930");
        assertThat(request.responseType()).isEqualTo(KisInquirePriceResponse.class);
    }

    private KisInquirePriceResponse kisResponse() {
        return new KisInquirePriceResponse(
                "0",
                "MCA00000",
                "정상처리 되었습니다.",
                new KisInquirePriceResponse.KisInquirePriceOutput(
                        "75100",
                        "100",
                        "0.13",
                        "1234567",
                        "20260529",
                        "093000"
                )
        );
    }
}
