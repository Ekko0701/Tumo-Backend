package com.tumo.stock.adapter.out.kis.rest.master;

import com.tumo.stock.domain.stock.Market;
import com.tumo.stock.port.client.StockMasterFileClient;
import com.tumo.stock.service.master.StockMasterFile;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.springframework.stereotype.Component;

/**
 * KIS 공식 종목 마스터 다운로드 파일을 조회하는 outbound adapter.
 */
@Component
public class KisStockMasterFileClient implements StockMasterFileClient {

    private static final String KOSPI_MASTER_URL = "https://new.real.download.dws.co.kr/common/master/kospi_code.mst.zip";
    private static final String KOSDAQ_MASTER_URL = "https://new.real.download.dws.co.kr/common/master/kosdaq_code.mst.zip";

    /**
     * KIS 공식 다운로드 URL에서 KOSPI/KOSDAQ 종목 마스터 파일을 내려받는다.
     *
     * @return 시장별 종목 마스터 파일 목록
     */
    @Override
    public List<StockMasterFile> downloadStockMasterFiles() {
        return List.of(
                download(Market.KOSPI, KOSPI_MASTER_URL),
                download(Market.KOSDAQ, KOSDAQ_MASTER_URL)
        );
    }

    private StockMasterFile download(Market market, String url) {
        try (
                InputStream inputStream = URI.create(url).toURL().openStream();
                ZipInputStream zipInputStream = new ZipInputStream(inputStream)
        ) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (!entry.isDirectory() && entry.getName().endsWith(".mst")) {
                    return new StockMasterFile(market, readAllBytes(zipInputStream));
                }
            }

            throw new IllegalStateException("KIS 종목 마스터 압축 파일에서 .mst 파일을 찾을 수 없습니다.");
        } catch (IOException exception) {
            throw new IllegalStateException("KIS 종목 마스터 파일 다운로드에 실패했습니다.", exception);
        }
    }

    private byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        inputStream.transferTo(outputStream);
        return outputStream.toByteArray();
    }
}