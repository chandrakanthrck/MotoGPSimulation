package com.motogp.MotoGP.service;

import com.motogp.MotoGP.dto.RaceResultDTO;
import com.motogp.MotoGP.model.*;
import com.motogp.MotoGP.repository.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class RaceSimulationService {

    private final RaceSessionRepository raceSessionRepository;
    private final LapRepository lapRepository;
    private final PitStopRepository pitStopRepository;
    private final RiderRepository riderRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private final ReentrantLock pitLock = new ReentrantLock();
    private final Condition pitAvailable = pitLock.newCondition();
    private static final int MAX_PIT_SLOTS = 2;
    private int pitInUse = 0;

    public RaceSimulationService(RaceSessionRepository raceSessionRepository,
                                 LapRepository lapRepository,
                                 PitStopRepository pitStopRepository,
                                 RiderRepository riderRepository,
                                 SimpMessagingTemplate messagingTemplate) {
        this.raceSessionRepository = raceSessionRepository;
        this.lapRepository = lapRepository;
        this.pitStopRepository = pitStopRepository;
        this.riderRepository = riderRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public void startRace(List<Rider> riders) {
        RaceSession raceSession = new RaceSession();
        raceSession.setTrackName("Le Mans");
        raceSession.setStartTime(LocalDateTime.now());
        raceSession = raceSessionRepository.save(raceSession);

        CountDownLatch raceStartLatch = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(riders.size());

        for (Rider rider : riders) {
            rider.setRaceSession(raceSession);
            riderRepository.save(rider);

            executor.submit(() -> {
                try {
                    System.out.println("🔒 " + rider.getName() + " waiting for race start...");
                    raceStartLatch.await();
                    runRiderRace(rider);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}

        System.out.println("🏁 RACE STARTING NOW!");
        raceStartLatch.countDown();
        executor.shutdown();
    }

    private void runRiderRace(Rider rider) {
        for (int lap = 1; lap <= 5; lap++) {
            try {
                long lapTime = (long) (3000 + Math.random() * 2000);
                Thread.sleep(lapTime);

                Lap newLap = new Lap();
                newLap.setLapNumber(lap);
                newLap.setLapTimeMillis(lapTime);
                newLap.setTimestamp(LocalDateTime.now());
                newLap.setRider(rider);
                lapRepository.save(newLap);

                System.out.println("✅ " + rider.getName() + " completed lap " + lap);
                broadcastLapUpdate(rider, newLap);

                if (lap == 3) {
                    handlePitStop(rider);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void handlePitStop(Rider rider) {
        long requestTimeMillis = System.currentTimeMillis();
        pitLock.lock();

        try {
            System.out.println("🅿️ " + rider.getName() + " wants to enter the pit...");

            while (pitInUse >= MAX_PIT_SLOTS) {
                System.out.println("⏳ " + rider.getName() + " waiting. Pit is full.");
                pitAvailable.await();
            }

            long waitTimeMillis = System.currentTimeMillis() - requestTimeMillis;
            pitInUse++;
            System.out.println("🔧 " + rider.getName() + " entered pit (waited " + waitTimeMillis + " ms)");

            PitStop pit = new PitStop();
            pit.setType(Math.random() > 0.5 ? "Fuel" : "Tire");
            pit.setStartTime(LocalDateTime.now());
            pit.setWaitTimeMillis(waitTimeMillis);
            broadcastPitStopUpdate(rider, pit);

            pitLock.unlock();
            Thread.sleep((long) (1000 + Math.random() * 2000));
            pitLock.lock();

            pit.setEndTime(LocalDateTime.now());
            pit.setRider(rider);
            pitStopRepository.save(pit);

            pitInUse--;
            System.out.println("✅ " + rider.getName() + " exits pit. (in use: " + pitInUse + ")");
            pitAvailable.signal();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (pitLock.isHeldByCurrentThread()) {
                pitLock.unlock();
            }
        }
    }

    private void broadcastLapUpdate(Rider rider, Lap lap) {
        Map<String, Object> lapUpdate = new HashMap<>();
        lapUpdate.put("rider", rider.getName());
        lapUpdate.put("lapNumber", lap.getLapNumber());
        lapUpdate.put("lapTimeMillis", lap.getLapTimeMillis());

        messagingTemplate.convertAndSend("/topic/lap", lapUpdate);
    }

    private void broadcastPitStopUpdate(Rider rider, PitStop pit) {
        Map<String, Object> pitStopUpdate = new HashMap<>();
        pitStopUpdate.put("rider", rider.getName());
        pitStopUpdate.put("type", pit.getType());
        pitStopUpdate.put("waitTimeMillis", pit.getWaitTimeMillis());

        messagingTemplate.convertAndSend("/topic/pit", pitStopUpdate);
    }

    public List<RaceResultDTO> getRaceResults() {
        List<Rider> riders = riderRepository.findAll();
        List<RaceResultDTO> results = new ArrayList<>();

        for (Rider rider : riders) {
            List<Lap> laps = lapRepository.findAll().stream()
                    .filter(l -> l.getRider().getId().equals(rider.getId()))
                    .toList();

            List<PitStop> pitStops = pitStopRepository.findAll().stream()
                    .filter(p -> p.getRider().getId().equals(rider.getId()))
                    .toList();

            long totalLapTime = laps.stream().mapToLong(Lap::getLapTimeMillis).sum();
            long avgLapTime = laps.isEmpty() ? 0 : totalLapTime / laps.size();
            long bestLapTime = laps.stream().mapToLong(Lap::getLapTimeMillis).min().orElse(0);
            long avgWaitTime = pitStops.isEmpty() ? 0 :
                    pitStops.stream().mapToLong(PitStop::getWaitTimeMillis).sum() / pitStops.size();

            results.add(new RaceResultDTO(
                    rider.getName(),
                    avgLapTime,
                    laps.size(),
                    pitStops.size(),
                    avgWaitTime,
                    bestLapTime,
                    totalLapTime
            ));
        }

        writeResultsToCsv(results);
        return results;
    }

    private void writeResultsToCsv(List<RaceResultDTO> results) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String fileName = "race_results_" + timestamp + ".csv";

        try (PrintWriter writer = new PrintWriter(fileName)) {
            writer.println("Rider,AvgLapTime,TotalLaps,PitStops,AvgPitWaitTime,BestLap,TotalRaceTime");

            for (RaceResultDTO r : results) {
                writer.printf("%s,%s,%d,%d,%s,%s,%s%n",
                        r.getRiderName(),
                        formatMillis(r.getAverageLapTime()),
                        r.getTotalLaps(),
                        r.getPitStopCount(),
                        formatMillis(r.getAveragePitWaitTime()),
                        formatMillis(r.getBestLapTime()),
                        formatMillis(r.getTotalRaceTime()));
            }

            System.out.println("📁 Race results written to: " + fileName);
        } catch (Exception e) {
            System.err.println("❌ Failed to write race results: " + e.getMessage());
        }
    }

    private String formatMillis(long millis) {
        long minutes = millis / 60000;
        long seconds = (millis % 60000) / 1000;
        long ms = millis % 1000;
        return String.format("%02d:%02d.%03d", minutes, seconds, ms);
    }
}
