package ch.asipiit.bankparser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class DonationController {

    private final CamtProcessingService processingService;
    private final DonationAnalysisService analysisService;

    @Autowired
    public DonationController(CamtProcessingService processingService, DonationAnalysisService analysisService) {
        this.processingService = processingService;
        this.analysisService = analysisService;
    }

    @PostMapping(value = "/donations/analyze-single", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> analyzeSingleFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(createErrorResponse("File is empty"));
        }

        try {
            String xmlContent = new String(file.getBytes(), StandardCharsets.UTF_8);
            String fileType = determineFileType(xmlContent);

            var transactions = processingService.processFile(xmlContent);
            var analysis = analysisService.analyzeDonations(transactions);

            Map<String, Object> response = new HashMap<>();
            response.put("analysis", analysis);
            response.put("fileInfo", createFileInfo(file, fileType));
            response.put("transactionsProcessed", transactions.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to process file: " + e.getMessage()));
        }
    }

    @PostMapping(value = "/donations/analyze-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> analyzeMultipleFiles(@RequestParam("files") List<MultipartFile> files) {
        if (files.isEmpty()) {
            return ResponseEntity.badRequest().body(createErrorResponse("No files provided"));
        }

        try {
            List<Transaction> allTransactions = new ArrayList<>();
            List<Map<String, Object>> processedFiles = new ArrayList<>();

            for (MultipartFile file : files) {
                String xmlContent = new String(file.getBytes(), StandardCharsets.UTF_8);
                String fileType = determineFileType(xmlContent);

                var transactions = processingService.processFile(xmlContent);
                allTransactions.addAll(transactions);

                Map<String, Object> fileInfo = createFileInfo(file, fileType);
                fileInfo.put("transactionsFound", transactions.size());
                processedFiles.add(fileInfo);
            }

            var analysis = analysisService.analyzeDonations(allTransactions);

            Map<String, Object> response = new HashMap<>();
            response.put("analysis", analysis);
            response.put("processedFiles", processedFiles);
            response.put("totalTransactionsProcessed", allTransactions.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to process files: " + e.getMessage()));
        }
    }

    @PostMapping("/donations/validate")
    public ResponseEntity<?> validateFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(createErrorResponse("File is empty"));
        }

        try {
            String xmlContent = new String(file.getBytes(), StandardCharsets.UTF_8);
            String fileType = determineFileType(xmlContent);
            boolean isValid = processingService.canProcessFile(xmlContent);

            Map<String, Object> response = new HashMap<>();
            response.put("isValid", isValid);
            response.put("fileInfo", createFileInfo(file, fileType));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to validate file: " + e.getMessage()));
        }
    }

    @GetMapping("/donations/report")
    public ResponseEntity<?> getDonationReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        // TODO: Implement donation reporting with date ranges
        return ResponseEntity.ok().build();
    }

    @GetMapping("/formats")
    public ResponseEntity<?> getSupportedFormats() {
        try {
            return ResponseEntity.ok(processingService.getSupportedFormats());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to retrieve supported formats"));
        }
    }

    private String determineFileType(String xmlContent) {
        if (xmlContent.contains("camt.053.001.08")) {
            return "CAMT.053";
        } else if (xmlContent.contains("camt.054.001.08")) {
            return "CAMT.054";
        }
        return "Unknown";
    }

    private Map<String, Object> createFileInfo(MultipartFile file, String fileType) {
        Map<String, Object> fileInfo = new HashMap<>();
        fileInfo.put("fileName", file.getOriginalFilename());
        fileInfo.put("fileSize", file.getSize());
        fileInfo.put("fileType", fileType);
        return fileInfo;
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Internal server error: " + e.getMessage()));
    }
}