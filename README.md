# MotoGP Simulation Backend

A multi-threaded race simulation backend built with **Spring Boot** and **MySQL**, designed to model real-time MotoGP racing with advanced concurrency and system design concepts.

---

## 🚀 Key Features

- 🏁 **Synchronized Race Start** — All riders start together using `CountDownLatch`.
- ⏱️ **Lap Time Simulation** — Riders complete laps with randomized durations.
- 🅿️ **Pit Stop Limiting** — Only 2 riders allowed in the pit simultaneously via `Semaphore` + `ReentrantLock`.
- 📊 **Persistent Storage** — Riders, laps, pit stops, and race sessions stored in MySQL via Spring Data JPA.
- 💡 **System Design Oriented** — Built to demonstrate real-world synchronization, multithreading, and resource contention patterns.

---

## 📬 API Endpoints

### `POST /race/start`

Starts a new race with given rider names.

**Request Body:**
```json
["Rossi", "Marquez", "Bagnaia"]
```

**Response:**
```
🏁 Race started with 3 riders!
```

---

### `GET /race/results`

Returns average lap time, pit stops, best lap, total race time per rider.

---

### `GET /race/leaderboard`

Sorted leaderboard based on best lap time.

---

## ⚙️ Setup & Run

1. **Start MySQL** and create the database:
```sql
CREATE DATABASE motogp_db;
```

2. **Configure `application.yml`:**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/motogp_db?socket=/tmp/mysql.sock
    username: root
    password:
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

3. **Build & Run:**
```bash
mvn clean install
./mvnw spring-boot:run
```

4. **Test with Postman or cURL.**

---

## 📈 Sample Output Metrics

| Rider   | Avg Lap | Pit Stops | Best Lap | Wait Time | Total Time |
|---------|---------|-----------|----------|-----------|------------|
| Rossi   | 04:12.1 | 1         | 03:52.3  | 00:01.3   | 20:30.8    |
| Marquez | 04:07.5 | 1         | 03:49.0  | 00:02.1   | 20:15.5    |

_CSV logs are auto-generated after each race._

---

## 🧪 CI/CD Ready

- GitHub Actions for Maven build and test
- `.github/workflows/build.yml` included
- Easily extendable to run integration tests with MySQL Docker container
