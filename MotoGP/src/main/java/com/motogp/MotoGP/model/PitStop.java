package com.motogp.MotoGP.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class PitStop {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // "Fuel" or "Tire"
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long waitTimeMillis; // how long rider waited to enter pit

    @ManyToOne
    private Rider rider;
}
