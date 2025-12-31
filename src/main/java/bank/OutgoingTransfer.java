package bank;

/**
 * Repräsentiert einen ausgehenden Transfer (Sendung).
 * Für das Konto ist der Betrag negativ.
 */
public class OutgoingTransfer extends Transfer {

    /**
     * Erstellt einen neuen ausgehenden Transfer ohne Sender/Empfänger-Angaben.
     * @param date        Datum des Transfers (nicht null)
     * @param amount      positiver Betrag (wird intern negativ verrechnet)
     * @param description Beschreibung des Transfers
     */
    public OutgoingTransfer(String date, double amount, String description) {
        super(date, amount, description);
    }

    /**
     * Erstellt einen neuen ausgehenden Transfer mit Sender und Empfänger.
     * @param date        Datum des Transfers (nicht null)
     * @param amount      positiver Betrag (wird intern negativ verrechnet)
     * @param description Beschreibung des Transfers
     * @param sender      Name des Senders (dieses Konto)
     * @param recipient   Name des Empfängers
     */

    public OutgoingTransfer(String date, double amount, String description,
                            String sender, String recipient) {
        super(date, amount, description, sender, recipient);
    }
    //Kopierkonstruktor.
    public OutgoingTransfer(OutgoingTransfer t2) {
        super(t2);
    }

    /**
     * Berechnet den Wert des Transfers für das Konto.
     * @return negativer Betrag des Transfers
     */
    @Override
    public double calculate() {
        // Ausgehend: negativer Beitrag zum Kontostand
        return -getAmount();
    }
}
