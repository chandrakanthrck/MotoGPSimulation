package com.motogp.MotoGP.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PitStopEventDTO {
    private String riderName;
    private String type; // Fuel or Tire
    private String status; // "ENTERED" or "EXITED"
    private String timestamp;
}
