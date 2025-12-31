//Payment
package bank;

/**
 * Zahlung: positiver Betrag = Einzahlung, negativer Betrag = Auszahlung.
 */
public class Payment extends Transaction {

    /** Zins für Einzahlungen (0..1). */
    private double incomingInterest;
    /** Zins für Auszahlungen (0..1). */
    private double outgoingInterest;

    /**
     * Erzeugt ein Payment mit Basis-Attributen.
     *
     * @param date        Datum im Format {@code "DD.MM.YYYY"}
     * @param amount      Betrag (positiv = Einzahlung, negativ = Auszahlung)
     * @param description Beschreibung
     */
    public Payment(String date, double amount, String description) {
        super(date, amount, description);
    }
    /**
    * Erzeugt ein Payment mit allen Attributen.
     *
     * @param date             Datum im Format {@code "DD.MM.YYYY"}
     * @param amount           Betrag (positiv = Einzahlung, negativ = Auszahlung)
     * @param description      Beschreibung
     * @param incomingInterest Zins für Einzahlungen (0..1)
     * @param outgoingInterest Zins für Auszahlungen (0..1)
     */
    public Payment(String date, double amount, String description,
                   double incomingInterest, double outgoingInterest) {
        this(date, amount, description);
        setIncomingInterest(incomingInterest);
        setOutgoingInterest(outgoingInterest);
    }
    //Kopierkonstruktor.
    public Payment(Payment other) {
        this(other.getDate(), other.getAmount(), other.getDescription(),
                other.incomingInterest, other.outgoingInterest);
    }

    // Getter/Setter der eigenen Attribute
    public double getIncomingInterest() { return incomingInterest; }
    public double getOutgoingInterest() { return outgoingInterest; }

    public void setIncomingInterest(double incomingInterest) {
        if (incomingInterest < 0 || incomingInterest > 1) {
            throw new IllegalArgumentException("Der Zinssatz muss zwischen 0 und 1 liegen");
        }
        this.incomingInterest = incomingInterest;
    }

    public void setOutgoingInterest(double outgoingInterest) {
        if (outgoingInterest < 0 || outgoingInterest > 1) {
            throw new IllegalArgumentException("Der Zinssatz muss zwischen 0 und 1 liegen");
        }
        this.outgoingInterest = outgoingInterest;
    }

    /**
     * Berechnet den Betrag unter Berücksichtigung der Zinsen.
     *
     * @return berechneter Betrag
     */

    @Override
    public double calculate() {
        double result;

        if (amount >= 0) {
            // Einzahlung → incomingInterest abziehen
            result = amount * (1 - incomingInterest);
        } else {
            // Auszahlung → outgoingInterest addieren
            result = amount * (1 + outgoingInterest);
        }

        return result;
    }

    @Override
    public String toString() {
        return super.toString() + "\n incomingInterest: " + incomingInterest + "\n outgoingInterest: " + outgoingInterest;
    }

    /**
     * Gleichheitstest inkl. eigener Attribute.
     *
     * @param obj anderes Objekt
     * @return {@code true}, wenn alle Attribute gleich sind
     */
    //Payment.equals vergleicht Zinsen mit Double.compare -> stabil
    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) return false; // vergleicht date, amount, description
        Payment other = (Payment) obj;
        return Double.compare(this.incomingInterest, other.incomingInterest) == 0
                && Double.compare(this.outgoingInterest, other.outgoingInterest) == 0;
    }
}
