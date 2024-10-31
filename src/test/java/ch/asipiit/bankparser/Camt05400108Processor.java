package ch.asipiit.bankparser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class Camt05400108ProcessorTest {

    private Camt05400108Processor processor;

    @BeforeEach
    void setUp() {
        processor = new Camt05400108Processor();
    }

    @Test
    void canProcess_WithValidFormat_ShouldReturnTrue() {
        String xmlContent = "<?xml version=\"1.0\"?><Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:camt.054.001.08\">";

        boolean result = processor.canProcess(xmlContent);

        assertThat(result).isTrue();
    }

    @Test
    void canProcess_WithInvalidFormat_ShouldReturnFalse() {
        String xmlContent = "<?xml version=\"1.0\"?><Document xmlns=\"wrong-format\">";

        boolean result = processor.canProcess(xmlContent);

        assertThat(result).isFalse();
    }

    @Test
    void processTransactions_WithValidContent_ShouldReturnTransactions() {
        String validXml = getValidCamt054Xml();

        List<Transaction> transactions = processor.processTransactions(validXml);

        assertThat(transactions)
                .isNotEmpty()
                .allSatisfy(transaction -> {
                    assertThat(transaction.getDebtorName()).isNotNull();
                    assertThat(transaction.getAmount()).isNotNull();
                    assertThat(transaction.getDate()).isNotNull();
                });
    }

    private String getValidCamt054Xml() {
        // Create a minimal valid CAMT.054 XML for testing
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:camt.054.001.08\">" +
                "  <BkToCstmrDbtCdtNtfctn>" +
                "    <Ntfctn>" +
                "      <Ntry>" +
                "        <!-- Add necessary entry details -->" +
                "      </Ntry>" +
                "    </Ntfctn>" +
                "  </BkToCstmrDbtCdtNtfctn>" +
                "</Document>";
    }
}