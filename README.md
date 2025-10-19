# BankParser

A Spring Boot application for parsing ISO 20022 CAMT (Cash Management) XML files and analyzing donation transactions.

## Overview

BankParser processes bank statement files in CAMT format and generates donation analysis reports. It supports multiple CAMT formats and can process single or multiple files to identify donors, calculate monthly averages, and flag donors below minimum donation thresholds.

## Features

- **Multi-Format Support**: Processes CAMT.053 and CAMT.054 XML formats
- **Donation Analysis**: Groups transactions by donor and calculates statistics
- **Monthly Averaging**: Calculates average monthly donations per donor
- **Threshold Detection**: Identifies donors below minimum monthly donation (30.00)
- **Batch Processing**: Supports analyzing multiple files simultaneously
- **File Validation**: Validates CAMT files before processing
- **RESTful API**: Easy-to-use HTTP endpoints
- **Docker Support**: Containerized deployment ready

## Prerequisites

- Java 23 or higher
- Gradle 8.5 or higher (or use included wrapper)
- Docker (optional, for containerized deployment)

## Installation

### Clone the repository

```bash
git clone <repository-url>
cd bankParser
```

### Build the project

```bash
./gradlew build
```

### Run the application

```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`

## API Endpoints

### V1 API (`/api/v1`)

#### Analyze Single File
```http
POST /api/v1/donations/analyze-single
Content-Type: multipart/form-data

file: <CAMT XML file>
```

**Response:**
```json
{
  "analysis": {
    "donorSummaries": [...],
    "totalDonations": "1234.56",
    "donorsBelowMinimum": 5,
    "analysisDate": "2024-11-07T10:30:00"
  },
  "fileInfo": {
    "fileName": "statement.xml",
    "fileSize": 12345,
    "fileType": "CAMT.053"
  },
  "transactionsProcessed": 42
}
```

#### Analyze Multiple Files
```http
POST /api/v1/donations/analyze-multiple
Content-Type: multipart/form-data

files: <CAMT XML file 1>
files: <CAMT XML file 2>
...
```

#### Validate File
```http
POST /api/v1/donations/validate
Content-Type: multipart/form-data

file: <CAMT XML file>
```

#### Get Supported Formats
```http
GET /api/v1/formats
```

**Response:**
```json
["053.001.08", "054.001.08"]
```

### Legacy API (`/api`)

Similar endpoints available at `/api/analyze`, `/api/validate`, and `/api/formats`

## Docker Deployment

### Build Docker image

```bash
docker build -t bank-parser .
```

### Run container

```bash
docker run -p 8080:8080 bank-parser
```

### Using Docker Compose (example)

```yaml
version: '3.8'
services:
  bank-parser:
    build: .
    ports:
      - "8080:8080"
    environment:
      - JAVA_OPTS=-Xmx512m
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/api/v1/health"]
      interval: 30s
      timeout: 3s
      retries: 3
```

## Configuration

### Application Properties

Located in `src/main/resources/application.properties`:

```properties
spring.application.name=bankParser
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB

# CORS configuration
spring.web.cors.allowed-origin-patterns=https://bank.es-selam.ch
spring.web.cors.allowed-methods=GET,POST,PUT,DELETE
spring.web.cors.allowed-headers=*
spring.web.cors.allow-credentials=true
```

### Docker Configuration

When running in Docker, the application uses `application-docker.yml` with:
- Context path: `/api`
- Enhanced logging to `/app/logs/bankparser.log`
- Port: 8080

## Development

### Run tests

```bash
./gradlew test
```

### Run specific test class

```bash
./gradlew test --tests "ch.asipiit.bankparser.DonationAnalysisServiceTest"
```

### Build without tests

```bash
./gradlew build -x test
```

## Architecture

The application uses a strategy pattern for CAMT processing:

1. **CamtProcessor Interface**: Defines contract for format processors
2. **Format-Specific Processors**: Implement parsing logic for each CAMT version
3. **CamtProcessingService**: Orchestrates processor selection
4. **DonationAnalysisService**: Analyzes extracted transactions

For detailed architecture information, see [CLAUDE.md](CLAUDE.md).

## Supported CAMT Formats

- **CAMT.053.001.08**: Bank-to-customer account statement
- **CAMT.054.001.08**: Bank-to-customer debit/credit notification

## Technology Stack

- **Java 23** (Amazon Corretto)
- **Spring Boot 3.3.5**
- **Prowide ISO20022** (pw-iso20022) - CAMT XML parsing
- **Jakarta XML Binding (JAXB)** - XML processing
- **Gradle 8.5** - Build tool
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework

## License

[Add your license information here]

## Contributing

[Add contribution guidelines here]

## Support

For issues and questions, please [create an issue](../../issues) in this repository.
