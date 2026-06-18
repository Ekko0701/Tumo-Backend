package com.tumo.stock.repository;

import com.tumo.stock.domain.candle.CandleInterval;
import com.tumo.stock.domain.candle.StockCandle;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockCandleRepository extends JpaRepository<StockCandle, Long> {

    /**
     * 종목·시간 단위·기준 시각 범위에 해당하는 캔들을 시각 오름차순으로 조회한다.
     */
    List<StockCandle> findByStockCodeAndIntervalAndCandleTimeBetweenOrderByCandleTimeAsc(
            String stockCode,
            CandleInterval interval,
            LocalDateTime start,
            LocalDateTime end
    );

    /**
     * 종목·시간 단위의 가장 최근 저장 캔들을 조회한다. 증분 동기화 기준점으로 사용한다.
     */
    Optional<StockCandle> findTopByStockCodeAndIntervalOrderByCandleTimeDesc(
            String stockCode,
            CandleInterval interval
    );

    /**
     * 종목·시간 단위의 가장 이른 저장 캔들을 조회한다. 조회 시작 구간이 이미 채워져 있는지 판단하는 데 사용한다.
     */
    Optional<StockCandle> findTopByStockCodeAndIntervalOrderByCandleTimeAsc(
            String stockCode,
            CandleInterval interval
    );

    /**
     * 종목·시간 단위·기준 시각의 캔들이 이미 저장돼 있는지 확인한다. upsert 시 중복 방지에 사용한다.
     */
    boolean existsByStockCodeAndIntervalAndCandleTime(
            String stockCode,
            CandleInterval interval,
            LocalDateTime candleTime
    );

    /**
     * 종목·시간 단위의 특정 시각 범위 캔들을 즉시 삭제한다. 최신 봉을 새 데이터로 교체(delete-then-insert)할 때 사용한다.
     *
     * <p>벌크 삭제로 즉시 실행되어 같은 트랜잭션의 후속 insert와 유니크 제약이 충돌하지 않는다.</p>
     */
    @Modifying
    @Query("delete from StockCandle c "
            + "where c.stockCode = :stockCode and c.interval = :interval "
            + "and c.candleTime between :start and :end")
    void deleteCandleWindow(
            @Param("stockCode") String stockCode,
            @Param("interval") CandleInterval interval,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
