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

    public String getFormattedLapTime() {
        return formatMillis(averageLapTime);
    }

    public String getFormattedPitWaitTime() {
        return formatMillis(averagePitWaitTime);
    }

    private String formatMillis(long millis) {
        long minutes = millis / 60000;
        long seconds = (millis % 60000) / 1000;
        long ms = millis % 1000;
        return String.format("%02d:%02d.%03d", minutes, seconds, ms);
    }
}
