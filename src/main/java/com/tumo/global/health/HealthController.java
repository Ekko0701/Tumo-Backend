package com.tumo.global.health;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name="Health Check", description = "API 서버의 상태를 확인하기 위한 헬스 체크 엔드포인트")
public class HealthController {

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
