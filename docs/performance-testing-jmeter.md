# Performance Testing – Product Service with Apache JMeter

## Step 1: Install Apache JMeter

1. Go to https://jmeter.apache.org/download_jmeter.cgi
2. Download the **Binary** zip (e.g., `apache-jmeter-5.x.x.zip`)
3. Extract it somewhere (e.g., `C:\jmeter`)
4. Run `C:\jmeter\bin\jmeter.bat` to launch the GUI

> Requires Java 8+. Check with `java -version`.

---

## Step 2: Create a Test Plan

### 2a. Add a Thread Group

1. Right-click **Test Plan** → **Add** → **Threads (Users)** → **Thread Group**
2. Configure:
   - **Number of Threads (users):** `50` (virtual users)
   - **Ramp-Up Period:** `10` (seconds — starts all 50 users over 10s)
   - **Loop Count:** `5` (each user sends 5 requests = 250 total requests)

### 2b. Add an HTTP Request Sampler

1. Right-click **Thread Group** → **Add** → **Sampler** → **HTTP Request**
2. Configure:
   - **Protocol:** `http`
   - **Server Name or IP:** `localhost`
   - **Port:** `8080` (or whatever port your service runs on)
   - **HTTP Method:** `GET`
   - **Path:** `/products/1` (use a valid product ID)

### 2c. Add HTTP Header Manager (if needed)

1. Right-click **HTTP Request** → **Add** → **Config Element** → **HTTP Header Manager**
2. Add header: `Content-Type` = `application/json`

### 2d. Add Listeners (for results)

Right-click **Thread Group** → **Add** → **Listener** → add both:

- **View Results Tree** — shows each request/response detail
- **Summary Report** — shows throughput, avg response time, error %

---

## Step 3: Start Your Product Service

Make sure the service is running before executing the test:

```powershell
./mvnw spring-boot:run
```

---

## Step 4: Run the Test

1. Click the **green Play button** (▶) in JMeter toolbar
2. Switch to **View Results Tree** listener to watch requests in real time
3. Green = success, Red = failure

---

## Step 5: Analyze Results

### View Results Tree shows per-request:

- **Request** tab — what was sent
- **Response Data** tab — what came back
- **Sampler result** — response time, status code

### Summary Report shows aggregate:

| Metric | What it means |
|---|---|
| **Throughput** | Requests/second the server handled |
| **Average (ms)** | Mean response time |
| **90th %ile** | 90% of requests finished within this time |
| **Error %** | Percentage of failed requests |

---

## Tips

- Start with low threads (e.g., 10) and increase to find the breaking point
- Save your test plan as `.jmx` file (**File → Save**) to reuse it
- If you get connection errors, ensure the service is actually running and the port matches
- Use a **real product ID** that exists in your DB for the path `/products/{id}`