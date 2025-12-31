package bank;

import bank.exceptions.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Repräsentiert eine private Bank mit mehreren Konten und deren Transaktionen.
 * <p>
 * Für jedes Konto wird eine Liste von {@link Transaction}-Objekten gespeichert.
 * Die Bank verwaltet außerdem bankweite Zinssätze, die bei {@link Payment}-Transaktionen
 * verwendet werden.
 * </p>
 */
public class PrivateBank implements Bank {
    /**
     * Name der Bank.
     */
    private String name;
    private double incomingInterest; //Einzahlungen
    private double outgoingInterest; //Auszahlungen
    private String directoryName; // Der Speicherort der Konten
    /**
     * Zuordnung von Kontonamen zu deren Transaktionslisten.
     * <p>
     * Der Schlüssel ist der Kontoname (String), der Wert eine {@link List}
     * </p>
     */
    private Map<String, List<Transaction>> accountsToTransactions = new HashMap<>(); //Map: Kontoname -> Liste von Transaktionen

    // Getter
    public String getName() {
        return name;
    }

    public double getIncomingInterest() {
        return incomingInterest;
    }

    public double getOutgoingInterest() {
        return outgoingInterest;
    }

    // Setter
    public void setName(String name) {
        this.name = name;
    }

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
     * Konstruktor zum Anlegen einer neuen Bank mit Namen und Zinssätzen.
     *
     * @param name             Name der Bank
     * @param incomingInterest Zinssatz für eingehende Zahlungen im Bereich {@code [0,1]}
     * @param outgoingInterest Zinssatz für ausgehende Zahlungen im Bereich {@code [0,1]}
     */
    public PrivateBank(String name, double incomingInterest,
                       double outgoingInterest, String directoryName) throws IOException {
        this.name = name;
        setIncomingInterest(incomingInterest);
        setOutgoingInterest(outgoingInterest);
        this.directoryName = directoryName;

        Files.createDirectories(Path.of(directoryName));

        // Bestehende Konten vom Dateisystem laden
        readAccounts();
    }

    /**
     * Kopierkonstruktor.
     *
     * @param other die zu kopierende {@code PrivateBank}-Instanz
     */
    public PrivateBank(PrivateBank other) {
        this.name = other.name;
        this.incomingInterest = other.incomingInterest;
        this.outgoingInterest = other.outgoingInterest;
        this.accountsToTransactions = new HashMap<>(other.accountsToTransactions);
        this.directoryName = other.directoryName;
    }

    /**
     * Liefert eine textuelle Darstellung der Bank.
     *
     * @return String mit Name und Zinssätzen dieser Bank
     */
    @Override
    public String toString() {
        return "name: " + name + "\nincomingInterest: " + incomingInterest
                + "\noutgoingInterest: " + outgoingInterest
                + "\ndirectoryName: " + directoryName;
    }

    /**
     * Vergleicht diese Bank mit einem anderen Objekt auf Gleichheit.
     *
     * @param obj das zu vergleichende Objekt
     * @return {@code true}, wenn beide Objekte gleich sind, sonst {@code false}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PrivateBank)) return false;

        PrivateBank other = (PrivateBank) obj;

        boolean sameIncoming = Double.compare(other.incomingInterest, incomingInterest) == 0;
        boolean sameOutgoing = Double.compare(other.outgoingInterest, outgoingInterest) == 0;
        boolean sameName = Objects.equals(name, other.name);
        boolean sameDirectory = Objects.equals(directoryName, other.directoryName);
        boolean sameAccounts = Objects.equals(accountsToTransactions, other.accountsToTransactions);

        return sameIncoming && sameOutgoing && sameDirectory && sameName && sameAccounts;
    }


    // Bank-Methoden

    /**
     * Legt ein neues Konto ohne Anfangstransaktionen an.
     *
     * @param account Name des neuen Kontos
     * @throws AccountAlreadyExistsException falls bereits ein Konto mit diesem Namen existiert
     */
    @Override
    public void createAccount(String account) throws AccountAlreadyExistsException, IOException {
        if (accountsToTransactions.containsKey(account)) {
            throw new AccountAlreadyExistsException("Account already exists: " + account);
        }

        accountsToTransactions.put(account, new ArrayList<Transaction>());

        // Konto persistieren
        writeAccount(account);
    }

    /**
     * Legt ein neues Konto mit einer Liste von Anfangstransaktionen an.
     *
     * @param account      Name des neuen Kontos
     * @param transactions Liste von Transaktionen, die dem Konto zugeordnet werden sollen; darf {@code null} sein
     * @throws AccountAlreadyExistsException     falls bereits ein Konto mit diesem Namen existiert
     * @throws TransactionAlreadyExistsException falls in der übergebenen Liste für dieses Konto doppelte Transaktionen enthalten sind
     * @throws TransactionAttributeException     falls ungültige Attributwerte in den Transaktionen erkannt werden (z.B. negativer Transferbetrag
     *                                           oder ungültige Zinssätze)
     */
    @Override
    public void createAccount(String account, List<Transaction> transactions)
            throws AccountAlreadyExistsException, TransactionAlreadyExistsException, TransactionAttributeException, IOException {

        if (accountsToTransactions.containsKey(account)) {
            throw new AccountAlreadyExistsException("Account already exists: " + account);
        }

        List<Transaction> accountTransactions = new ArrayList<Transaction>();

        if (transactions != null) {
            for (Transaction t : transactions) {

                // doppelte Transaktion verhindern
                if (accountTransactions.contains(t)) {
                    throw new TransactionAlreadyExistsException("Transaction already exists for this account: " + t);
                }

                // Attribut-Prüfung: Transfer amount >= 0
                if (t instanceof Transfer) {
                    Transfer transfer = (Transfer) t;
                    if (transfer.getAmount() < 0) {
                        throw new TransactionAttributeException("Transfer amount must be >= 0");
                    }
                }

                // Attribut-Prüfung + Zinsübernahme bei Payment
                if (t instanceof Payment) {
                    if (incomingInterest < 0 || incomingInterest > 1
                            || outgoingInterest < 0 || outgoingInterest > 1) {
                        throw new TransactionAttributeException("Interest rates must be between 0 and 1");
                    }

                    Payment payment = (Payment) t;
                    // Bank-Zinsen überschreiben Payment-Zinsen
                    payment.setIncomingInterest(incomingInterest);
                    payment.setOutgoingInterest(outgoingInterest);
                }

                accountTransactions.add(t);
            }
        }

        accountsToTransactions.put(account, accountTransactions);

        // Konto persistieren
        writeAccount(account);
    }

    /**
     * Fügt einem bestehenden Konto eine neue Transaktion hinzu.
     *
     * @param account     Name des Kontos
     * @param transaction hinzuzufügende Transaktion
     * @throws TransactionAlreadyExistsException falls die Transaktion bereits für dieses Konto existiert
     * @throws AccountDoesNotExistException      falls das Konto nicht existiert
     * @throws TransactionAttributeException     falls ungültige Attributwerte in der Transaktion erkannt werden
     */
    @Override
    public void addTransaction(String account, Transaction transaction)
            throws TransactionAlreadyExistsException, AccountDoesNotExistException, TransactionAttributeException, IOException {

        // 1. Konto muss existieren
        if (!accountsToTransactions.containsKey(account)) {
            throw new AccountDoesNotExistException("Account does not exist: " + account);
        }

        // 2. Liste der Transaktionen für dieses Konto holen
        List<Transaction> accountTransactions = accountsToTransactions.get(account);

        // 3. Transaktion darf nicht schon vorhanden sein
        if (accountTransactions.contains(transaction)) {
            throw new TransactionAlreadyExistsException("Transaction already exists for this account: " + transaction);
        }

        // 4. Attribut-Prüfung: Transfer amount >= 0
        if (transaction instanceof Transfer) {
            Transfer transfer = (Transfer) transaction;
            if (transfer.getAmount() < 0) {
                throw new TransactionAttributeException("Transfer amount must be >= 0");
            }
        }

        // 5. Payment: Bank-Zinsen übernehmen + prüfen
        if (transaction instanceof Payment) {
            if (incomingInterest < 0 || incomingInterest > 1
                    || outgoingInterest < 0 || outgoingInterest > 1) {
                throw new TransactionAttributeException("Interest rates must be between 0 and 1");
            }

            Payment payment = (Payment) transaction;
            payment.setIncomingInterest(incomingInterest);
            payment.setOutgoingInterest(outgoingInterest);
        }

        // 6. Transaktion hinzufügen
        accountTransactions.add(transaction);

        // Konto persistieren
        writeAccount(account);
    }

    /**
     * Entfernt eine Transaktion von einem Konto.
     *
     * @param account     Name des Kontos
     * @param transaction zu entfernende Transaktion
     * @throws AccountDoesNotExistException     falls das Konto nicht existiert
     * @throws TransactionDoesNotExistException falls die Transaktion nicht in der Liste des Kontos enthalten ist
     */
    @Override
    public void removeTransaction(String account, Transaction transaction)
            throws AccountDoesNotExistException, TransactionDoesNotExistException {

        if (!accountsToTransactions.containsKey(account)) {
            throw new AccountDoesNotExistException("Account does not exist: " + account);
        }

        List<Transaction> accountTransactions = accountsToTransactions.get(account);

        boolean removed = accountTransactions.remove(transaction);
        if (!removed) {
            throw new TransactionDoesNotExistException("Transaction does not exist for this account: " + transaction);
        }
    }

    /**
     * Prüft, ob eine bestimmte Transaktion bei einem Konto existiert.
     *
     * @param account     Name des Kontos
     * @param transaction gesuchte Transaktion
     * @return {@code true}, wenn das Konto existiert und die Transaktion in der Liste enthalten ist, sonst {@code false}
     */
    @Override
    public boolean containsTransaction(String account, Transaction transaction) {
        if (!accountsToTransactions.containsKey(account)) {
            return false;
        }
        List<Transaction> accountTransactions = accountsToTransactions.get(account);
        return accountTransactions.contains(transaction);
    }

    /**
     * Berechnet den aktuellen Kontostand eines Kontos.
     *
     * @param account Name des Kontos
     * @return aktueller Kontostand
     */
    @Override
    public double getAccountBalance(String account) {
        List<Transaction> accountTransactions = accountsToTransactions.get(account);
        if (accountTransactions == null) {
            return 0.0;
        }

        double balance = 0.0;

        for (Transaction transaction : accountTransactions) {
            // Dank IncomingTransfer / OutgoingTransfer reicht calculate()
            balance += transaction.calculate();
        }

        return balance;
    }

    /**
     * Liefert alle Transaktionen eines Kontos als neue Liste.
     * <p>
     * Wird ein unbekanntes Konto angegeben, wird eine leere Liste zurückgegeben.
     * Die interne Datenstruktur der Bank wird durch eine Kopie geschützt.
     * </p>
     *
     * @param account Name des Kontos
     * @return neue Liste mit allen Transaktionen des Kontos (nie {@code null})
     */
    @Override
    public List<Transaction> getTransactions(String account) {
        List<Transaction> accountTransactions = accountsToTransactions.get(account);

        // Wenn Konto nicht existiert oder leer ist: leere Liste zurückgeben
        if (accountTransactions == null) {
            return new ArrayList<>();
        }

        // Kopie zurückgeben (schützt interne Datenstruktur)
        return new ArrayList<>(accountTransactions);
    }

    /**
     * Liefert alle Transaktionen eines Kontos sortiert nach ihrem berechneten Wert.
     * <p>
     * Die Sortierung erfolgt auf Basis von {@link Transaction#calculate()}.
     * </p>
     *
     * @param account Name des Kontos
     * @param asc     {@code true} für aufsteigende, {@code false} für absteigende Sortierung
     * @return neue, sortierte Liste der Transaktionen (nie {@code null})
     */
    @Override
    public List<Transaction> getTransactionsSorted(String account, boolean asc) {
        List<Transaction> list = getTransactions(account);

        if (list == null) return new ArrayList<>();

        // Mache eine Kopie der Liste, um die Originaldaten nicht zu verändern
        List<Transaction> sortedList = new ArrayList<>(list);

        if (asc) {

            sortedList.sort(Comparator.comparingDouble(Transaction::calculate));  //kleine -> große

        } else {

            sortedList.sort(Comparator.comparingDouble(Transaction::calculate).reversed()); // große -> kleine
        }


        return sortedList;

    }

    /**
     * Filtert die Transaktionen eines Kontos nach ihrem Vorzeichen.
     *
     * @param account  Name des Kontos
     * @param positive {@code true}, um nur Transaktionen mit {@code calculate() >= 0}
     *                 zu erhalten, {@code false} für Transaktionen mit {@code calculate() < 0}
     * @return Liste der gefilterten Transaktionen (nie {@code null})
     */
    @Override
    public List<Transaction> getTransactionsByType(String account, boolean positive) {
        List<Transaction> result = new ArrayList<Transaction>();
        List<Transaction> accountTransactions = accountsToTransactions.get(account);

        if (accountTransactions == null) {
            return result;
        }

        for (Transaction transaction : accountTransactions) {
            double value = transaction.calculate();
            if (positive) {
                if (value >= 0) {
                    result.add(transaction);
                }
            } else {
                if (value < 0) {
                    result.add(transaction);
                }
            }
        }

        return result;
    }

    /**
     * Löscht ein bestehendes Konto aus der Bank und entfernt die zugehörige Persistenzdatei.
     *
     * @param account Name des zu löschenden Kontos
     * @throws AccountDoesNotExistException wenn das Konto nicht existiert
     * @throws IOException                  wenn beim Löschen der Datei ein Fehler auftritt
     */
    @Override
    public void deleteAccount(String account) throws AccountDoesNotExistException, IOException {
        if (!accountsToTransactions.containsKey(account)) {
            throw new AccountDoesNotExistException("Account does not exist: " + account);
        }

        // Erst aus der In-Memory-Struktur entfernen
        accountsToTransactions.remove(account);

        // Dann die Datei löschen (falls vorhanden)
        Path filePath = Path.of(directoryName, account + ".json");
        Files.deleteIfExists(filePath);
    }

    /**
     * Gibt alle aktuell bekannten Kontonamen zurück.
     *
     * @return Liste der Kontonamen (Kopie), sortiert nach Name
     */
    @Override
    public List<String> getAllAccounts() {
        List<String> accounts = new ArrayList<>(accountsToTransactions.keySet());
        Collections.sort(accounts);
        return accounts;
    }


    /**
     * Liest alle vorhandenen Konten vom Dateisystem und lädt sie in accountsToTransactions.
     *
     * @throws IOException wenn ein Fehler beim Lesen auftritt
     */
    private void readAccounts() throws IOException {
        // Verzeichnis erstellen, falls es nicht existiert
        Path dirPath = Path.of(directoryName);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
            return; // Keine Konten zum Lesen
        }

        // Gson mit Custom Serializer/Deserializer konfigurieren
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Transaction.class, new De_Serialisieren())
                .setPrettyPrinting()
                .create();

        // Alle JSON-Dateien im Verzeichnis durchgehen
        File dir = new File(directoryName);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));

        if (files != null) {
            for (File file : files) {
                // Kontoname ist der Dateiname ohne .json
                String accountName = file.getName().replace(".json", "");

                // JSON-Datei lesen
                try (Reader reader = Files.newBufferedReader(file.toPath())) {
                    // Liste von Transaktionen deserialisieren
                    Type transactionListType = new TypeToken<List<Transaction>>() {
                    }.getType();
                    List<Transaction> transactions = gson.fromJson(reader, transactionListType);

                    // In die Map eintragen
                    if (transactions == null) {
                        transactions = new ArrayList<>();
                    } else {
                        // null-Werte herausfiltern (falls vorhanden)
                        transactions = transactions.stream()
                                .filter(Objects::nonNull)
                                .collect(java.util.stream.Collectors.toList());
                    }

                    accountsToTransactions.put(accountName, transactions);

                } catch (Exception ex) {
                    // Fehlerhafte Dateien abfangen
                    System.err.println("FEHLER beim Laden von " + file.getName() + ": " + ex.getMessage());
                    // Leere Liste für fehlerhafte Dateien
                    accountsToTransactions.put(accountName, new ArrayList<>());
                }
            }
        }
    }

    /**
     * Schreibt das angegebene Konto ins Dateisystem (persistiert es als JSON).
     *
     * @param account Name des zu speichernden Kontos
     * @throws IOException wenn ein Fehler beim Schreiben auftritt
     */

    private void writeAccount(String account) throws IOException {
        // Gson mit Custom Serializer/Deserializer konfigurieren
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Transaction.class, new De_Serialisieren())
                .setPrettyPrinting()
                .create();

        // Transaktionen des Kontos holen
        List<Transaction> transactions = accountsToTransactions.get(account);
        if (transactions == null) {
            transactions = new ArrayList<>();
        }

        // Pfad zur JSON-Datei
        Path filePath = Path.of(directoryName, account + ".json");

        // In JSON-Datei schreiben
        try (Writer writer = Files.newBufferedWriter(filePath)) {
            Type transactionListType = new TypeToken<List<Transaction>>() {}.getType();
            gson.toJson(transactions, transactionListType, writer);
        }
    }
}
/*
-   readAccounts() sollte im Konstruktor aufgerufen werden, um bestehende Konten zu laden
-   writeAccount() sollte immer dann aufgerufen werden, wenn sich ein Konto ändert:
-       -Nach createAccount()
-       -Nach addTransaction()
-       -Nach removeTransaction()
*/
