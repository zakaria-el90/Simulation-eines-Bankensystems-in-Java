package bank.exceptions;

/**
 * Wird geworfen, wenn Attribute einer Transaktion ungültig sind
 * (z.B. negativer Betrag bei Transfer, ungültige Interest-Werte bei Payment).
 */
public class TransactionAttributeException extends Exception {

    public TransactionAttributeException(String ausgabe) {
        super(ausgabe);
    }
}
