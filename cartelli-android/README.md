# Cartelli PAM — App Android

App Android nativa per generare i **cartelli offerta PAM** (formato A4) partendo
dalla scansione del **codice a barre** del prodotto. Versione "app" della pagina
web `cartelli.html`.

## Cosa fa

1. **Inquadri il codice a barre** del prodotto (fotocamera + ML Kit on-device).
2. L'app cerca il prodotto su **Open Food Facts** (database online gratuito,
   nessuna API key) e **precompila nome e peso** (es. barcode Lavazza → "Lavazza
   Qualità Rossa", 250 g).
3. **Scatti la foto** del prodotto: viene messa sul cartello, ridimensionata
   nell'area che decidi tu (Impostazioni).
4. Inserisci **prezzo** e **testo a piè di pagina**; il prezzo al litro/kg e la
   box sconto −% si calcolano da soli (stessa logica della pagina web, con
   arrotondamento PAM per eccesso).
5. **Stampa / Salva come PDF** (A4) oppure **condividi** il PDF.

L'anteprima del cartello si aggiorna in tempo reale ed è una replica fedele del
layout di `cartelli.html` (testata OFFERTA rossa / "Pam Conviene" verde, logo,
nome, riga al lt/kg, prezzone con ombra, etichetta, footer).

## Requisiti

- **Android Studio** (Koala o successivo) — consigliato per build ed esecuzione.
- Android SDK con **API 34**; l'app gira da **Android 7 (API 24)** in su.
- Un telefono/emulatore con fotocamera per la scansione.

## Build

> Nota: l'app richiede dipendenze dal Maven di Google (`dl.google.com`). In
> ambienti con rete ristretta la build va fatta su una macchina con accesso a
> internet normale (es. Android Studio).

Da terminale:

```bash
cd cartelli-android
./gradlew assembleDebug        # genera app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug         # installa su device/emulatore collegato
./gradlew test                 # esegue gli unit test (logica prezzo, parsing peso)
```

Oppure apri la cartella `cartelli-android/` in Android Studio e premi **Run**.

## Architettura

- `domain/` — logica pura e testabile:
  - `PriceCalculator` — porting 1:1 del JavaScript di `cartelli.html`
    (prezzo al lt/kg, arrotondamento, box sconto, split prezzone).
  - `QuantityParser` — converte il "quantity" di Open Food Facts (es. "250 g",
    "6 x 33 cl") in formato + unità.
  - `CartelloState` / `CartelloComputed`.
- `data/off/` — `ProductRepository` + modelli per Open Food Facts (OkHttp +
  kotlinx.serialization, header User-Agent come da policy OFF).
- `data/settings/` — `SettingsRepository` (DataStore): area foto in mm, footer e
  arrotondamento di default.
- `camera/BarcodeAnalyzer` — analizzatore CameraX con ML Kit (EAN/UPC).
- `image/` — caricamento e ridimensionamento della foto (con rotazione EXIF).
- `render/` — `CartelloRenderer` (disegna il cartello su Canvas: **sorgente
  unica** per anteprima e PDF), `PdfExporter` (PDF A4 + stampa di sistema),
  `CartelloAssets` (font e immagini).
- `ui/` — schermate Compose: `scan` (fotocamera), `editor` (form + anteprima),
  `settings`; `CartelloViewModel` condiviso.

## Asset

Font e immagini sono estratti dalla pagina web originale:

- `res/font/pam_price.ttf` ← Blocklyn Condensed (font del prezzone)
- `res/font/pam_text.otf` ← Mont Heavy (nome / righe)
- `res/drawable/banner_offerta.png`, `banner_conviene.png`, `logo_pam.png`

## Test

Gli unit test della logica di calcolo prezzo e del parsing del peso sono in
`app/src/test/...` ed eseguibili con `./gradlew test` (verificano, tra l'altro,
il caso "66 cl a 1,19 € → al lt 1,81" con arrotondamento PAM).

## Note / sviluppi futuri

- Se un prodotto non è su Open Food Facts, inserisci i dati a mano (fallback già
  presente). In futuro si può aggiungere una cache locale dei prodotti visti.
- Il layout è una replica fedele ma rifinibile: le costanti di posizione sono in
  `CartelloRenderer` e si possono allineare al pixel confrontando con la pagina
  web.
