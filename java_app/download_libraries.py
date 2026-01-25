#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Scarica le librerie JAR necessarie per la compilazione Java
"""

import os
import urllib.request
import shutil
from pathlib import Path

SCRIPT_DIR = Path(__file__).resolve().parent
JAR_DIR = SCRIPT_DIR / "lib"
JAR_DIR.mkdir(parents=True, exist_ok=True)

# Repository Maven
MAVEN_REPO = "https://repo1.maven.org/maven2"

# Librerie da scaricare (format: filename, artifact path)
LIBRARIES = [
    # Apache POI
    ("poi-5.2.5.jar", 
     "org/apache/poi/poi/5.2.5/poi-5.2.5.jar"),
    ("poi-ooxml-5.2.5.jar", 
     "org/apache/poi/poi-ooxml/5.2.5/poi-ooxml-5.2.5.jar"),
    
    # Dipendenze POI
    ("commons-io-2.13.0.jar",
     "commons-io/commons-io/2.13.0/commons-io-2.13.0.jar"),
    ("commons-compress-1.24.0.jar",
     "org/apache/commons/commons-compress/1.24.0/commons-compress-1.24.0.jar"),
    ("commons-codec-1.15.jar",
     "commons-codec/commons-codec/1.15/commons-codec-1.15.jar"),
    ("commons-collections4-4.4.jar",
     "org/apache/commons/commons-collections4/4.4/commons-collections4-4.4.jar"),
    ("commons-logging-1.2.jar",
     "commons-logging/commons-logging/1.2/commons-logging-1.2.jar"),
    ("commons-math3-3.6.1.jar",
     "org/apache/commons/commons-math3/3.6.1/commons-math3-3.6.1.jar"),
    ("log4j-api-2.22.1.jar",
     "org/apache/logging/log4j/log4j-api/2.22.1/log4j-api-2.22.1.jar"),
    ("log4j-core-2.22.1.jar",
     "org/apache/logging/log4j/log4j-core/2.22.1/log4j-core-2.22.1.jar"),
    
    # XML
    ("xmlbeans-5.1.1.jar",
     "org/apache/xmlbeans/xmlbeans/5.1.1/xmlbeans-5.1.1.jar"),
    
    # Altro
    ("isoparser-1.1.22.jar",
     "com/googlecode/mp4parser/isoparser/1.1.22/isoparser-1.1.22.jar"),
    ("metadata-extractor-2.18.0.jar",
     "com/drewnoakes/metadata-extractor/2.18.0/metadata-extractor-2.18.0.jar"),
]

print("[INFO] Download delle librerie JAR...")
print(f"[INFO] Destinazione: {JAR_DIR}")

failed = []
success = 0

for filename, artifact_path in LIBRARIES:
    filepath = JAR_DIR / filename
    
    # Salta se già esiste
    if filepath.exists():
        print(f"[SKIP] {filename} (già presente)")
        success += 1
        continue
    
    url = f"{MAVEN_REPO}/{artifact_path}"
    print(f"[DOWN] {filename}... ", end="", flush=True)
    
    try:
        urllib.request.urlretrieve(url, filepath)
        print("OK")
        success += 1
    except Exception as e:
        print(f"ERRORE: {e}")
        failed.append((filename, str(e)))

# Rimuovi versioni obsolete che possono causare conflitti
legacy_commons_io = JAR_DIR / "commons-io-2.11.0.jar"
if legacy_commons_io.exists():
    try:
        legacy_commons_io.unlink()
        print("[CLEAN] Rimosso commons-io-2.11.0.jar (obsoleto)")
    except Exception as e:
        print(f"[WARN] Impossibile rimuovere {legacy_commons_io}: {e}")

print("\n" + "="*60)
print(f"[DONE] {success} librerie scaricate")

if failed:
    print(f"\n[WARN] {len(failed)} librerie hanno avuto errori:")
    for lib, error in failed:
        print(f"  - {lib}: {error}")
    print("\nPuoi scaricarle manualmente oppure usare Maven.")
else:
    print("\n[OK] Tutte le librerie scaricate correttamente!")

print("\nProssimi step:")
print("1. cd f:\\PAM\\java_app")
print("2. javac -cp lib/* -d build src/*.java")
print("3. jar -cmf manifest.txt dist/PAM-PDF-to-Excel.jar -C build .")
