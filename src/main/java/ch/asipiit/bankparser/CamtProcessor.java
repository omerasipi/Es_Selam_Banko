package ch.asipiit.bankparser;

import java.util.List;

public interface CamtProcessor<T> {
    boolean canProcess(String xmlContent);
    List<Transaction> processTransactions(String xmlContent);
    String getFormatVersion();
}