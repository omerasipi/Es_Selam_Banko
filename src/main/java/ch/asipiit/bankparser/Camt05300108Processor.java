package ch.asipiit.bankparser;

import com.prowidesoftware.swift.model.mx.MxCamt05300108;
import com.prowidesoftware.swift.model.mx.dic.ReportEntry10;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class Camt05300108Processor implements CamtProcessor<ReportEntry10> {
    private static final String FORMAT = "urn:iso:std:iso:20022:tech:xsd:camt.053.001.08";

    @Override
    public boolean canProcess(String xmlContent) {
        return xmlContent.contains(FORMAT);
    }

    @Override
    public List<Transaction> processTransactions(String xmlContent) {
        try {
            var camt = MxCamt05300108.parse(xmlContent);
            return camt.getBkToCstmrStmt()
                    .getStmt()
                    .stream()
                    .flatMap(statement -> statement.getNtry().stream())
                    .map(this::mapToTransaction)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to process CAMT.053 file: " + e.getMessage(), e);
        }
    }

    @Override
    public String getFormatVersion() {
        return "053.001.08";
    }

    private Transaction mapToTransaction(ReportEntry10 entry) {
        return new Transaction(
                extractDebtorName(entry),
                extractDate(entry),
                extractAmount(entry),
                extractReference(entry),
                extractTransactionType(entry)
        );
    }

    private String extractDebtorName(ReportEntry10 entry) {
        try {
            return Optional.ofNullable(entry)
                    .flatMap(e -> Optional.ofNullable(e.getNtryDtls()))
                    .flatMap(details -> details.stream().findFirst())
                    .flatMap(detail -> Optional.ofNullable(detail.getTxDtls()))
                    .flatMap(txDetails -> txDetails.stream().findFirst())
                    .flatMap(tx -> Optional.ofNullable(tx.getRltdPties()))
                    .flatMap(parties -> Optional.ofNullable(parties.getDbtr()))
                    .flatMap(debtor -> Optional.ofNullable(debtor.getPty()))
                    .map(party -> party.getNm())
                    .orElse("Unknown");
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private LocalDate extractDate(ReportEntry10 entry) {
        try {
            return Optional.ofNullable(entry)
                    .map(e -> e.getBookgDt())
                    .map(dt -> dt.getDt())
                    .map(dt -> LocalDate.parse(dt.toString()))
                    .orElse(LocalDate.now());
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    private BigDecimal extractAmount(ReportEntry10 entry) {
        try {
            return Optional.ofNullable(entry)
                    .map(e -> e.getAmt())
                    .map(amt -> amt.getValue())
                    .orElse(BigDecimal.ZERO);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private String extractReference(ReportEntry10 entry) {
        try {
            return Optional.ofNullable(entry)
                    .flatMap(e -> Optional.ofNullable(e.getNtryDtls()))
                    .flatMap(details -> details.stream().findFirst())
                    .flatMap(detail -> Optional.ofNullable(detail.getTxDtls()))
                    .flatMap(txDetails -> txDetails.stream().findFirst())
                    .flatMap(tx -> Optional.ofNullable(tx.getRmtInf()))
                    .flatMap(rmt -> Optional.ofNullable(rmt.getStrd()))
                    .flatMap(strd -> strd.stream().findFirst())
                    .flatMap(s -> Optional.ofNullable(s.getCdtrRefInf()))
                    .map(ref -> ref.getRef())
                    .orElse("");
        } catch (Exception e) {
            return "";
        }
    }

    private TransactionType extractTransactionType(ReportEntry10 entry) {
        try {
            return Optional.ofNullable(entry)
                    .map(e -> e.getCdtDbtInd())
                    .map(ind -> "CRDT".equalsIgnoreCase(ind.name()))
                    .map(isCredit -> isCredit ? TransactionType.CREDIT : TransactionType.DEBIT)
                    .orElse(TransactionType.DEBIT);
        } catch (Exception e) {
            return TransactionType.DEBIT;
        }
    }
}