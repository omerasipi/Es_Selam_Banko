package ch.asipiit.bankparser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class DonationAnalysisServiceTest {

    private DonationAnalysisService service;

    @BeforeEach
    void setUp() {
        service = new DonationAnalysisService();
    }

    @Test
    void analyzeDonations_WithMultipleDonors_ShouldCalculateCorrectly() {
        List<Transaction> transactions = Arrays.asList(
                new Transaction("John Doe", LocalDate.now(), new BigDecimal("100.00"), "REF1", TransactionType.CREDIT),
                new Transaction("John Doe", LocalDate.now().plusMonths(1), new BigDecimal("100.00"), "REF2", TransactionType.CREDIT),
                new Transaction("Jane Doe", LocalDate.now(), new BigDecimal("20.00"), "REF3", TransactionType.CREDIT)
        );

        DonationAnalysis analysis = service.analyzeDonations(transactions);

        assertThat(analysis.getTotalDonations()).isEqualByComparingTo(new BigDecimal("220.00"));
        assertThat(analysis.getDonorsBelowMinimum()).isEqualTo(1);
        assertThat(analysis.getDonors()).hasSize(2);
    }

    @Test
    void analyzeDonations_WithEmptyList_ShouldReturnEmptyAnalysis() {
        DonationAnalysis analysis = service.analyzeDonations(List.of());

        assertThat(analysis.getTotalDonations()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(analysis.getDonorsBelowMinimum()).isZero();
        assertThat(analysis.getDonors()).isEmpty();
    }

    @Test
    void analyzeDonations_ShouldIdentifyDonorsBelowMinimum() {
        List<Transaction> transactions = Arrays.asList(
                new Transaction("Low Donor", LocalDate.now(), new BigDecimal("20.00"), "REF1", TransactionType.CREDIT),
                new Transaction("High Donor", LocalDate.now(), new BigDecimal("100.00"), "REF2", TransactionType.CREDIT)
        );

        DonationAnalysis analysis = service.analyzeDonations(transactions);

        assertThat(analysis.getDonorsBelowMinimum()).isEqualTo(1);
        assertThat(analysis.getDonors())
                .extracting("name", "belowMinimum")
                .contains(
                        tuple("Low Donor", true),
                        tuple("High Donor", false)
                );
    }

    @Test
    void analyzeDonations_ShouldCalculateMonthlyAverages() {
        LocalDate baseDate = LocalDate.of(2024, 1, 1);
        List<Transaction> transactions = Arrays.asList(
                new Transaction("John Doe", baseDate, new BigDecimal("30.00"), "REF1", TransactionType.CREDIT),
                new Transaction("John Doe", baseDate.plusMonths(1), new BigDecimal("30.00"), "REF2", TransactionType.CREDIT),
                new Transaction("John Doe", baseDate.plusMonths(2), new BigDecimal("30.00"), "REF3", TransactionType.CREDIT)
        );

        DonationAnalysis analysis = service.analyzeDonations(transactions);

        assertThat(analysis.getDonors())
                .extracting("monthlyAverage")
                .allMatch(avg -> ((BigDecimal) avg).compareTo(new BigDecimal("30.00")) == 0);
    }
}