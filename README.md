# Graph Database Cloud Benchmarking Suite

## Overview

The **Graph Database Cloud Benchmarking Suite** is a Java-based benchmarking application designed to evaluate and compare the performance of multiple graph database platforms using the same dataset and standardized workloads.

The goal of the project is to provide a consistent benchmarking methodology across different graph database systems and collect performance measurements that can be compared using throughput and latency metrics.

## Objectives

* Benchmark multiple graph database platforms using an identical dataset.
* Measure database data-loading performance.
* Measure read and traversal workload performance.
* Execute workloads repeatedly after a warm-up phase.
* Report latency using percentiles rather than averages alone.
* Compare database performance using consistent workloads and configurations.
* Support both local Docker-based and cloud database environments where applicable.

---

# Databases

The benchmarking suite is designed to support the following platforms:

* **Neo4j**
* **Memgraph**
* **FalkorDB**
* **ArangoDB**
* **CognoDB**

Each database is accessed through its appropriate driver, client, or API.

---

# Benchmark Requirements

## Dataset

The benchmark uses a public graph dataset containing more than 100,000 relationships.

### Dataset Used

**Stanford SNAP Wiki-Vote Network**

Dataset source:

**Stanford Network Analysis Project (SNAP)**

Dataset characteristics used by this project:

| Property      |   Value |
| ------------- | ------: |
| Nodes         |   7,115 |
| Relationships | 103,689 |

The same logical dataset is used across the supported database platforms to ensure that benchmark results are comparable.

The dataset is intentionally kept within the approximate **100,000–500,000 relationship** range so that it remains practical for free-tier and local development environments.

> The dataset file is not committed to this repository. It should be downloaded separately during project setup.

---

# Dataset Loading

The benchmark requires the identical dataset to be loaded into each database platform.

The project uses database-specific loading approaches depending on the platform, including driver-based insertion/batching and database APIs where applicable.

The loading methodology is recorded for each database so that the ingestion results can be compared consistently.

### Data Loading Metrics

The benchmark measures:

* Total wall-clock loading time
* Nodes loaded per second
* Relationships loaded per second

The general throughput calculations are:

```text
Node Throughput =
Number of Nodes / Total Loading Time

Relationship Throughput =
Number of Relationships / Total Loading Time
```

---

# Required Benchmark Metrics

The benchmark measures performance across multiple categories.

## Data Loading

| Metric            | Measurements          |
| ----------------- | --------------------- |
| Ingest throughput | Nodes/second          |
| Ingest throughput | Relationships/second  |
| Loading time      | Total wall-clock time |

## Read Workloads

The benchmark includes graph query workloads such as:

* Point lookup
* Indexed lookup
* 1-hop traversal
* 2-hop traversal
* 3-hop traversal
* Aggregation queries

Each read workload should be executed repeatedly after a warm-up phase.

The recommended benchmark configuration is:

```text
Warm-up iterations: configurable
Measured iterations: 100 or more
```

For measured workloads, latency should be reported using percentiles such as:

```text
P50
P95
P99
```

This provides a more reliable representation of database performance than reporting an average alone.

---

# Benchmark Methodology

To maintain consistency between platforms, the benchmark follows these principles:

1. Use the same dataset for every database.
2. Use equivalent graph operations across platforms.
3. Perform warm-up executions before collecting measurements.
4. Execute each read workload repeatedly.
5. Record individual execution times.
6. Calculate latency percentiles.
7. Measure data-loading wall-clock time.
8. Calculate node and relationship ingestion throughput.
9. Record successful and failed operations.
10. Keep database and benchmark configuration documented.

---

# Workloads

## Point Lookup

Retrieves a specific node or entity using a unique identifier.

Purpose:

* Measure basic node lookup performance.
* Evaluate indexed or key-based access.

## Indexed Lookup

Retrieves nodes using an indexed property.

Purpose:

* Evaluate index-based query performance.

## 1-Hop Traversal

Retrieves directly connected neighboring nodes.

Purpose:

* Measure basic graph traversal performance.

## 2-Hop Traversal

Traverses two relationship levels from a starting node.

Purpose:

* Evaluate multi-hop traversal performance.

## 3-Hop Traversal

Traverses three relationship levels from a starting node.

Purpose:

* Evaluate deeper graph traversal performance.

## Aggregation

Performs aggregation operations over graph data.

Examples include:

* Counting relationships
* Counting connected nodes
* Grouping graph data

Purpose:

* Evaluate aggregation and analytical query performance.

## Write Operations

Measures database performance when inserting graph data.

Purpose:

* Evaluate write and ingestion behavior.

---

# Performance Metrics

The benchmark focuses on both throughput and latency.

### Throughput

Throughput measures how much work the database can complete within a given amount of time.

Examples:

```text
Nodes / second
Relationships / second
Operations / second
```

### Latency

Latency measures how long an individual operation takes to complete.

The benchmark reports:

```text
P50 - Median latency

P95 - 95th percentile latency

P99 - 99th percentile latency
```

Percentile-based reporting helps identify both typical and high-latency executions.

---

# Technology Stack

* **Java**
* **Maven**
* **Docker**
* **Git**
* **GitHub**
* Database-specific Java drivers/APIs
* JSON-based result processing

---

# Project Structure

```text
graph-database-cloud-benchmarking-suite/
│
├── src/
│   └── main/
│       └── java/
│           └── ...
│
├── pom.xml
├── docker-compose.yml
├── .gitignore
└── README.md
```

---

# Prerequisites

Install the following before running the project:

* Java 17 or compatible Java version
* Maven
* Docker Desktop
* Git
* Eclipse IDE or another Java IDE

Database-specific credentials may be required for cloud database environments.

---

# Installation

## Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/graph-database-cloud-benchmarking-suite.git
```

Navigate into the project:

```bash
cd graph-database-cloud-benchmarking-suite
```

## Build the Project

Using Maven:

```bash
mvn clean install
```

---

# Running Local Databases

For databases configured through Docker Compose:

```bash
docker compose up -d
```

Verify that the required containers are running:

```bash
docker ps
```

Database-specific configuration should be provided according to the project's configuration.

---

# Dataset Setup

Download the Wiki-Vote dataset from the official Stanford SNAP dataset collection.

The downloaded dataset should be placed in the location expected by the benchmark configuration.

The dataset is intentionally excluded from GitHub to avoid committing large generated/input data files.

---

# Configuration

Database connection details should be configured locally.

Sensitive information must not be committed to GitHub.

Do not commit:

```text
Passwords
API keys
Access tokens
Cloud credentials
Private connection strings
.env files
```

Use environment variables or local configuration files for sensitive settings.

---

# Benchmark Execution

Start the required database services and configure the corresponding database connection.

Then run the benchmark application from Eclipse or through Maven.

The benchmark executes the configured workloads and records their execution performance.

Generated results may include:

* Query execution time
* Loading time
* Throughput
* Percentile latency
* Successful operations
* Failed operations

---

# Results

Benchmark results should be reported for each database using the same workload and dataset.

A typical result format is:

| Database | Workload        | P50 | P95 | P99 |
| -------- | --------------- | --: | --: | --: |
| Memgraph | Point Lookup    |   - |   - |   - |
| Memgraph | 1-Hop Traversal |   - |   - |   - |
| ArangoDB | Point Lookup    |   - |   - |   - |
| ArangoDB | 1-Hop Traversal |   - |   - |   - |

Data-loading results should include:

| Database | Nodes/sec | Relationships/sec | Total Time |
| -------- | --------: | ----------------: | ---------: |
| Memgraph |         - |                 - |          - |
| ArangoDB |         - |                 - |          - |
| Neo4j    |         - |                 - |          - |
| FalkorDB |         - |                 - |          - |
| CognoDB  |         - |                 - |          - |

Values should be populated from actual benchmark executions.

---

# Current Project Status

The benchmarking framework has been implemented for multiple graph database platforms.

Local Docker-based testing has been performed for supported databases where the required services are available.

Some database platforms may require additional configuration, credentials, or environment-specific setup before complete benchmark execution.

The benchmark results should be considered complete only after all required workloads and metrics have been successfully executed on every target platform.

---

# Limitations

* Database performance can vary depending on hardware and environment.
* Cloud database performance depends on network latency and service configuration.
* Free-tier database limitations may affect benchmark execution.
* Database-specific query languages and capabilities may require equivalent implementations rather than identical query syntax.
* Results are meaningful only when the same dataset, workload, and benchmark methodology are used consistently.

---

# Future Improvements

* Automated percentile calculation and reporting.
* Automated benchmark result visualization.
* HTML-based performance reports.
* Automated comparison between database platforms.
* CI/CD integration.
* Additional graph database platforms.
* Improved cloud configuration management.
* Automated dataset loading.
* More comprehensive workload scenarios.

---

# Security

No passwords, API keys, tokens, or cloud credentials should be stored in the source code or committed to GitHub.

Sensitive configuration should be provided through environment variables or local configuration files.

The `.gitignore` file is configured to exclude common environment and generated files.

---

# Author

**Sasi Kiran**

Java Backend Developer

---

# Project Purpose

This project was developed as a **graph database benchmarking assignment** to study and compare the performance characteristics of multiple graph database platforms using a standardized dataset and benchmarking methodology.

# Submission

This project was completed as part of the **Wexa AI Candidate Take-Home Assignment**.

The completed source code is maintained in a GitHub repository.

## Submission Requirements

* GitHub repository containing the completed project.
* Repository URL submitted to the designated HR contact.
* Assignment submission subject format:

```text
CognoDB Assignment 1 - sasikiran
```

## Security and Credentials

**No database passwords, API keys, access tokens, or private connection URIs are included in this repository.**

Database credentials and connection details must be provided through environment variables or local configuration.

For example:

```text
NEO4J_URI
NEO4J_USERNAME
NEO4J_PASSWORD

ARANGODB_URI
ARANGODB_USERNAME
ARANGODB_PASSWORD

COGNODB_URI
COGNODB_USERNAME
COGNODB_PASSWORD
```



