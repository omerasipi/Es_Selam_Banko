package ch.asipiit.bankparser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BankParserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CamtProcessingService processingService;

    @Mock
    private DonationAnalysisService analysisService;

    @InjectMocks
    private BankParserController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void healthCheck_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Service is running"));
    }

    @Test
    void analyzeDonations_WithValidFile_ShouldReturnAnalysis() throws Exception {
        // Prepare test data
        String xmlContent = "<xml>test</xml>";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.xml",
                MediaType.TEXT_XML_VALUE,
                xmlContent.getBytes()
        );

        List<Transaction> transactions = Arrays.asList(
                new Transaction("John Doe", LocalDate.now(), new BigDecimal("100.00"), "REF1", TransactionType.CREDIT),
                new Transaction("Jane Doe", LocalDate.now(), new BigDecimal("200.00"), "REF2", TransactionType.CREDIT)
        );

        DonationAnalysis analysis = new DonationAnalysis(
                Arrays.asList(new DonorSummary("John Doe", new BigDecimal("100.00"), new BigDecimal("100.00"), false, transactions)),
                new BigDecimal("300.00"),
                0,
                LocalDate.now()
        );

        when(processingService.processFile(any())).thenReturn(transactions);
        when(analysisService.analyzeDonations(any())).thenReturn(analysis);

        mockMvc.perform(multipart("/api/analyze").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysis").exists())
                .andExpect(jsonPath("$.fileName").value("test.xml"))
                .andExpect(jsonPath("$.transactionsProcessed").value(2));
    }

    @Test
    void analyzeDonations_WithEmptyFile_ShouldReturnBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.xml",
                MediaType.TEXT_XML_VALUE,
                new byte[0]
        );

        mockMvc.perform(multipart("/api/analyze").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("File is empty"));
    }

    @Test
    void validateFile_WithValidFile_ShouldReturnValidationResult() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.xml",
                MediaType.TEXT_XML_VALUE,
                "<xml>test</xml>".getBytes()
        );

        when(processingService.canProcessFile(any())).thenReturn(true);

        mockMvc.perform(multipart("/api/validate").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isValid").value(true))
                .andExpect(jsonPath("$.fileName").value("test.xml"));
    }
}