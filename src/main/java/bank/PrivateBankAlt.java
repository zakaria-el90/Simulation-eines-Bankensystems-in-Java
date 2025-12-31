package bank;

import java.io.IOException;

/**
 * Alternative PrivateBank-Implementierung (Variante 2 mit instanceof).
 */
public class PrivateBankAlt extends PrivateBank {

    public PrivateBankAlt(String name, double incomingInterest, double outgoingInterest, String directoryName) throws IOException {
        super(name, incomingInterest, outgoingInterest, directoryName);
    }

    @Override
    public double getAccountBalance(String account) {

        double balance = 0.0;

        for (Transaction transaction : getTransactions(account)) {
            if (transaction instanceof Payment) {
                // Payment: direkt calculate() benutzen
                balance += transaction.calculate();
            } else if (transaction instanceof Transfer) {
                Transfer transfer = (Transfer) transaction;

                // Variante 2: Unterscheidung über sender/recipient
                if (account.equals(transfer.getSender())) {
                    // "Sendung" -> Betrag abziehen
                    balance -= transfer.calculate();
                } else if (account.equals(transfer.getRecipient())) {
                    // "Empfang" -> Betrag hinzufügen
                    balance += transfer.calculate();
                }
            }
        }

        return balance;
    }
}