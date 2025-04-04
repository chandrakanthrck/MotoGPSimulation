package com.motogp.MotoGP.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LiveLapUpdate {
    private String rider;
    private int lap;
    private long lapTimeMillis;
}
