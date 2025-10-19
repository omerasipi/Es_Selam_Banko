# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

BankParser is a Spring Boot application that parses ISO 20022 CAMT (Cash Management) XML files and analyzes donation transactions. The application processes CAMT.053 (bank-to-customer statement) and CAMT.054 (debit/credit notification) formats to extract transaction data and generate donation analysis reports.

## Build Commands

**Build the project:**
```bash
./gradlew build
```

**Build without tests:**
```bash
./gradlew build -x test
```

**Run tests:**
```bash
./gradlew test
```

**Run specific test class:**
```bash
./gradlew test --tests "ch.asipiit.bankparser.ClassName"
```

**Run the application:**
```bash
./gradlew bootRun
```

**Build Docker image:**
```bash
docker build -t bank-parser .
```

**Run Docker container:**
```bash
docker run -p 8080:8080 bank-parser
```

## Architecture Overview

### CAMT Processing Pipeline

The application uses a **strategy pattern** for processing different CAMT formats:

1. **CamtProcessor Interface** (`CamtProcessor.java`): Defines the contract for all CAMT processors
   - `canProcess(String xmlContent)`: Determines if processor can handle the format
   - `processTransactions(String xmlContent)`: Extracts transactions from XML
   - `getFormatVersion()`: Returns the supported format version

2. **CamtProcessingService** (`CamtProcessingService.java`): Orchestrates CAMT processing
   - Spring autowires all `CamtProcessor` implementations
   - Iterates through processors to find one that can handle the file
   - Returns list of `Transaction` objects

3. **Format-Specific Processors**:
   - `Camt05300108Processor`: Processes CAMT.053.001.08 (bank statements)
   - `Camt05400108Processor`: Processes CAMT.054.001.08 (debit/credit notifications)
   - Both use the **Prowide ISO20022 library** for XML parsing
   - Extract: debtor name, date, amount, reference, transaction type

### Donation Analysis Pipeline

1. **DonationAnalysisService** (`DonationAnalysisService.java`):
   - Filters credit transactions (donations)
   - Groups by donor name
   - Calculates monthly averages and identifies donors below minimum threshold (30.00)
   - Returns `DonationAnalysis` with `DonorSummary` objects

2. **Domain Models**:
   - `Transaction`: Immutable record with debtorName, date, amount, reference, type
   - `TransactionType`: Enum (CREDIT, DEBIT)
   - `DonorSummary`: Aggregated donation data per donor
   - `DonationAnalysis`: Complete analysis with totals and donor summaries

### API Endpoints

**Two controllers expose different API versions:**

1. **BankParserController** (`/api`):
   - `POST /api/analyze`: Single file donation analysis
   - `POST /api/validate`: Validate CAMT file format
   - `GET /api/formats`: List supported formats
   - `GET /api/health`: Health check

2. **DonationController** (`/api/v1`):
   - `POST /api/v1/donations/analyze-single`: Single file analysis
   - `POST /api/v1/donations/analyze-multiple`: Multi-file batch processing
   - `POST /api/v1/donations/validate`: Validate file
   - `GET /api/v1/formats`: List supported formats
   - `GET /api/v1/donations/report`: TODO - not yet implemented

### Adding New CAMT Format Support

To add support for a new CAMT format:

1. Create a new processor class implementing `CamtProcessor<T>`
2. Annotate with `@Component` for Spring auto-detection
3. Define the format URN constant (e.g., `urn:iso:std:iso:20022:tech:xsd:camt.XXX.001.XX`)
4. Implement `canProcess()` to detect the format by URN
5. Implement `processTransactions()` using appropriate Prowide MX class
6. Map extracted data to `Transaction` objects

Spring will automatically register the processor and `CamtProcessingService` will use it.

## Configuration

- **application.properties**: Main config with CORS settings for production (https://bank.es-selam.ch)
- **application-docker.yml**: Docker-specific config with context path `/api` and logging
- Max file upload size: 50MB
- Server port: 8080
- Java version: 23 (Amazon Corretto)
- Gradle version: 8.5

## Test Resources

Sample CAMT files are located in `src/test/resources/`:
- `test-camt053.xml`: CAMT.053 test file
- `test-camt054.xml`: CAMT.054 test file

Use these files when writing integration tests or manual testing.

## Key Dependencies

- **Spring Boot 3.3.5**: Web framework
- **Prowide ISO20022 (pw-iso20022)**: Parses CAMT XML using MX classes
- **Jakarta XML Binding (JAXB)**: XML marshalling/unmarshalling
- **JUnit 5**: Testing framework
- **Mockito**: Mocking framework for unit tests
