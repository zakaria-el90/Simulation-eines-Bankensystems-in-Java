package bank.exceptions;

/**
 * Wird geworfen, wenn ein Konto bereits existiert.
 */
public class AccountAlreadyExistsException extends Exception {

    public AccountAlreadyExistsException(String ausgabe) {
        super(ausgabe);
    }
}
