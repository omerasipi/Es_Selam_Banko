package ch.asipiit.bankparser;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Transaction {
    private final String debtorName;
    private final LocalDate date;
    private final BigDecimal amount;
    private final String reference;
    private final TransactionType type;

    public Transaction(String debtorName, LocalDate date, BigDecimal amount, String reference, TransactionType type) {
        this.debtorName = debtorName;
        this.date = date;
        this.amount = amount;
        this.reference = reference;
        this.type = type;
    }

    public String getDebtorName() {
        return debtorName;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getReference() {
        return reference;
    }

    public TransactionType getType() {
        return type;
    }
}