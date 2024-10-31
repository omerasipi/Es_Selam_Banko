package ch.asipiit.bankparser;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionTest {

    @Test
    void constructor_WithValidData_ShouldCreateTransaction() {
        LocalDate date = LocalDate.now();
        Transaction transaction = new Transaction(
                "John Doe",
                date,
                new BigDecimal("100.00"),
                "REF1",
                TransactionType.CREDIT
        );

        assertThat(transaction.getDebtorName()).isEqualTo("John Doe");
        assertThat(transaction.getDate()).isEqualTo(date);
        assertThat(transaction.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(transaction.getReference()).isEqualTo("REF1");
        assertThat(transaction.getType()).isEqualTo(TransactionType.CREDIT);
    }

    @Test
    void constructor_WithNullValues_ShouldThrowException() {
        assertThatThrownBy(() ->
                new Transaction(null, LocalDate.now(), new BigDecimal("100.00"), "REF1", TransactionType.CREDIT)
        ).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() ->
                new Transaction("John Doe", null, new BigDecimal("100.00"), "REF1", TransactionType.CREDIT)
        ).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() ->
                new Transaction("John Doe", LocalDate.now(), null, "REF1", TransactionType.CREDIT)
        ).isInstanceOf(IllegalArgumentException.class);
    }
}