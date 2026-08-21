#!/bin/bash
set -e

CYAN='\033[0;36m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${CYAN}=============================================================${NC}"
echo -e "${CYAN}         Starting Graph Database Benchmark Harness           ${NC}"
echo -e "${CYAN}=============================================================${NC}"

echo -e "\n${GREEN}[1/3] Starting local databases (Memgraph, FalkorDB, ArangoDB)...${NC}"
if command -v docker-compose &> /dev/null; then
    docker-compose up -d
    echo -e "Waiting 5 seconds for local database engines to initialize..."
    sleep 5
else
    echo -e "${YELLOW}Warning: Docker Compose was not found.${NC}"
fi

echo -e "\n${GREEN}[2/3] Compiling and packaging Java application...${NC}"
if command -v mvn &> /dev/null; then
    mvn clean package
else
    echo -e "${RED}Error: Maven (mvn) was not found.${NC}"
    exit 1
fi

echo -e "\n${GREEN}[3/3] Running Graph Database Benchmarks...${NC}"
if [ -f "target/graph-benchmark-1.0-SNAPSHOT-jar-with-dependencies.jar" ]; then
    java -jar target/graph-benchmark-1.0-SNAPSHOT-jar-with-dependencies.jar
else
    echo -e "${RED}Error: Compiled JAR file was not found.${NC}"
    exit 1
fi

echo -e "\n${CYAN}=============================================================${NC}"
echo -e "${CYAN}Benchmark Run Completed!${NC}"
echo -e "${GREEN}Open 'index.html' in your browser to view the interactive dashboard.${NC}"
echo -e "${CYAN}=============================================================${NC}"
