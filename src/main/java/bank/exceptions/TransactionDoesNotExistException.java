package bank.exceptions;

/**
 * Wird geworfen, wenn eine gesuchte Transaktion nicht gefunden wird.
 */
public class TransactionDoesNotExistException extends Exception {

    public TransactionDoesNotExistException(String ausgabe) {
        super(ausgabe);
    }
}
