package com.motogp.MotoGP.controller;

import com.motogp.MotoGP.dto.RaceResultDTO;
import com.motogp.MotoGP.service.RaceSimulationService;
import com.motogp.MotoGP.model.Rider;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/race")
public class RaceController {

    private final RaceSimulationService simulationService;

    public RaceController(RaceSimulationService simulationService) {
        this.simulationService = simulationService;
    }

    /**
     * Starts a new MotoGP race with a list of rider names.
     * Example input: ["Rossi", "Marquez", "Bagnaia"]
     */
    @PostMapping("/start")
    public String startRace(@RequestBody List<String> riderNames) {
        if (riderNames == null || riderNames.isEmpty()) {
            throw new IllegalArgumentException("Rider names list cannot be empty.");
        }

        System.out.println("[DEBUG] /race/start called with riders: " + riderNames);

        List<Rider> riders = riderNames.stream().map(name -> {
            Rider rider = new Rider();
            rider.setName(name);
            rider.setTeam("Factory Team"); // default team name
            return rider;
        }).collect(Collectors.toList());

        simulationService.startRace(riders);

        return "Race started with " + riders.size() + " riders!";
    }

    /**
     * Simple ping endpoint to verify controller is loaded.
     */
    @GetMapping("/ping")
    public String ping() {
        return "RaceController is up!";
    }

    @GetMapping("/results")
    public List<RaceResultDTO> getResults() {
        return simulationService.getRaceResults();
    }

    /**
     * Leaderboard endpoint to get sorted results.
     * Optional query param: ?sort=bestLap | totalTime (default: bestLap)
     */
    @GetMapping("/leaderboard")
    public List<RaceResultDTO> getLeaderboard(@RequestParam(name = "sort", defaultValue = "bestLap") String sortBy) {
        List<RaceResultDTO> results = simulationService.getRaceResults();

        switch (sortBy.toLowerCase()) {
            case "totaltime" -> results.sort(Comparator.comparingLong(RaceResultDTO::getTotalRaceTime));
            case "bestlap" -> results.sort(Comparator.comparingLong(RaceResultDTO::getBestLapTime));
            default -> throw new IllegalArgumentException("Unsupported sort type. Use 'bestLap' or 'totalTime'.");
        }

        return results;
    }
}
