<h1 align="center">⚠️ Fraud Detection Stream Processor</h1>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-blue?style=for-the-badge&logo=openjdk" />
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.8-F?style=for-the-badge&logo=springboot" />
  <img src="https://img.shields.io/badge/Apache%20Kafka-Event%20Streaming-white?style=for-the-badge&logo=apachekafka" />
  <img src="https://img.shields.io/badge/CI-Build%20Passing-fdff00?style=for-the-badge&logo=githubactions" />
  <img src="https://img.shields.io/badge/Status-Running-brightgreen?style=for-the-badge&logo=ticktick" />
</p>

<p align="center">
  <strong>A modular, real-time FinTech Fraud Detection Service built using Kafka Streams, Rule Engine, Synthetic Data Generator and Event-driven Microservices, over a Modular Monolith Architecture.</strong>
</p>

---

## 🚀 Overview

The **Fraud Detection Stream Processor** is a modular, event-driven FinTech system designed to efficiently analyze high-volume transactional data in real time. It provides a scalable and extensible foundation for building fraud-analysis pipelines commonly used in modern payment processing platforms.

This system is capable of:

- Ingesting and processing **high-throughput synthetic transactions** (500+ TPS benchmark)
- Evaluating incoming events through a **real-time fraud assessment workflow**
- Applying a **pluggable Fraud Rule Engine** with configurable rule sets
- Computing fraud scores and enriching transactions with additional metadata
- Emitting processed events and generating alerts via a dedicated **Alert Service**
- Scaling horizontally through Kafka consumer groups for distributed processing

Built with a focus on clarity, maintainability, and production alignment, the project demonstrates how real-time fraud detection systems are architected in large-scale financial environments.

---

## 🚚 Migration Notes

This repository is a **clean, intentionally refactored migration** of an older codebase that has since been deprecated and deleted. The migration was intentional and performed to achieve:

- Improved overall architecture and code readability
- Removal of outdated or experimental components
- Consistent naming conventions and modern best practices
- A cleaner and more maintainable project structure
- A stronger foundation for future enhancements and scalability

While the project’s core purpose remains the same, the entire codebase has been **modernized and reorganized intentionally** to ensure better clarity, higher code quality, and long-term maintainability.

---

## 🧱 **System Architecture**

    ┌─────────────────────────┐
    │   Synthetic Generator   │
           ----------
    │ HTTP controlled         │
    │ Generates → tx stream   │
    └───────────────┬─────────┘
                    |
    │ Kafka Topic: transactions.in
                    |
                    ▼
    ┌─────────────────────────┐
    │ Fraud Stream Processor  │
            ----------
    │ - Kafka Consumer        │
    │ - Calls Rule Engine     │ ──────────────────────────────────┐
    │ - Scores + Enriches     │                                   │
    │ - Publishes alerts      │                                   │
    └───────────────┬─────────┘                                   │
                    |                                             │
    │ Kafka Topic: transactions.out                               │
                    |                                             │
                    ▼                                             │
    ┌─────────────────────────┐                                   │
    │     Alert Service       │                                   ⇵
            ----------                                            │
    │ - Consumes fraud tx     │                                   │
    │ - Endpoint notifications│                                   │
    └─────────────────────────┘                                   │
                                                                  │
    ┌─────────────────────────┐                                   │
    │    Fraud Rule Engine    │                                   │
             ----------                                           │
    │ - Scoring model         │ ──────────────────────────────────┘
    │ - Pluggable rules       │
    └─────────────────────────┘

---

## 🧩 **Modules Breakdown**

### **1. `common/`**
A common lib of Shared DTOs:
- `Transaction`
- `GeoLocation`
- `FraudLabel`

---

### **2. `synthetic-generator/`**
A highly configurable synthetic event generator:

#### REST Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/generator/start` | Start generating transactions |
| POST | `/api/generator/stop` | Stop generator |
| POST | `/api/generator/setRate?eps=1000` | Dynamically change TPS |
| GET | `/api/generator/status` | Current EPS |

#### Features
- Randomized realistic fields
- Probabilistic fraud injection
- High-volume Kafka publishing (`transactions.in`)

---

### **3. `fraud-rule-engine/`**
A standalone scoring engine with configurable rule weights:

#### Built-in Rules
- Device Change Rule
- Geo-Velocity Rule
- Rapid-fire Transaction Rule
- MCC Risk Score Rule
- Amount Spike Rule
- Night-out Spending Rule

#### Configurable via YAML
```yaml
fraud.engine.threshold: 50.0
fraud.engine.aggregate-mode: sum
fraud.engine.rules.DeviceChangeSuspiciousRule.weight: 1.5

Exposes:
  POST /api/engine/score      
  POST /api/engine/evaluate   
```
---
### **4. `fraud-stream-processor/`**

Real-time processing microservice:

> ✔ Consumes from transactions.in  
> ✔ Calls Rule Engine (HTTP)  
> ✔ Produces enriched results → transactions.out  
> ✔ Adds fraud label with reason  
> ✔ Logging & error-safe processing
---
### **5. `alert-service/`**

Consumes only fraudulent transactions:

> Listens to transactions.out
>
> Filters → label.isFraud == true

Future upgrades:
- Email/SMS 
- Dashboard
- Slack/Teams alerts
- Persistence
---
## 📦 Tech Stack

| Layer     | Tech                 |
|-----------|----------------------|
| Language  | `Java 21`            |
| Framework | `Spring Boot 3.5.8`  |
| Messaging | `Kafka`              |
| caching   | `Caffeine`           |
| CI | `GitHub Actions`            |
---
## ▶️ Running the System
1. Start Kafka (docker-compose)
```
   docker-compose up -d zookeeper kafka
```

2. Build all modules
```
   mvn clean package -DskipTests
```
3. Run Services (fat jars)
```
   java -jar synthetic-generator/target/synthetic-generator-0.1.0-SNAPSHOT.jar
   java -jar fraud-rule-engine/target/fraud-rule-engine-0.1.0-SNAPSHOT.jar
   java -jar fraud-stream-processor/target/fraud-stream-processor-0.1.0-SNAPSHOT.jar
   java -jar alert-service/target/alert-service-0.1.0-SNAPSHOT.jar
```
---
## 📡 Kafka Topics
|Topic	| Purpose |
|-------|---------|
|transactions.in |	Raw synthetic transactions|
|transactions.out|	Enriched + scored transactions (fraud flag included)|
---
## 🕵️ Fraud Detection Flow

- Synthetic Generator → Kafka (`transactions.in`)

- Stream Processor consumes & transforms

- Rule Engine scores the transaction

- Stream Processor emits enriched transaction

- Alert Service consumes → triggers downstream actions
---
## 🧪 Sample Enriched Output (from transactions.out)
```json
{
"transactionId": "81751271-5e32-44c6-a8b8-2f7d3787253f",
"amount": 68.46,
"currency": "INR",
"label": {
    "isFraud": false,
    "reason": "score=5.00 threshold=50.00"
  }
}
```
## 👤 Author
This system was designed, engineered and implemented by **Vishwas Karode (@vishwasio)**.  

Issues & PRs are welcome. [git clone -> branch -> PR]
```
git clone https://github.com/vishwasio/fraud-detection-stream-processor.git

```

---

## 🔚 Final Remarks

Thank you for taking the time to review this project.  
If you'd like to follow its progress or support future updates, please consider **⭐ starring**, **👀 watching**, or **🍴 forking** the repository.  
More enhancements, refinements, and production-grade improvements are on the way.

Happy Coding! 😊
