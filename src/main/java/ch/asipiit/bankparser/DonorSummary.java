package ch.asipiit.bankparser;

import java.math.BigDecimal;
import java.util.List;

public class DonorSummary {
    private final String name;
    private final BigDecimal totalAmount;
    private final BigDecimal monthlyAverage;
    private final boolean belowMinimum;
    private final List<Transaction> donations;

    public DonorSummary(
            String name,
            BigDecimal totalAmount,
            BigDecimal monthlyAverage,
            boolean belowMinimum,
            List<Transaction> donations
    ) {
        this.name = name;
        this.totalAmount = totalAmount;
        this.monthlyAverage = monthlyAverage;
        this.belowMinimum = belowMinimum;
        this.donations = donations;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public BigDecimal getMonthlyAverage() {
        return monthlyAverage;
    }

    public boolean isBelowMinimum() {
        return belowMinimum;
    }

    public List<Transaction> getDonations() {
        return donations;
    }
}