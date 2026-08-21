Write-Host "=============================================================" -ForegroundColor Cyan
Write-Host "         Starting Graph Database Benchmark Harness           " -ForegroundColor Cyan
Write-Host "=============================================================" -ForegroundColor Cyan

Write-Host "`n[1/3] Starting local databases (Memgraph, FalkorDB, ArangoDB)..." -ForegroundColor Green
if (Get-Command docker-compose -ErrorAction SilentlyContinue) {
    docker-compose up -d
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Warning: docker-compose up failed. Proceeding." -ForegroundColor Yellow
    } else {
        Write-Host "Waiting 5 seconds for local database engines to initialize..." -ForegroundColor DarkGray
        Start-Sleep -Seconds 5
    }
} else {
    Write-Host "Warning: Docker Compose was not found." -ForegroundColor Yellow
}

Write-Host "`n[2/3] Compiling and packaging Java application..." -ForegroundColor Green
if (Get-Command mvn -ErrorAction SilentlyContinue) {
    mvn clean package
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Error: Maven build failed." -ForegroundColor Red
        Exit 1
    }
} else {
    Write-Host "Error: Maven (mvn) was not found." -ForegroundColor Red
    Exit 1
}

Write-Host "`n[3/3] Running Graph Database Benchmarks..." -ForegroundColor Green
if (Test-Path "target/graph-benchmark-1.0-SNAPSHOT-jar-with-dependencies.jar") {
    java -jar target/graph-benchmark-1.0-SNAPSHOT-jar-with-dependencies.jar
} else {
    Write-Host "Error: Compiled JAR file was not found." -ForegroundColor Red
    Exit 1
}

Write-Host "`n=============================================================" -ForegroundColor Cyan
Write-Host "Benchmark Run Completed!" -ForegroundColor Cyan
Write-Host "Open 'index.html' in your browser to view the interactive dashboard." -ForegroundColor Green
Write-Host "=============================================================" -ForegroundColor Cyan
