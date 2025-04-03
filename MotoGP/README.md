# 🏍️ MotoGP Simulation

A high-performance, multi-threaded MotoGP race simulation backend built with **Spring Boot** and **MySQL**, designed to demonstrate system design principles such as:

- Multithreading
- Synchronization (CountDownLatch, Semaphore)
- Concurrency-safe I/O operations
- Data persistence using JPA
- Real-world race simulation logic

---

## 🚀 Features

- 🏁 **Synchronized race start**: All riders wait until the race officially starts using `CountDownLatch`.
- ⏱️ **Lap simulation**: Riders complete timed laps with random durations.
- 🅿️ **Pit stop coordination**: Only 2 riders allowed in the pit crew simultaneously, managed with a `Semaphore`.
- 📊 **Data persistence**: Race sessions, riders, laps, and pit stops are stored in a MySQL database.
- 🧠 **System design focus**: Ideal for showcasing concurrency, synchronization, and real-time simulation logic.

---

## 📦 Tech Stack

- Java 17
- Spring Boot 3.x
- Spring Data JPA
- MySQL
- Lombok
- Maven

---

## 📂 Project Structure

```
├── controller         # REST endpoints for race simulation
├── model              # JPA entity models (Rider, Lap, PitStop, RaceSession)
├── repository         # Spring Data repositories
├── service            # Race logic including concurrency and synchronization
├── resources
│   └── application.yml # Database configuration
└── MotoGpApplication.java
```

---

## 📬 API Endpoints

### POST `/race/start`

Start a new race with a list of rider names.

**Request body:**
```json
["Rossi", "Marquez", "Bagnaia"]
```

**Response:**
```
🏁 Race started with 3 riders!
```

---

## ⚙️ Concurrency Highlights

| Feature               | Tool Used                     | Purpose                                      |
|----------------------|-------------------------------|----------------------------------------------|
| Race synchronization | `CountDownLatch`              | All threads wait for green light to start    |
| Pit lane limit       | `Semaphore` (2 permits)       | Only 2 riders allowed in pit stop at a time  |
| Multithreaded laps   | `ExecutorService`             | Each rider runs in parallel                  |
| Shared data safety   | Spring JPA (thread-safe repos)| Avoid race conditions in DB writes           |

---

## 🗃️ Database Schema

Automatically generated via JPA:

- `race_session`
- `rider`
- `lap`
- `pit_stop`

---

## 🧪 How to Run

1. Start MySQL and create a database:

```sql
CREATE DATABASE motogp_db;
```

2. Configure your `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/motogp_db?socket=/tmp/mysql.sock
    username: root
    password:
```

3. Build and run:

```bash
mvn clean install
./mvnw spring-boot:run
```

4. Use Postman or `curl` to hit `/race/start`.

---

## ✨ What's Next

- ⛓️ Add pit lane queue with `Condition` variables
- 📁 Write lap/pit logs to file (File I/O)
- 🏆 Leaderboard API (fastest lap, average lap, total time)
- 📊 Prometheus metrics or OpenAPI UI

---

## 💼 Designed For System Interviews

This project simulates concurrency-heavy workflows and showcases:

- Thread lifecycle control
- Resource contention handling
- Real-world system breakdown
- Clean, testable backend architecture