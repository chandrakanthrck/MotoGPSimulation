package com.motogp.MotoGP.service;

import com.motogp.MotoGP.model.*;
import com.motogp.MotoGP.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class RaceSimulationService {

    private final RaceSessionRepository raceSessionRepository;
    private final LapRepository lapRepository;
    private final PitStopRepository pitStopRepository;
    private final RiderRepository riderRepository;

    // Only 2 crews available for pit stops
    private final ReentrantLock pitLock = new ReentrantLock();
    private final Condition pitAvailable = pitLock.newCondition();
    private int pitInUse = 0;
    private final int MAX_PIT_SLOTS = 2;

    public RaceSimulationService(RaceSessionRepository raceSessionRepository,
                                 LapRepository lapRepository,
                                 PitStopRepository pitStopRepository,
                                 RiderRepository riderRepository) {
        this.raceSessionRepository = raceSessionRepository;
        this.lapRepository = lapRepository;
        this.pitStopRepository = pitStopRepository;
        this.riderRepository = riderRepository;
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
                    raceStartLatch.await(); // Wait for green light
                    runRiderRace(rider);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        try {
            Thread.sleep(1000); // Let all threads reach await()
        } catch (InterruptedException ignored) {}

        System.out.println("🏁 RACE STARTING NOW!");
        raceStartLatch.countDown(); // Green light: all riders go!

        executor.shutdown();
    }

    private void runRiderRace(Rider rider) {
        for (int lap = 1; lap <= 5; lap++) {
            try {
                long lapTime = (long) (3000 + Math.random() * 2000);
                Thread.sleep(lapTime); // Simulate lap duration

                Lap newLap = new Lap();
                newLap.setLapNumber(lap);
                newLap.setLapTimeMillis(lapTime);
                newLap.setTimestamp(LocalDateTime.now());
                newLap.setRider(rider);
                lapRepository.save(newLap);

                System.out.println("✅ " + rider.getName() + " completed lap " + lap);

                if (lap == 3) {
                    handlePitStop(rider);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void handlePitStop(Rider rider) {
        pitLock.lock();
        try {
            System.out.println("🅿️ " + rider.getName() + " wants to enter the pit...");

            // Wait until a pit crew slot is available
            while (pitInUse >= MAX_PIT_SLOTS) {
                System.out.println("⏳ " + rider.getName() + " is waiting. Pit is full.");
                pitAvailable.await();
            }

            // Enter pit
            pitInUse++;
            System.out.println("🔧 " + rider.getName() + " has entered the pit. (in use: " + pitInUse + ")");

            // Prepare pit stop log
            PitStop pit = new PitStop();
            pit.setType(Math.random() > 0.5 ? "Fuel" : "Tire");
            pit.setStartTime(LocalDateTime.now());

            // Unlock while simulating the actual pit stop
            pitLock.unlock();
            Thread.sleep((long) (1000 + Math.random() * 2000)); // simulate pit duration
            pitLock.lock();

            pit.setEndTime(LocalDateTime.now());
            pit.setRider(rider);
            pitStopRepository.save(pit);

            // Exit pit
            pitInUse--;
            System.out.println("✅ " + rider.getName() + " leaves the pit. (in use: " + pitInUse + ")");
            pitAvailable.signal(); // Notify one waiting rider

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // Defensive: check if the lock is held before unlocking
            if (pitLock.isHeldByCurrentThread()) {
                pitLock.unlock();
            }
        }
    }
}
