package com.tumo.stock.port.client;

import com.tumo.stock.service.master.StockMasterFile;
import java.util.List;

/**
 * 외부 provider에서 종목 마스터 파일을 가져오는 outbound port.
 */
public interface StockMasterFileClient {

    /**
     * KOSPI/KOSDAQ 종목 마스터 파일을 조회한다.
     *
     * @return 시장별 종목 마스터 파일 목록
     */
    List<StockMasterFile> downloadStockMasterFiles();
}
