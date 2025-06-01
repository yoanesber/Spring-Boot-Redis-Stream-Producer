# Order Payment Service with Redis Streams as Reliable Message Producer for PAYMENT_SUCCESS / PAYMENT_FAILED Events

## 📖 Overview  

This project is a **Spring Boot REST API** for creating and processing order payments using **Redis Streams** as the message broker — replacing **Pub/Sub** for a more reliable, persistent, and scalable solution. Unlike traditional Pub/Sub mechanisms where messages are lost if no subscriber is listening, Redis Streams persist messages until they are explicitly **acknowledged**, ensuring **durability and reliability** in event-driven systems. Redis interprets the **acknowledgment** as: this message was correctly processed so it can be evicted from the consumer group.  


### 💡 Why Redis Streams?  

Unlike Pub/Sub, Redis Streams offer:  

- **Persistence** – Messages are stored in Redis until explicitly acknowledged by a consumer.
- **Reliability** – Ensures that no messages are lost — perfect for critical systems like payments.
- **Scalability** – Built-in support for consumer groups and horizontal scaling.
- **Replayability** – Failed or pending messages can be retried, replayed, or analyzed.


### 💡 What is a Consumer Group in Redis Streams?  

A **Consumer Group** in Redis Streams is a mechanism for distributing and managing data consumption by **multiple consumers** in a parallel and coordinated manner. While `XREAD` (regular) is suitable for a single consumer, a Consumer Group (`XREADGROUP`) is ideal for multiple consumers processing the stream together. A Consumer Group allows multiple consumers to share the workload of processing messages without duplication. Each message is delivered to only one consumer in the group.  


#### 💡 Why Do We Need Consumer Groups?  

- To enable multiple consumers to collaborate in processing messages.
- To track which messages have been read and which are still pending.
- To retry processing if a message fails.
- To ensure each message is read by only one consumer, unlike Pub/Sub where all consumers receive the same message.


#### 💡 How Consumer Groups Work?

1. A stream is created (`XADD`).  
2. A consumer group is created (`XGROUP CREATE`).  
3. Multiple consumers join the group and start processing messages (`XREADGROUP`).  
4. Messages are assigned to a consumer within the group (only one consumer gets each message).  
5. Consumers acknowledge (`XACK`) processed messages, so Redis knows they are handled.  


### 📌 Redis Stream Message ID (RecordId)  

Each message published to a Redis stream is assigned a unique `RecordId` which acts as its primary identifier in the stream. This ID is composed of a **timestamp and sequence number**, ensuring uniqueness even for multiple messages added in the same millisecond.  


### 🧩 Event-Driven Architecture

This project implements a reliable event-driven architecture using Redis Streams to handle Order Payment creation and processing. Below is a breakdown of the full flow:  

```text
[Client]──▶ (HTTP POST /order-payment)──▶ [Spring Boot API] ──▶ [Redis Stream: PAYMENT_SUCCESS or PAYMENT_FAILED]
```

#### 🔄 How It Works  

1. Client Request  
A client sends an HTTP POST request to the `/order-payment` endpoint with the necessary order payment data.  
2. Spring Boot API (Producer)  
- The API receives the request, processes the initial business logic, and then publishes a message to the appropriate Redis stream:  
    - `PAYMENT_SUCCESS` if the payment is successful.  
    - `PAYMENT_FAILED` if the payment fails validation or processing.  
- Each message sent to the stream includes a manually generated `RecordId`, ensuring consistent tracking and ordering.  
3. Redis Streams  
- Redis Streams persist these messages until they are acknowledged by a consumer.  
- This allows for reliable message delivery, replay, and tracking of pending/unprocessed messages.  


### 🚀 Features  

Below are the core features that make this solution robust and ready for real-world scenarios:  

- ✅ Create and submit order payment requests via REST API  
- 📨 StreamProducer sends events to Redis stream (`PAYMENT_SUCCESS` or `PAYMENT_FAILED`)  

---

## 🤖 Tech Stack  

The technology used in this project are:  

| Technology                    | Description                                                                                                                                                                       |
|-------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `Spring Boot Starter Web`     | Building RESTful APIs or web applications. Used in this project to handle HTTP requests for creating and managing order payments.                                                 |
| `Spring Data Redis (Lettuce)` | A high-performance Redis client built on Netty. Integrates Redis seamlessly into Spring, allowing the application to produce and consume Redis Streams with ease.                 |
| `RedisTemplate`               | A powerful abstraction provided by Spring Data Redis for performing Redis operations, including stream publishing (XADD), consuming (XREADGROUP), acknowledging (XACK), and more. |
| `Lombok`                      | Reducing boilerplate code                                                                                                                                                         |

---

## 🧱 Architecture Overview  

The project is organized into the following package structure:  

```bash
📁 redis-stream-producer/
└── 📂src/
    └── 📂main/
        ├── 📂docker/
        │   ├── 📂app/                     # Dockerfile for Spring Boot application (runtime container)
        │   └── 📂redis/                   # Dockerfile and configs for Redis container (optional/custom)
        ├── 📂java/
        │   ├── 📂config/                  # Spring configuration classes
        │   │   ├── 📂redis/               # Redis-specific configuration (e.g., RedisTemplate, Lettuce client setup)
        │   │   └── 📂serializer/          # Custom Jackson serializers/deserializers (e.g., for `Instant`)
        │   ├── 📂controller/              # Defines REST API endpoints for handling order payment requests, acting as the entry point for client interactions.
        │   ├── 📂dto/                     # Contains Data Transfer Objects used for API request and response models, such as creating an order payment.
        │   ├── 📂entity/                  # Includes core domain models like Order, OrderDetail, and OrderPayment which represent the message structures.
        │   ├── 📂mapper/                  # Data mappers or converters, mapping between entity and DTOs or other representations
        │   ├── 📂redis/                   # Manages Redis stream message producers, including logic for publishing payment events (`PAYMENT_SUCCESS`, `PAYMENT_FAILED`).
        │   └── 📂service/                 # Encapsulates the business logic related to order creation and payment processing.
        │       └── 📂impl/                # Implementation of services
        └── 📂resources/
            └── application.properties     # Application configuration (redis, profiles, etc.)
```
---


## 🛠️ Installation & Setup  

Follow these steps to set up and run the project locally:  

### ✅ Prerequisites

Make sure the following tools are installed on your system:

| Tool                                      | Description                                                           | Required      |
|-------------------------------------------|-----------------------------------------------------------------------|---------------|
| [Java 17+](https://adoptium.net/)         | Java Development Kit (JDK) to run the Spring application              | ✅            |
| [Redis](https://redis.io/)                | In-memory data structure store used as a message broker via Streams   | ✅            |
| [Make](https://www.gnu.org/software/make/)| Automation tool for tasks like `make run-app`                         | ✅            |
| [Docker](https://www.docker.com/)         | To run services like Redis in isolated containers                     | ⚠️ *optional* |

### ☕ 1. Install Java 17  

1. Ensure **Java 17** is installed on your system. You can verify this with:  

```bash
java --version
```  

2. If Java is not installed, follow one of the methods below based on your operating system:  

#### 🐧 Linux  

**Using apt (Ubuntu/Debian-based)**:  

```bash
sudo apt update
sudo apt install openjdk-17-jdk
```  

#### 🪟 Windows  
1. Use [https://adoptium.net](https://adoptium.net) to download and install **Java 17 (Temurin distribution recommended)**.  

2. After installation, ensure `JAVA_HOME` is set correctly and added to the `PATH`.  

3. You can check this with:  

```bash
echo $JAVA_HOME
```  

### 🔌 2. Install Redis  

1. Redis doesn't provide official support for Windows, but you can run it via **WSL(Windows Subsystem for Linux)** or **Docker**:  

#### 🐧 Linux  

**Using apt (Ubuntu/Debian-based)**:  

```bash
sudo apt update
sudo apt install redis
```  

2. Start Redis:

```bash
sudo systemctl start redis
```

3. Enable on boot:

```bash
sudo systemctl enable redis
```

4. Test:

```bash
redis-cli ping
# PONG
```

5. (Optional) If you want to add a specific user with access to a specific stream channel, you can run the following command in **Redis CLI**:  

```bash
ACL SETUSER spring_producer on >P@ssw0rd ~stream:* +xadd +ping
ACL SETUSER spring_consumer on >P@ssw0rd ~stream:* +xread +xreadgroup +xack +ping
```

Set up Redis user and password in `application.properties`:  

```properties
# Redis properties
spring.data.redis.username=spring_producer
spring.data.redis.password=P@ssw0rd
```

### 🧰 3. Install `make` (Optional but Recommended)  
This project uses a `Makefile` to streamline common tasks.  

Install `make` if not already available:  

#### 🐧 Linux  

Install `make` using **APT**  

```bash
sudo apt update
sudo apt install make
```  

You can verify installation with:   
```bash
make --version
```  

#### 🪟 Windows  

If you're using **PowerShell**:  

- Install [Chocolatey](https://chocolatey.org/install) (if not installed):  
```bash
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.SecurityProtocolType]::Tls12; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
```  

- Verify `Chocolatey` installation:  
```bash
choco --version
```  

- Install `make` via `Chocolatey`:  
```bash
choco install make
```  

After installation, **restart your terminal** or ensure `make` is available in your `PATH`.  

### 🔁 4. Clone the Project  

Clone the repository:  

```bash
git clone https://github.com/yoanesber/Spring-Boot-Redis-Stream-Producer.git
cd Spring-Boot-Redis-Stream-Producer
```  


### ⚙️ 5. Configure Application Properties  

Set up your `application.properties` in `src/main/resources`:  

```properties
# Application properties
spring.application.name=redis-stream-producer
server.port=8080
spring.profiles.active=development

# Redis configuration
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.username=default
spring.data.redis.password=
spring.data.redis.timeout=5
spring.data.redis.connect-timeout=3
spring.data.redis.lettuce.shutdown-timeout=10
```

- **🔐 Notes**:  Ensure that:  
  - Redis username, and password are correct.  



## 🚀 6. Running the Application  

This section provides step-by-step instructions to run the application either **locally** or via **Docker containers**.

- **Notes**:  
  - All commands are defined in the `Makefile`.
  - To run using `make`, ensure that `make` is installed on your system.
  - To run the application in containers, make sure `Docker` is installed and running.


### 🔧 Run Locally (Non-containerized)

Ensure Redis are running locally, then:

```bash
make dev
```

### 🐳 Run Using Docker

To build and run all services (Redis, Spring app):

```bash
make docker-up
```

To stop and remove all containers:

```bash
make docker-down
```

- **Notes**:  
  - Before running the application inside Docker, make sure to update your `application.properties`
    - Replace `localhost` with the appropriate **container name** for services like Redis.  
    - For example:
      - Change `spring.data.redis.host=localhost` to `spring.data.redis.host=redis-stream-server`

### 🟢 Application is Running

Now your application is accessible at:

```bash
http://localhost:8080
```

---

## 🧪 Testing Scenarios

To ensure that the Redis Stream-based order payment system works reliably under various conditions, several test scenarios were conducted. Below are the descriptions and outcomes of each test scenario along with visual evidence (captured screenshots) to demonstrate the flow and results.  

1. Successful Order Payment Processing  

This scenario tests the normal flow where an order payment is created successfully and published to the Redis stream.  
`POST http://localhost:8080/api/v1/order-payment` - Create a new order payment and trigger payment processing.  

**Body Request (CREDIT_CARD):**  
```json
{
    "orderId":"ORD123456781",
    "amount":"199.99",
    "currency":"USD",
    "paymentMethod":"CREDIT_CARD",
    "cardNumber":"1234 5678 9012 3456",
    "cardExpiry":"31/12",
    "cardCvv":"123"
}
```

**Successful Response (CREDIT_CARD):**  
```json
{
    "statusCode": 201,
    "timestamp": "2025-04-06T14:36:32.568915Z",
    "message": "Order payment created successfully",
    "data": {
        "orderId": "ORD123456781",
        "transactionId": "TXN1743950189267",
        "paymentStatus": "SUCCESS",
        "amount": 199.99,
        "currency": "USD",
        "paymentMethod": "CREDIT_CARD",
        "createdAt": "2025-04-06T14:36:29.268562700Z"
    }
}
```

**Run this command to show latest entries only. This reads the stream in reverse order (latest first).**  
```bash
XREVRANGE PAYMENT_SUCCESS + - COUNT 1
```

**📸 Screenshot below shows the stream entry**  
![Image](https://github.com/user-attachments/assets/60efba3b-bee0-49f7-83be-0d91294ce16b)

**Conclusion:** After executing the `XREVRANGE PAYMENT_SUCCESS + - COUNT 1` command, the latest message in the `PAYMENT_SUCCESS` Redis stream was successfully retrieved. This confirms that the normal flow, where a valid order payment is processed and then published to the Redis stream, is working as expected. The message was acknowledged correctly, indicating that the system successfully handled the order payment event without any errors or retries.

---


## 🔗 Related Repositories  

- For the Redis Stream as Message Consumer implementation, check out [Spring Boot Redis Stream Consumer with ThreadPoolTaskScheduler Integration](https://github.com/yoanesber/Spring-Boot-Redis-Stream-Consumer).  
- For the Redis Publisher implementation, check out [Spring Boot Redis Publisher with Lettuce](https://github.com/yoanesber/Spring-Boot-Redis-Publisher-Lettuce).  
- For the Redis Subscriber implementation, check out [Spring Boot Redis Subscriber with Lettuce](https://github.com/yoanesber/Spring-Boot-Redis-Subscriber-Lettuce).  