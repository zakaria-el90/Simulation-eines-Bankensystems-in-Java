//Transfer
package bank;

/**
 * Überweisung von Sender an Empfänger (Betrag muss >= 0 sein).
 */
public class Transfer extends Transaction {

    private String sender;
    private String recipient;

    // Konstruktoren
    /**
     * Konstruktoren mit Datum, Betrag und Beschreibung
     * @param date Datum der Überweisung
     * @param amount Betrag
     */
    public Transfer(String date, double amount, String description) {
        super(date, amount, description);
    }
    /**
     * Erzeugt einen Transfer mit allen Attributen.
     *
     * @param date        Datum im Format {@code "DD.MM.YYYY"}
     * @param amount      Betrag (muss {@code >= 0} sein)
     * @param description Beschreibung
     * @param sender      Sender
     * @param recipient   Empfänger
     */
    public Transfer(String date, double amount, String description,
                    String sender, String recipient) {
        this(date, amount, description);
        this.sender = sender;
        this.recipient = recipient;
    }
    /**
     * Kopierkonstruktor.
     *
     * @param other anderes {@code Transfer}-Objekt
     */
    public Transfer(Transfer other) {
        this(other.getDate(), other.getAmount(), other.getDescription(),
                other.sender, other.recipient);
    }

    // Getter/Setter der eigenen Attribute
    public String getSender() { return sender; }
    public String getRecipient() { return recipient; }
    public void setSender(String sender) { this.sender = sender; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    /**
     * Setzt den Betrag nur, wenn er {@code >= 0} ist.
     *
     * @param amount neuer Betrag
     */
    @Override
    public void setAmount(double amount) {
        if (amount >= 0) {
            super.setAmount(amount);
        } else {
            throw new IllegalArgumentException("Die Überweisung muss positiv sein");
        }
    }

    // CalculateBill
    @Override
    public double calculate() {
        return getAmount(); // keine Zinsen/Gebühren
    }

    /**
     * Textdarstellung inkl. eigener Attribute.
     *
     * @return String-Repräsentation
     */
    //redundanzarm halten
    @Override
    public String toString() {
        return super.toString() + "\n sender: " + sender + "\n  recipient: " +  recipient;
    }
    /**
     * Gleichheitstest inkl. eigener Attribute.
     *
     * @param obj anderes Objekt
     * @return {@code true}, wenn alle Attribute gleich sind
     */

   @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false; // vergleicht date, amount, description
        Transfer other = (Transfer) obj;
        boolean SenderEquals = sender.equals(other.sender);
        boolean RecipientEquals = recipient.equals(other.recipient);
        return SenderEquals && RecipientEquals;
    }
}
