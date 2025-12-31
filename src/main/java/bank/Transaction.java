//Transaction
package bank;

/**
 * Abstrakte Oberklasse für gemeinsame Eigenschaften von Payment und Transfer.
 */
public abstract class Transaction implements CalculateBill {

    /** Datum im Format "DD.MM.YYYY". */
    protected  String date;

    /** Ursprünglicher Betrag der Transaktion. */
    protected  double amount;

    /** Beschreibungstext. */
    protected  String description;

    /**
     * Basis-Konstruktor.
     * @param date Datum (Format "DD.MM.YYYY")
     * @param amount Betrag der Transaktion
     * @param description Beschreibung
     */
    public Transaction(String date, double amount, String description) {
        this.date = date;
        setAmount(amount);
        this.description = description;
    }


    /**
     * copy-Konstruktor: Erstellt eine Kopie eines vorhanden Payment-Objekts.
     * @param other Das zu kopierende Payment-Objekt
     * */
    public Transaction(Transaction other) {
        this(other.date, other.amount, other.description);
    }

    // Getter
    public String getDate() { return date; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }

    // Setter
    public void setDate(String date) { this.date = date; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setDescription(String description) { this.description = description; }

    /**
     * Textdarstellung mit dem **berechneten** Betrag (calculate()).
     */
    @Override
    public String toString(){
        return "\ndate: " + date + "\namount: "+ amount + "\ndescription: " + description;
    }

    /**
     * Gleichheitsprüfung über alle **gemeinsamen** Attribute.
     * @param obj anderes Objekt
     * @return true, wenn alle Attribute gleich sind
     */
    //sind beide Objekte vom exakt gleichen Typ?
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Transaction temp = (Transaction) obj;
        return date.equals(temp.date) 
            && description.equals(temp.description) 
            && Double.compare(amount, temp.amount) == 0;
    }
}


