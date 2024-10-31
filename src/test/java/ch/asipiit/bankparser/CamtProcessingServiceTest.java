package ch.asipiit.bankparser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CamtProcessingServiceTest {

    @Mock
    private Camt05300108Processor camt053Processor;

    @Mock
    private Camt05400108Processor camt054Processor;

    private CamtProcessingService service;

    @BeforeEach
    void setUp() {
        service = new CamtProcessingService(Arrays.asList(camt053Processor, camt054Processor));
    }

    @Test
    void processFile_WithCamt053_ShouldReturnTransactions() {
        String xmlContent = "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:camt.053.001.08\">";
        List<Transaction> expectedTransactions = Arrays.asList(
                new Transaction("John Doe", LocalDate.now(), new BigDecimal("100.00"), "REF1", TransactionType.CREDIT)
        );

        when(camt053Processor.canProcess(xmlContent)).thenReturn(true);
        when(camt053Processor.processTransactions(xmlContent)).thenReturn(expectedTransactions);

        List<Transaction> result = service.processFile(xmlContent);

        assertThat(result).isEqualTo(expectedTransactions);
    }

    @Test
    void processFile_WithCamt054_ShouldReturnTransactions() {
        String xmlContent = "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:camt.054.001.08\">";
        List<Transaction> expectedTransactions = Arrays.asList(
                new Transaction("Jane Doe", LocalDate.now(), new BigDecimal("200.00"), "REF2", TransactionType.CREDIT)
        );

        when(camt054Processor.canProcess(xmlContent)).thenReturn(true);
        when(camt054Processor.processTransactions(xmlContent)).thenReturn(expectedTransactions);

        List<Transaction> result = service.processFile(xmlContent);

        assertThat(result).isEqualTo(expectedTransactions);
    }

    @Test
    void processFile_WithUnsupportedFormat_ShouldThrowException() {
        String xmlContent = "<Document xmlns=\"unsupported\">";

        when(camt053Processor.canProcess(xmlContent)).thenReturn(false);
        when(camt054Processor.canProcess(xmlContent)).thenReturn(false);

        assertThatThrownBy(() -> service.processFile(xmlContent))
                .isInstanceOf(UnsupportedCamtFormatException.class)
                .hasMessage("No processor found for this CAMT format");
    }

    @Test
    void getSupportedFormats_ShouldReturnAllFormats() {
        when(camt053Processor.getFormatVersion()).thenReturn("053.001.08");
        when(camt054Processor.getFormatVersion()).thenReturn("054.001.08");

        List<String> formats = service.getSupportedFormats();

        assertThat(formats)
                .hasSize(2)
                .contains("053.001.08", "054.001.08");
    }
}