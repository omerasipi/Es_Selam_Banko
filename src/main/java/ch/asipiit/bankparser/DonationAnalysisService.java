package ch.asipiit.bankparser;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DonationAnalysisService {
    private static final BigDecimal MINIMUM_MONTHLY_DONATION = new BigDecimal("30.00");

    public DonationAnalysis analyzeDonations(List<Transaction> transactions) {
        Map<String, List<Transaction>> donationsByDonor = transactions.stream()
                .filter(t -> t.getType() == TransactionType.CREDIT)
                .collect(Collectors.groupingBy(Transaction::getDebtorName));

        List<DonorSummary> donorSummaries = calculateDonorSummaries(donationsByDonor);

        return new DonationAnalysis(
                donorSummaries,
                calculateTotalDonations(donorSummaries),
                countDonorsBelowMinimum(donorSummaries),
                LocalDateTime.now()
        );
    }

    private List<DonorSummary> calculateDonorSummaries(Map<String, List<Transaction>> donationsByDonor) {
        return donationsByDonor.entrySet().stream()
                .map(entry -> createDonorSummary(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private DonorSummary createDonorSummary(String donorName, List<Transaction> donations) {
        BigDecimal total = donations.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal monthlyAverage = calculateMonthlyAverage(donations);

        return new DonorSummary(
                donorName,
                total,
                monthlyAverage,
                monthlyAverage.compareTo(MINIMUM_MONTHLY_DONATION) < 0,
                donations
        );
    }

    private BigDecimal calculateMonthlyAverage(List<Transaction> donations) {
        if (donations.isEmpty()) return BigDecimal.ZERO;

        LocalDate firstDonation = donations.stream()
                .map(Transaction::getDate)
                .min(LocalDate::compareTo)
                .get();

        LocalDate lastDonation = donations.stream()
                .map(Transaction::getDate)
                .max(LocalDate::compareTo)
                .get();

        long monthsBetween = java.time.temporal.ChronoUnit.MONTHS.between(
                firstDonation.withDayOfMonth(1),
                lastDonation.withDayOfMonth(1)
        ) + 1;

        BigDecimal total = donations.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return total.divide(BigDecimal.valueOf(monthsBetween), 2, BigDecimal.ROUND_HALF_UP);
    }

    private BigDecimal calculateTotalDonations(List<DonorSummary> summaries) {
        return summaries.stream()
                .map(DonorSummary::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int countDonorsBelowMinimum(List<DonorSummary> summaries) {
        return (int) summaries.stream()
                .filter(DonorSummary::isBelowMinimum)
                .count();
    }
}
