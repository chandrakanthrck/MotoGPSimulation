package com.motogp.MotoGP.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Lap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int lapNumber;
    private Long lapTimeMillis;
    private LocalDateTime timestamp;

    @ManyToOne
    private Rider rider;
}
