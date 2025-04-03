package com.motogp.MotoGP.service;

import com.motogp.MotoGP.model.Lap;
import com.motogp.MotoGP.model.PitStop;
import com.motogp.MotoGP.model.RaceSession;
import com.motogp.MotoGP.model.Rider;
import com.motogp.MotoGP.repository.LapRepository;
import com.motogp.MotoGP.repository.PitStopRepository;
import com.motogp.MotoGP.repository.RaceSessionRepository;
import com.motogp.MotoGP.repository.RiderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

@Service
public class RaceSimulationService {
    private final RaceSessionRepository raceSessionRepository;
    private final LapRepository lapRepository;
    private final PitStopRepository pitStopRepository;
    private final RiderRepository riderRepository;

    // Only 2 crews available
    private final Semaphore pitCrewSemaphore = new Semaphore(2);


    public RaceSimulationService(RaceSessionRepository raceSessionRepository,
                                 LapRepository lapRepository,
                                 PitStopRepository pitStopRepository,
                                 RiderRepository riderRepository) {
        this.raceSessionRepository = raceSessionRepository;
        this.lapRepository = lapRepository;
        this.pitStopRepository = pitStopRepository;
        this.riderRepository = riderRepository;
    }

    public void startRace(List<Rider> riders){
        RaceSession raceSession = new RaceSession();
        raceSession.setTrackName("Le Mans");
        raceSession.setStartTime(LocalDateTime.now());
        raceSessionRepository.save(raceSession);

        ExecutorService executor = Executors.newFixedThreadPool(riders.size());

        for(Rider rider : riders){
            rider.setRaceSession(raceSession);
            riderRepository.save(rider);
            executor.submit(() -> runRiderRace(rider));
        }

        executor.shutdown();
    }

    private void runRiderRace(Rider rider){
        for(int lap = 1; lap <= 5; lap++){
            try{
                //simulation
                long lapTime = (long) (3000 + Math.random() * 2000);
                //real time lap duration simulation
                Thread.sleep(lapTime);
                //log the completed lap
                Lap newLap = new Lap();
                newLap.setLapNumber(lap);
                newLap.setLapTimeMillis(lapTime);
                newLap.setTimestamp(LocalDateTime.now());
                newLap.setRider(rider);
                //lap data saved to repository
                lapRepository.save(newLap);
                //at lap 3, the rider will go for a pitstop
                if(lap == 3){
                    handlePitStop(rider);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // This method simulates a pit stop operation (tire/fuel) with crew limitation
    private void handlePitStop(Rider rider){
        try{
            // Limit pit stops to only 2 riders at a time using a Semaphore
            pitCrewSemaphore.acquire(); // Waits if both crews are busy
            // Create new pit stop log entry
            PitStop pit = new PitStop();
            pit.setType(Math.random() > 0.5 ? "Fuel" : "Tire"); // Randomly pick type
            pit.setStartTime(LocalDateTime.now());
            Thread.sleep((long) (1000 + Math.random() * 2000)); // 🛠 Work being done

            // Record when pit stop ends
            pit.setEndTime(LocalDateTime.now());
            pit.setRider(rider);
            pitStopRepository.save(pit); // Save pit stop data to DB
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted state
        } finally {
            pitCrewSemaphore.release(); // Free up pit crew slot for next rider
        }
    }
}
