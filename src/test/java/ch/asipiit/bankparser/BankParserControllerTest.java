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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void analyzeSingleFile_WithValidCamt054_ShouldReturnAnalysis() throws Exception {
        // Prepare test data
        String xmlContent = "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:camt.054.001.08\">";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.xml",
                MediaType.TEXT_XML_VALUE,
                xmlContent.getBytes()
        );

        List<Transaction> transactions = Arrays.asList(
                new Transaction("John Doe", LocalDate.now(), new BigDecimal("100.00"), "REF1", TransactionType.CREDIT)
        );

        when(processingService.processFile(any())).thenReturn(transactions);
        when(analysisService.analyzeDonations(any())).thenReturn(createSampleAnalysis());

        mockMvc.perform(multipart("/api/v1/donations/analyze-single").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysis").exists())
                .andExpect(jsonPath("$.fileInfo.fileType").value("CAMT.054"));
    }

    @Test
    void analyzeSingleFile_WithEmptyFile_ShouldReturnBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.xml",
                MediaType.TEXT_XML_VALUE,
                new byte[0]
        );

        mockMvc.perform(multipart("/api/v1/donations/analyze-single").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void analyzeMultipleFiles_WithValidFiles_ShouldReturnCombinedAnalysis() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile(
                "files",
                "test1.xml",
                MediaType.TEXT_XML_VALUE,
                "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:camt.054.001.08\">".getBytes()
        );

        MockMultipartFile file2 = new MockMultipartFile(
                "files",
                "test2.xml",
                MediaType.TEXT_XML_VALUE,
                "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:camt.053.001.08\">".getBytes()
        );

        List<Transaction> transactions = Arrays.asList(
                new Transaction("John Doe", LocalDate.now(), new BigDecimal("100.00"), "REF1", TransactionType.CREDIT),
                new Transaction("Jane Doe", LocalDate.now(), new BigDecimal("200.00"), "REF2", TransactionType.CREDIT)
        );

        when(processingService.processFile(any())).thenReturn(transactions);
        when(analysisService.analyzeDonations(any())).thenReturn(createSampleAnalysis());

        mockMvc.perform(multipart("/api/v1/donations/analyze-multiple")
                        .file(file1)
                        .file(file2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processedFiles").exists())
                .andExpect(jsonPath("$.analysis").exists());
    }

    private DonationAnalysis createSampleAnalysis() {
        List<Transaction> transactions = Arrays.asList(
                new Transaction("John Doe", LocalDate.now(), new BigDecimal("100.00"), "REF1", TransactionType.CREDIT)
        );

        List<DonorSummary> donors = Arrays.asList(
                new DonorSummary(
                        "John Doe",
                        new BigDecimal("100.00"),
                        new BigDecimal("100.00"),
                        false,
                        transactions
                )
        );

        return new DonationAnalysis(donors, new BigDecimal("100.00"), 0, LocalDate.now().atStartOfDay());
    }
}
