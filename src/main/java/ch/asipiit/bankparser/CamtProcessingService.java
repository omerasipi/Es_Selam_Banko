package ch.asipiit.bankparser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CamtProcessingService {
    private final List<CamtProcessor<?>> processors;

    @Autowired
    public CamtProcessingService(List<CamtProcessor<?>> processors) {
        this.processors = processors;
    }

    public List<Transaction> processFile(String xmlContent) {
        return processors.stream()
                .filter(processor -> processor.canProcess(xmlContent))
                .findFirst()
                .map(processor -> processor.processTransactions(xmlContent))
                .orElseThrow(() -> new UnsupportedCamtFormatException("No processor found for this CAMT format"));
    }

    public boolean canProcessFile(String xmlContent) {
        return processors.stream()
                .anyMatch(processor -> processor.canProcess(xmlContent));
    }

    public List<String> getSupportedFormats() {
        return processors.stream()
                .map(CamtProcessor::getFormatVersion)
                .collect(Collectors.toList());
    }
}