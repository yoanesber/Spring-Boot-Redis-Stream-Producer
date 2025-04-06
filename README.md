# Order Payment Service with Redis Streams as Reliable Message Producer for PAYMENT_SUCCESS / PAYMENT_FAILED Events

## 📖 Overview
This project is a **Spring Boot REST API** for creating and processing order payments using **Redis Streams** as the message broker — replacing **Pub/Sub** for a more reliable, persistent, and scalable solution. Unlike traditional Pub/Sub mechanisms where messages are lost if no subscriber is listening, Redis Streams persist messages until they are explicitly **acknowledged**, ensuring **durability and reliability** in event-driven systems. Redis interprets the acknowledgment as: this message was correctly processed so it can be evicted from the consumer group.  

### 🚀 Features  

- ✅ Create and submit order payment requests via REST API
- 📨 StreamProducer sends events to Redis stream (`PAYMENT_SUCCESS` or `PAYMENT_FAILED`)


### 💡 Why Redis Streams?  

Unlike Pub/Sub, Redis Streams offer:  

- **Persistence** – Messages are stored in Redis until explicitly acknowledged by a consumer.
- **Reliability** – Ensures that no messages are lost — perfect for critical systems like payments.
- **Scalability** – Built-in support for consumer groups and horizontal scaling.
- **Replayability** – Failed or pending messages can be retried, replayed, or analyzed.


### 📌 Redis Stream Message ID (RecordId)  

Each message published to a Redis stream is assigned a unique `RecordId` which acts as its primary identifier in the stream. This ID is composed of a **timestamp and sequence number**, ensuring uniqueness even for multiple messages added in the same millisecond.  


### 🧩 Architecture Overview

The system implements a reliable event-driven architecture using Redis Streams to handle Order Payment creation and processing. Below is a breakdown of the full flow:  

```text
[Client] ── (HTTP POST /order-payment)──▶ [Spring Boot API] ──▶ [Redis Stream: PAYMENT_SUCCESS or PAYMENT_FAILED]
                                                                                         │
                                                                                         ▼
                                                                            [StreamConsumer - every 5s]
                                                                                         │
                                                                        ┌────────────────┬──────────────────────┐
                                                                        ▼                ▼                      ▼
                                                                [✔ Processed]   [🔁Retry Queue]  [❌ DLQ (failed after max attempts)]
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
4. StreamConsumer (Scheduled Every 5 Seconds)  
- A scheduled consumer job runs every 5 seconds, using `XREADGROUP` to read new or pending messages from the stream as part of a consumer group.  
- It attempts to process each message accordingly:
    - **✅Processed Successfully**: The consumer handles the message and sends an `XACK` to acknowledge its completion. The message is then removed from the pending list.
    - **🔁Retry Queue**: If processing fails temporarily, the message is **not acknowledged**, allowing it to be retried in the next cycle. If its idle time exceeds a threshold, the consumer can reclaim the message for retry using `XCLAIM`.
    - **❌Dead Letter Queue (DLQ)**: If the message fails after exceeding the maximum delivery attempts, it is moved to a DLQ stream for manual inspection, alerting, or later analysis.
---

## 🤖 Tech Stack
The technology used in this project are:  
- `Spring Boot Starter Web` – Building RESTful APIs or web applications. Used in this project to handle HTTP requests for creating and managing order payments.
- `Spring Data Redis (Lettuce)` – A high-performance Redis client built on Netty. Integrates Redis seamlessly into Spring, allowing the application to produce and consume Redis Streams with ease.
- `RedisTemplate` – A powerful abstraction provided by Spring Data Redis for performing Redis operations, including stream publishing (XADD), consuming (XREADGROUP), acknowledging (XACK), and more.
- `Lombok` – Reducing boilerplate code
---

## 🏗️ Project Structure
The project is organized into the following package structure:  
```bash
redis-stream-producer/
│── src/main/java/com/yoanesber/redis_stream_producer/
│   ├── 📂config/                   # Contains configuration classes, including Redis connection settings.
│   ├── 📂controller/               # Defines REST API endpoints for handling order payment requests, acting as the entry point for client interactions.
│   ├── 📂dto/                      # Contains Data Transfer Objects used for API request and response models, such as creating an order payment.
│   ├── 📂entity/                   # Includes core domain models like Order, OrderDetail, and OrderPayment which represent the message structures.
│   ├── 📂service/                  # Encapsulates the business logic related to order creation and payment processing.
│   │   ├── 📂impl/                 # Implementation of services
│   ├── 📂util/                     # Provides helper utilities and serializers to support common operations such as message transformation.
```
---

## ⚙ Environment Configuration
Configuration values are stored in `.env.development` and referenced in `application.properties`.  
Example `.env.development` file content:  
```properties
# Application properties
APP_PORT=8081
SPRING_PROFILES_ACTIVE=development

# Redis properties
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_USERNAME=default
REDIS_PASSWORD=your_password
REDIS_TIMEOUT=5
REDIS_CONNECT_TIMEOUT=3
REDIS_LETTUCE_SHUTDOWN_TIMEOUT=10
```

Example `application.properties` file content:  
```properties
# Application properties
spring.application.name=redis-stream-producer
server.port=${APP_PORT}
spring.profiles.active=${SPRING_PROFILES_ACTIVE}

# Redis properties
spring.data.redis.host=${REDIS_HOST}
spring.data.redis.port=${REDIS_PORT}
spring.data.redis.username=${REDIS_USERNAME}
spring.data.redis.password=${REDIS_PASSWORD}
spring.data.redis.timeout=${REDIS_TIMEOUT}
spring.data.redis.connect-timeout=${REDIS_CONNECT_TIMEOUT}
spring.data.redis.lettuce.shutdown-timeout=${REDIS_LETTUCE_SHUTDOWN_TIMEOUT}
```
---

## 🛠️ Installation & Setup
A step by step series of examples that tell you how to get a development env running.  
1. Clone the repository  
```bash
git clone https://github.com/yoanesber/Spring-Boot-Redis-Stream-Producer.git
cd Spring-Boot-Redis-Stream-Producer
```

2. Ensure Redis is installed and running:  
```bash
redis-server
```

3. (Optional) If you want to add a specific user with access to a specific channel, you can run the following command in Redis CLI:  
```bash
ACL SETUSER your_user +CHANNEL~your_channel on >your_password
```

4. Set up Redis user and password in `.env.development` file:  
```properties
# Redis properties
REDIS_PASSWORD=your_password
```

5. Build and run the application  
```bash
mvn spring-boot:run
```

6. Use API endpoints to test payment processing. Now, application is available at:  
```bash
http://localhost:8081/
```

---

## 🧪 Testing Scenarios

To ensure that the Redis Stream-based order payment system works reliably under various conditions, several test scenarios were conducted. Below are the descriptions and outcomes of each test scenario along with visual evidence (captured screenshots) to demonstrate the flow and results.  

1. Successful Order Payment Processing  
This scenario tests the normal flow where an order payment is created successfully and published to the Redis stream.  
`POST http://localhost:8081/api/v1/order-payment` - Create a new order payment and trigger payment processing.  

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

---


## 🔗 Related Repositories
- For the Redis Publisher implementation, check out [Spring Boot Redis Publisher with Lettuce](https://github.com/yoanesber/Spring-Boot-Redis-Publisher-Lettuce).
- For the Redis Subscriber implementation, check out [Spring Boot Redis Subscriber with Lettuce](https://github.com/yoanesber/Spring-Boot-Redis-Subscriber-Lettuce).