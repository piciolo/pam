# Build dell'app Cartelli PAM

Guida passo-passo per compilare ed eseguire l'app sul tuo PC (Windows, progetto
estratto in `F:\PAM\cartelli-android`).

> Perché la build non è stata fatta nell'ambiente cloud: lì l'accesso a
> `dl.google.com` / `maven.google.com` (dove stanno Android Gradle Plugin,
> AndroidX, Compose, CameraX, ML Kit e l'SDK) è bloccato dalla policy di rete.
> Sul tuo PC, con internet normale, è tutto standard.

## 1. Prerequisiti (una tantum)

1. Installa **Android Studio** (versione Koala 2024.1 o successiva):
   https://developer.android.com/studio
2. Al primo avvio, l'installer guidato scarica l'**Android SDK**. Assicurati di
   avere installato (SDK Manager → *SDK Platforms* / *SDK Tools*):
   - **Android 14 (API 34)** — platform
   - **Android SDK Build-Tools** 34.x
   - **Android SDK Platform-Tools**
   - (per l'emulatore) un *system image* con Google APIs
3. Installa il **JDK 17** se Android Studio non lo include già (di norma è
   incluso come JBR 17).

## 2. Apri il progetto

1. Android Studio → **Open**.
2. Seleziona la cartella **`F:\PAM\cartelli-android`** (quella che contiene
   `settings.gradle.kts`), non la cartella superiore.
3. Alla prima apertura parte il **Gradle Sync**: scarica AGP, le librerie
   AndroidX/Compose/CameraX/ML Kit, ecc. (qualche minuto, serve internet).
   - Se chiede di installare componenti SDK mancanti o di accettare licenze →
     **accetta** ("Accept" / "Install").
   - `local.properties` con `sdk.dir=...` viene creato automaticamente da Android
     Studio: non serve crearlo a mano (ed è giustamente ignorato da git).

## 3. Esegui

### Su telefono fisico (consigliato, per la fotocamera)
1. Sul telefono: *Impostazioni → Opzioni sviluppatore → Debug USB* attivo.
2. Collega via USB, autorizza il PC.
3. In Android Studio scegli il device in alto e premi **Run ▶** (oppure
   `Shift+F10`).
4. Concedi il permesso **Fotocamera** quando richiesto.

### Su emulatore
- Crea un AVD (Device Manager → Create) con API 34 e *Google APIs*. La fotocamera
  dell'emulatore è simulata: la scansione barcode funziona meglio su telefono
  vero. Per provarla nell'emulatore puoi mostrare un'immagine di un barcode alla
  "fotocamera virtuale".

## 4. Da riga di comando (opzionale)

Dalla cartella `cartelli-android`:

```bat
gradlew.bat assembleDebug      :: genera app\build\outputs\apk\debug\app-debug.apk
gradlew.bat installDebug       :: installa su device/emulatore collegato
gradlew.bat test               :: esegue gli unit test (logica prezzo, parsing peso)
```

L'APK firmato di debug si trova in
`app\build\outputs\apk\debug\app-debug.apk` e si può installare anche
manualmente sul telefono.

## 5. Test rapido end-to-end

1. Avvia l'app → schermata **Scansione**: inquadra il codice a barre di un
   prodotto reale (es. Lavazza Qualità Rossa).
2. L'app cerca su Open Food Facts e apre l'**editor** con nome e peso
   precompilati.
3. **Scatta** la foto del prodotto → compare nell'area del cartello.
4. Inserisci **prezzo** e **footer**; controlla il prezzo al lt/kg e (se metti il
   prezzo base) la box sconto −%.
5. **Stampa / PDF** → si apre il dialog di sistema (stampante o "Salva come PDF").
6. **Impostazioni** (icona ingranaggio) → cambia dimensioni/posizione dell'area
   foto in mm.

## 6. Problemi comuni

- **Gradle sync lento / fallisce la prima volta**: di solito è la rete che
  scarica le dipendenze. Riprova *File → Sync Project with Gradle Files*.
- **"SDK location not found"**: apri *File → Project Structure → SDK Location*,
  oppure lascia che Android Studio crei `local.properties`.
- **Licenze SDK non accettate** (da CLI): esegui
  `"%ANDROID_HOME%\cmdline-tools\latest\bin\sdkmanager" --licenses` e accetta.
- **La fotocamera non parte**: verifica il permesso Fotocamera nelle impostazioni
  app del telefono.
