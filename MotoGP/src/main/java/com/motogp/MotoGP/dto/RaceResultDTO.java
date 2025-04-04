package com.motogp.MotoGP.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RaceResultDTO {
    private String riderName;
    private long averageLapTime;
    private int totalLaps;
    private int pitStopCount;
    private long averagePitWaitTime;
}
