package com.milko.wallet_service.exceptionhandling;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String type;
    private String message;
    private LocalDateTime time;
    private Map<String, Object> extra;
}
