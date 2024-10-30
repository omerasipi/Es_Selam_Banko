package ch.asipiit.bankparser;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class DonationAnalysis {
    private final List<DonorSummary> donors;
    private final BigDecimal totalDonations;
    private final int donorsBelowMinimum;
    private final LocalDateTime analyzedAt;

    public DonationAnalysis(
            List<DonorSummary> donors,
            BigDecimal totalDonations,
            int donorsBelowMinimum,
            LocalDateTime analyzedAt
    ) {
        this.donors = donors;
        this.totalDonations = totalDonations;
        this.donorsBelowMinimum = donorsBelowMinimum;
        this.analyzedAt = analyzedAt;
    }

    public List<DonorSummary> getDonors() {
        return donors;
    }

    public BigDecimal getTotalDonations() {
        return totalDonations;
    }

    public int getDonorsBelowMinimum() {
        return donorsBelowMinimum;
    }

    public LocalDateTime getAnalyzedAt() {
        return analyzedAt;
    }
}