package com.motogp.MotoGP.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Rider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String team;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private RaceSession raceSession;

    @OneToMany(mappedBy = "rider", cascade = CascadeType.ALL)
    private List<Lap> laps;

    @OneToMany(mappedBy = "rider", cascade = CascadeType.ALL)
    private List<PitStop> pitStops;
}
