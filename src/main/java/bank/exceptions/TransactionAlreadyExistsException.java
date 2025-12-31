package bank.exceptions;

/**
 * Wird geworfen, wenn eine Transaktion bereits für dieses Konto vorhanden ist.
 */
public class TransactionAlreadyExistsException extends Exception  {

    public TransactionAlreadyExistsException(String ausgabe) {
        super(ausgabe);
    }
}
