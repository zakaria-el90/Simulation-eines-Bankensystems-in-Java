//CalculateBill
package bank;

/**
 * Einfache Schnittstelle zur Berechnung des auszuzahlenden/anzurechnenden Betrags.
 * Die Implementierung darf den gespeicherten amount-Wert NICHT verändern.
 */
public interface CalculateBill {
    /**
     * Berechnet den Wert der Transaktion (z. B. abzüglich/zzgl. Zinsen/Gebühren).
     * @return berechneter Betrag als double
     */
    double calculate();
}
