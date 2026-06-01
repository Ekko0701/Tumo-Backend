package com.tumo.stock.adapter.out.kis.rest.quotation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tumo.stock.domain.price.StockPrice;
import org.junit.jupiter.api.Test;

class KisInquirePriceResponseTest {

    @Test
    void toStockPrice() {
        KisInquirePriceResponse response = new KisInquirePriceResponse(
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

        StockPrice stockPrice = response.toStockPrice("005930");

        assertThat(stockPrice.stockCode()).isEqualTo("005930");
        assertThat(stockPrice.currentPrice()).isEqualTo(75100L);
        assertThat(stockPrice.changePrice()).isEqualTo(100L);
        assertThat(stockPrice.changeRate()).isEqualByComparingTo("0.13");
        assertThat(stockPrice.tradeVolume()).isEqualTo(1234567L);
        assertThat(stockPrice.priceChangedAt()).isEqualTo("2026-05-29T09:30:00");
    }

    @Test
    void throwsExceptionWhenKisResponseIsFailure() {
        KisInquirePriceResponse response = new KisInquirePriceResponse(
                "1",
                "EGW00123",
                "오류가 발생했습니다.",
                null
        );

        assertThatThrownBy(() -> response.toStockPrice("005930"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("KIS 현재가 조회에 실패했습니다. code=1, message=오류가 발생했습니다.");
    }

    @Test
    void throwsExceptionWhenOutputIsNull() {
        KisInquirePriceResponse response = new KisInquirePriceResponse(
                "0",
                "MCA00000",
                "정상처리 되었습니다.",
                null
        );

        assertThatThrownBy(() -> response.toStockPrice("005930"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("KIS 현재가 조회 응답이 비어 있습니다.");
    }
}
