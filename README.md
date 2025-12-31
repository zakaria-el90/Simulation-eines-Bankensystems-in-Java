# Simulation eines Bankensystems (JavaFX)

Dieses Projekt simuliert ein einfaches Bankensystem mit grafischer Oberfläche (JavaFX). Nutzer:innen können Konten anlegen, Transaktionen verwalten und den Kontostand einsehen. Die Daten werden lokal in JSON-Dateien gespeichert.

## Funktionen

- **Konten verwalten**: Konten anlegen, auswählen und löschen.
- **Transaktionen**: Zahlungen und Überweisungen hinzufügen sowie entfernen.
- **Übersicht**: Kontostand anzeigen und Transaktionen filtern oder sortieren.
- **Persistenz**: Konten und Transaktionen werden in `bank-data/` abgelegt (JSON).

## Projektstruktur (Kurzüberblick)

- `src/main/java/bank/` – Geschäftslogik (Bank, Konten, Transaktionen, Exceptions)
- `src/main/java/UI/` – JavaFX-Einstiegspunkt (`FxApplication`)
- `src/main/java/UI/controller/` – Controller für die Views
- `src/main/resources/` – FXML-Layouts
- `bank-data/` – Ablage der gespeicherten Konten (wird zur Laufzeit befüllt)

## Voraussetzungen

- **Java 17** (siehe `pom.xml`)
- **Maven**

## Starten

```bash
mvn javafx:run
```

Das Hauptfenster ermöglicht das Anlegen und Auswählen von Konten. Über die Account-Ansicht lassen sich Transaktionen hinzufügen, löschen sowie nach Kriterien filtern/sortieren.

## Hinweise

- Beim Löschen eines Kontos werden die zugehörigen Daten auch von der Festplatte entfernt.
- Die JavaFX-Anwendung nutzt `UI.FxApplication` als Einstiegspunkt.

## Lizenz

Dieses Projekt ist eine Studien-/Übungsaufgabe und enthält keine explizite Lizenz.
