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
    private Camt05400108Processor camt054Processor;

    private CamtProcessingService service;

    @BeforeEach
    void setUp() {
        service = new CamtProcessingService(Arrays.asList(camt054Processor));
    }

    @Test
    void processFile_WithValidContent_ShouldReturnTransactions() {
        String xmlContent = "<xml>test</xml>";
        List<Transaction> expectedTransactions = Arrays.asList(
                new Transaction("John Doe", LocalDate.now(), new BigDecimal("100.00"), "REF1", TransactionType.CREDIT)
        );

        when(camt054Processor.canProcess(xmlContent)).thenReturn(true);
        when(camt054Processor.processTransactions(xmlContent)).thenReturn(expectedTransactions);

        List<Transaction> result = service.processFile(xmlContent);

        assertThat(result).isEqualTo(expectedTransactions);
    }

    @Test
    void processFile_WithUnsupportedFormat_ShouldThrowException() {
        String xmlContent = "<xml>unsupported</xml>";
        when(camt054Processor.canProcess(xmlContent)).thenReturn(false);

        assertThatThrownBy(() -> service.processFile(xmlContent))
                .isInstanceOf(UnsupportedCamtFormatException.class)
                .hasMessage("No processor found for this CAMT format");
    }

    @Test
    void canProcessFile_WithSupportedFormat_ShouldReturnTrue() {
        String xmlContent = "<xml>test</xml>";
        when(camt054Processor.canProcess(xmlContent)).thenReturn(true);

        boolean result = service.canProcessFile(xmlContent);

        assertThat(result).isTrue();
    }
}