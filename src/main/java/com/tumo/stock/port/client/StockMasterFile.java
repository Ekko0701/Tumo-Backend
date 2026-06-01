package com.tumo.stock.port.client;

import com.tumo.stock.domain.stock.Market;
import java.util.Objects;

/**
 * 외부 provider에서 받은 종목 마스터 원본 파일.
 *
 * @param market 종목 마스터 파일이 속한 시장
 * @param content 종목 마스터 파일 byte content
 */
public record StockMasterFile(
        Market market,
        byte[] content
) {

    public StockMasterFile {
        Objects.requireNonNull(market, "종목 마스터 시장 구분은 필수입니다.");
        Objects.requireNonNull(content, "종목 마스터 파일 내용은 필수입니다.");
        content = content.clone();
    }

    @Override
    public byte[] content() {
        return content.clone();
    }
}
