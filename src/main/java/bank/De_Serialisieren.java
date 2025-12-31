package bank;

import com.google.gson.*;

import java.lang.reflect.Type;

public class De_Serialisieren implements JsonSerializer<Transaction>, JsonDeserializer<Transaction> {

    /**
     * Serialisiert ein Transaction-Objekt in ein JSON-Objekt.
     *
     * @param transaction  das zu serialisierende Objekt
     * @param type         der Typ der Klasse (hier: Transaction)
     * @param jsonSerializationContext Kontext für verschachtelte Serialisierung
     * @return ein JSON-Objekt, das die Klasse und ihre Felder enthält
     */
    @Override
    public JsonElement serialize(Transaction transaction, Type type, JsonSerializationContext jsonSerializationContext) {

        // Grundstruktur für das Ausgabe-JSON
        JsonObject jsonObject = new JsonObject();   // Speichert CLASSNAME + INSTANCE
        JsonObject jsonInstance = new JsonObject(); // Speichert die tatsächlichen Felder

        // --- IncomingTransfer serialisieren ---

        if (transaction instanceof IncomingTransfer inT) { //wenn richtig wird Auto: IncomingTransfer inT = (IncomingTransfer) transaction;

            jsonObject.addProperty("CLASSNAME", "IncomingTransfer");
            jsonInstance.addProperty("sender", inT.getSender());
            jsonInstance.addProperty("recipient", inT.getRecipient());
            jsonInstance.addProperty("date", inT.getDate());
            jsonInstance.addProperty("amount", inT.getAmount());
            jsonInstance.addProperty("description", inT.getDescription());
            jsonObject.add("INSTANCE", jsonInstance);

        }
        // --- OutgoingTransfer serialisieren ---
        else if (transaction instanceof OutgoingTransfer outT) {

            jsonObject.addProperty("CLASSNAME", "OutgoingTransfer");
            jsonInstance.addProperty("sender", outT.getSender());
            jsonInstance.addProperty("recipient", outT.getRecipient());
            jsonInstance.addProperty("date", outT.getDate());
            jsonInstance.addProperty("amount", outT.getAmount());
            jsonInstance.addProperty("description", outT.getDescription());
            jsonObject.add("INSTANCE", jsonInstance);

        }
        // --- Payment serialisieren ---
        else if (transaction instanceof Payment tmpPayment) {

            jsonObject.addProperty("CLASSNAME", "Payment");
            jsonInstance.addProperty("incomingInterest", tmpPayment.getIncomingInterest());
            jsonInstance.addProperty("outgoingInterest", tmpPayment.getOutgoingInterest());
            jsonInstance.addProperty("date", tmpPayment.getDate());
            jsonInstance.addProperty("amount", tmpPayment.getAmount());
            jsonInstance.addProperty("description", tmpPayment.getDescription());
            jsonObject.add("INSTANCE", jsonInstance);
        }

        return jsonObject;
    }

    /**
     * Deserialisiert ein JSON-Objekt zurück in ein Transaction-Objekt.
     *
     * @param jsonElement  das JSON, das eingelesen wird
     * @param type         erwarteter Rückgabetyp (Transaction)
     * @param jsonDeserializationContext Kontext für verschachtelte Deserialisierung
     * @return ein konkretes Transaction-Objekt (Payment, Transfer, etc.)
     */
    @Override
    public Transaction deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

        // JSON-Element in ein Objekt umwandeln
        JsonObject jsonObject2 = jsonElement.getAsJsonObject();

        // "INSTANCE" enthält die eigentlichen Felder des Objekts
        JsonElement instanceElement = jsonObject2.get("INSTANCE");
        if (instanceElement == null || instanceElement.isJsonNull()) {
            System.err.println("FEHLER: der Schlüssel INSTANCE existiert nicht oder der Wert von INSTANCE ist null");
            throw new JsonParseException("Ungültige JSON-Struktur: INSTANCE fehlt oder ist null");
        }

        JsonObject instance = instanceElement.getAsJsonObject();

        // Aus CLASSNAME bestimmen wir die Zielklasse
        String classname = jsonObject2.get("CLASSNAME").getAsString();

        // Je nach CLASSNAME die passende Unterklasse erzeugen
        return switch (classname) {

            // Bei Payment: Standard-Deserialisierung reicht aus
            case "Payment" ->
                    new Gson().fromJson(instance, Payment.class);

            // Achtung: Es wird eine neue Gson()-Instanz ohne Custom-Deserializer benutzt.
            // Deshalb verwendet Gson hier die Standard-Deserialisierung.
            // Das funktioniert, da die JSON-Felder exakt den Attributen der Klasse entsprechen.
            case "OutgoingTransfer" ->
                    new Gson().fromJson(instance, OutgoingTransfer.class);

            case "IncomingTransfer" ->
                    new Gson().fromJson(instance, IncomingTransfer.class);

            case "Transfer" ->
                    new Gson().fromJson(instance, Transfer.class);

            // Falls ein falscher oder unbekannter CLASSNAME vorkommt → Fehler werfen
            default ->
                    throw new JsonParseException("Falsche Type");
        };
    }
}
