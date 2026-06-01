package com.tumo.stock.service.master;

import static org.assertj.core.api.Assertions.assertThat;

import com.tumo.stock.domain.stock.Market;
import java.nio.charset.Charset;
import org.junit.jupiter.api.Test;

class StockMasterFileParserTest {

    private static final Charset KIS_MASTER_CHARSET = Charset.forName("MS949");

    private final StockMasterFileParser parser = new StockMasterFileParser();

    @Test
    void parseKospiStockMasterFile() {
        String content = buildLine(
                "005930",
                "KR7005930003",
                "삼성전자",
                buildPart2(227, 41, "ST", "000080000")
        );
        StockMasterFile stockMasterFile = new StockMasterFile(Market.KOSPI, content.getBytes(KIS_MASTER_CHARSET));

        StockMasterParseResult result = parser.parse(stockMasterFile);

        assertThat(result.stockMasters()).hasSize(1);
        assertThat(result.skippedCount()).isZero();
        StockMasterInfo stockMaster = result.stockMasters().getFirst();
        assertThat(stockMaster.stockCode()).isEqualTo("005930");
        assertThat(stockMaster.stockName()).isEqualTo("삼성전자");
        assertThat(stockMaster.market()).isEqualTo(Market.KOSPI);
        assertThat(stockMaster.basePrice()).isEqualTo(80000L);
    }

    @Test
    void parseKosdaqStockMasterFile() {
        String content = buildLine(
                "247540",
                "KR7247540008",
                "에코프로비엠",
                buildPart2(221, 36, "ST", "000210000")
        );
        StockMasterFile stockMasterFile = new StockMasterFile(Market.KOSDAQ, content.getBytes(KIS_MASTER_CHARSET));

        StockMasterParseResult result = parser.parse(stockMasterFile);

        assertThat(result.stockMasters()).hasSize(1);
        assertThat(result.skippedCount()).isZero();
        StockMasterInfo stockMaster = result.stockMasters().getFirst();
        assertThat(stockMaster.stockCode()).isEqualTo("247540");
        assertThat(stockMaster.stockName()).isEqualTo("에코프로비엠");
        assertThat(stockMaster.market()).isEqualTo(Market.KOSDAQ);
        assertThat(stockMaster.basePrice()).isEqualTo(210000L);
    }

    @Test
    void skipsNonStockGroupRows() {
        String content = String.join("\n",
                buildLine("005930", "KR7005930003", "삼성전자", buildPart2(227, 41, "ST", "000080000")),
                buildLine("069500", "KR7069500007", "KODEX 200", buildPart2(227, 41, "EF", "000040000"))
        );
        StockMasterFile stockMasterFile = new StockMasterFile(Market.KOSPI, content.getBytes(KIS_MASTER_CHARSET));

        StockMasterParseResult result = parser.parse(stockMasterFile);

        assertThat(result.stockMasters()).hasSize(1);
        assertThat(result.stockMasters().getFirst().stockCode()).isEqualTo("005930");
        assertThat(result.skippedCount()).isEqualTo(1);
    }

    private String buildLine(
            String stockCode,
            String standardCode,
            String stockName,
            String part2
    ) {
        return "%-9s%-12s%-40s%s".formatted(stockCode, standardCode, stockName, part2);
    }

    private String buildPart2(
            int part2Length,
            int basePriceStartIndex,
            String stockGroupCode,
            String basePrice
    ) {
        StringBuilder builder = new StringBuilder(" ".repeat(part2Length));
        builder.replace(0, 2, stockGroupCode);
        builder.replace(basePriceStartIndex, basePriceStartIndex + basePrice.length(), basePrice);
        return builder.toString();
    }
}
