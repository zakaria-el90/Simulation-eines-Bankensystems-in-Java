package bank.exceptions;

/**
 * Wird geworfen, wenn auf ein nicht existierendes Konto zugegriffen wird.
 */
public class AccountDoesNotExistException extends Exception {

    public AccountDoesNotExistException(String ausgabe) {
        super(ausgabe);
    }
}
