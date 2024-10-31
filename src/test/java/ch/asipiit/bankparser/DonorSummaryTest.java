package ch.asipiit.bankparser;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DonorSummaryTest {

    @Test
    void constructor_WithValidData_ShouldCreateDonorSummary() {
        List<Transaction> transactions = Arrays.asList(
                new Transaction("John Doe", LocalDate.now(), new BigDecimal("100.00"), "REF1", TransactionType.CREDIT)
        );

        DonorSummary summary = new DonorSummary(
                "John Doe",
                new BigDecimal("100.00"),
                new BigDecimal("100.00"),
                false,
                transactions
        );

        assertThat(summary.getName()).isEqualTo("John Doe");
        assertThat(summary.getTotalAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(summary.getMonthlyAverage()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(summary.isBelowMinimum()).isFalse();
        assertThat(summary.getDonations()).hasSize(1);
    }
}