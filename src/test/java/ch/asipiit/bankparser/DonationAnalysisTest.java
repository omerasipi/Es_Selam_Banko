package ch.asipiit.bankparser;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DonationAnalysisTest {

    @Test
    void constructor_WithValidData_ShouldCreateAnalysis() {
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

        DonationAnalysis analysis = new DonationAnalysis(
                donors,
                new BigDecimal("100.00"),
                0,
                LocalDate.now().atStartOfDay()
        );

        assertThat(analysis.getDonors()).hasSize(1);
        assertThat(analysis.getTotalDonations()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(analysis.getDonorsBelowMinimum()).isZero();
    }
}
