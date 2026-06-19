package com.tumo.global.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 시간 기준(Clock) 설정.
 *
 * <p>"오늘" 판정은 한국 증시(KST) 기준이어야 한다. 서버 OS/JVM 기본 타임존에 의존하면
 * UTC 등으로 배포됐을 때 분봉의 당일/과거 분기나 미래 일자 검증이 하루 어긋날 수 있어,
 * 항상 {@code Asia/Seoul} 기준의 {@link Clock}을 주입한다. 테스트에서는 고정 Clock으로 대체한다.</p>
 */
@Configuration
public class ClockConfig {

    /**
     * 한국 증시 기준 타임존.
     */
    public static final ZoneId KST = ZoneId.of("Asia/Seoul");

    /**
     * KST 기준 시스템 Clock bean을 생성한다. 시간 의존 로직은 {@code LocalDate.now()} 대신 이 Clock을 사용한다.
     *
     * @return Asia/Seoul 기준 시스템 Clock
     */
    @Bean
    public Clock clock() {
        return Clock.system(KST);
    }
}
