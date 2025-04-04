package com.motogp.MotoGP.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LapEventDTO {
    private String riderName;
    private int lapNumber;
    private long lapTimeMillis;
    private String timestamp;
}
