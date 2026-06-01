package com.tumo.stock.service.master;

import com.tumo.stock.domain.stock.Market;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * KIS 종목 마스터 고정 길이 파일을 Backend 종목 마스터 정보로 변환하는 parser.
 */
@Component
public class StockMasterFileParser {

    private static final Charset KIS_MASTER_CHARSET = Charset.forName("MS949");
    private static final String STOCK_GROUP_CODE = "ST";

    private static final int SHORT_CODE_LENGTH = 9;
    private static final int STANDARD_CODE_LENGTH = 12;
    private static final int NAME_START_INDEX = SHORT_CODE_LENGTH + STANDARD_CODE_LENGTH;

    /*
     * KIS 공식 샘플은 개행 문자가 포함된 row 기준으로 KOSPI 228, KOSDAQ 222를 사용한다.
     * BufferedReader.readLine()은 개행 문자를 제거하므로 Java 파서는 실제 데이터 영역 길이만 사용한다.
     */
    private static final StockMasterFormat KOSPI_FORMAT = new StockMasterFormat(
            227,
            new int[]{
                    2, 1, 4, 4, 4, 1, 1, 1, 1, 1,
                    1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                    1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                    1, 9
            },
            31
    );
    private static final StockMasterFormat KOSDAQ_FORMAT = new StockMasterFormat(
            221,
            new int[]{
                    2, 1, 4, 4, 4, 1, 1, 1, 1, 1,
                    1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                    1, 1, 1, 1, 1, 1, 9
            },
            26
    );

    /**
     * 종목 마스터 원본 파일에서 일반 주식 종목만 추출한다.
     *
     * @param stockMasterFile 종목 마스터 원본 파일
     * @return 파싱 결과
     */
    public StockMasterParseResult parse(StockMasterFile stockMasterFile) {
        StockMasterFormat format = resolveFormat(stockMasterFile.market());
        List<StockMasterInfo> stockMasters = new ArrayList<>();
        int skippedCount = 0;

        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        new ByteArrayInputStream(stockMasterFile.content()),
                        KIS_MASTER_CHARSET
                ))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                StockMasterInfo stockMaster = parseLine(stockMasterFile.market(), format, line);
                if (stockMaster == null) {
                    skippedCount++;
                    continue;
                }
                stockMasters.add(stockMaster);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("종목 마스터 파일 파싱에 실패했습니다.", exception);
        }

        return new StockMasterParseResult(stockMasters, skippedCount);
    }

    private StockMasterInfo parseLine(
            Market market,
            StockMasterFormat format,
            String line
    ) {
        if (line.length() <= NAME_START_INDEX + format.part2Length()) {
            return null;
        }

        int part2StartIndex = line.length() - format.part2Length();
        String stockCode = line.substring(0, SHORT_CODE_LENGTH).trim();
        String stockName = line.substring(NAME_START_INDEX, part2StartIndex).trim();
        String part2 = line.substring(part2StartIndex);
        String stockGroupCode = part2.substring(0, 2).trim();

        if (!STOCK_GROUP_CODE.equals(stockGroupCode) || stockCode.isBlank() || stockName.isBlank()) {
            return null;
        }

        return new StockMasterInfo(
                stockCode,
                stockName,
                market,
                parseBasePrice(part2, format)
        );
    }

    private Long parseBasePrice(String part2, StockMasterFormat format) {
        int startIndex = format.basePriceStartIndex();
        int endIndex = startIndex + format.basePriceLength();
        if (part2.length() < endIndex) {
            return 0L;
        }

        String value = part2.substring(startIndex, endIndex).trim();
        if (value.isBlank()) {
            return 0L;
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            return 0L;
        }
    }

    private StockMasterFormat resolveFormat(Market market) {
        return switch (market) {
            case KOSPI -> KOSPI_FORMAT;
            case KOSDAQ -> KOSDAQ_FORMAT;
        };
    }

    private record StockMasterFormat(
            int part2Length,
            int[] fieldWidths,
            int basePriceFieldIndex
    ) {

        int basePriceStartIndex() {
            int startIndex = 0;
            for (int index = 0; index < basePriceFieldIndex; index++) {
                startIndex += fieldWidths[index];
            }
            return startIndex;
        }

        int basePriceLength() {
            return fieldWidths[basePriceFieldIndex];
        }
    }
}
