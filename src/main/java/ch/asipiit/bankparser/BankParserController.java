package ch.asipiit.bankparser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class BankParserController {

    private final CamtProcessingService processingService;
    private final DonationAnalysisService analysisService;

    @Autowired
    public BankParserController(CamtProcessingService processingService, DonationAnalysisService analysisService) {
        this.processingService = processingService;
        this.analysisService = analysisService;
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Service is running");
    }

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> analyzeDonations(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        try {
            String xmlContent = new String(file.getBytes(), StandardCharsets.UTF_8);

            // Process the file
            var transactions = processingService.processFile(xmlContent);
            var analysis = analysisService.analyzeDonations(transactions);

            Map<String, Object> response = new HashMap<>();
            response.put("analysis", analysis);
            response.put("fileName", file.getOriginalFilename());
            response.put("fileSize", file.getSize());
            response.put("transactionsProcessed", transactions.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to process file");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    @GetMapping("/formats")
    public ResponseEntity<?> getSupportedFormats() {
        try {
            return ResponseEntity.ok(processingService.getSupportedFormats());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve supported formats");
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        try {
            String xmlContent = new String(file.getBytes(), StandardCharsets.UTF_8);
            boolean isValid = processingService.canProcessFile(xmlContent);

            Map<String, Object> response = new HashMap<>();
            response.put("isValid", isValid);
            response.put("fileName", file.getOriginalFilename());
            response.put("fileSize", file.getSize());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to validate file");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Internal server error");
        errorResponse.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }
}