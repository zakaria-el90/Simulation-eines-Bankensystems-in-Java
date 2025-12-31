package bank;

/**
 * Repräsentiert einen eingehenden Transfer (Empfang).
 * Für das Konto ist der Betrag positiv.
 */
public class IncomingTransfer extends Transfer {

    /**
     * Erstellt einen neuen eingehenden Transfer ohne Angabe von Sender und Empfänger.
     * @param date        Datum des Transfers (nicht null)
     * @param amount      positiver Betrag des Transfers
     * @param description Beschreibung des Transfers
     */
    public IncomingTransfer(String date, double amount, String description) {
        super(date, amount, description);
    }

    /**
     * Erstellt einen neuen eingehenden Transfer inkl. Sender und Empfänger.
     * @param date        Datum des Transfers (nicht null)
     * @param amount      positiver Betrag des Transfers
     * @param description Beschreibung des Transfers
     * @param sender      Name des Senders
     * @param recipient   Name des Empfängers (dieses Konto)
     */
    public IncomingTransfer(String date, double amount, String description,
                            String sender, String recipient) {
        super(date, amount, description, sender, recipient);
    }
    //Kopierkonstruktor.
    public IncomingTransfer(IncomingTransfer t1) {
        super(t1);
    }
}
