package ch.asipiit.bankparser;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/donations")
public class DonationController {
    private final CamtProcessingService processingService;
    private final DonationAnalysisService analysisService;

    public DonationController(
            CamtProcessingService processingService,
            DonationAnalysisService analysisService
    ) {
        this.processingService = processingService;
        this.analysisService = analysisService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<DonationAnalysis> analyzeDonations(@RequestParam("file") MultipartFile file) {
        try {
            String xmlContent = new String(file.getBytes(), StandardCharsets.UTF_8);
            List<Transaction> transactions = processingService.processFile(xmlContent);
            DonationAnalysis analysis = analysisService.analyzeDonations(transactions);
            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Failed to process file: " + e.getMessage()
            );
        }
    }

    @GetMapping("/formats")
    public List<String> getSupportedFormats() {
        return processingService.getSupportedFormats();
    }
}