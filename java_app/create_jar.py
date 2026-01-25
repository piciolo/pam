#!/usr/bin/env python3
"""
Crea il file JAR usando Python (quando jar.exe non è disponibile)
"""
import os
import sys
import zipfile
from pathlib import Path

def create_jar():
    """Crea il JAR file con i file compilati"""
    
    build_dir = Path("build")
    dist_dir = Path("dist")
    jar_file = dist_dir / "PAM-PDF-to-Excel.jar"
    
    # Crea cartella dist se non esiste
    dist_dir.mkdir(exist_ok=True)
    
    # Leggi manifest
    manifest_file = Path("manifest.txt")
    if not manifest_file.exists():
        print("[ERROR] manifest.txt non trovato")
        return False
    
    manifest_content = manifest_file.read_text()
    
    # Crea JAR
    try:
        with zipfile.ZipFile(jar_file, 'w', zipfile.ZIP_DEFLATED) as jar:
            # Aggiungi manifest
            jar.writestr('META-INF/MANIFEST.MF', manifest_content)
            
            # Aggiungi file .class dalla cartella build
            if build_dir.exists():
                for class_file in build_dir.rglob("*.class"):
                    arcname = str(class_file.relative_to(build_dir))
                    jar.write(class_file, arcname)
                    print(f"  [+] {arcname}")
            
        print(f"\n[OK] JAR creato: {jar_file}")
        print(f"[OK] Dimensione: {jar_file.stat().st_size} bytes")
        return True
        
    except Exception as e:
        print(f"[ERROR] Errore nella creazione del JAR: {e}")
        return False

if __name__ == "__main__":
    success = create_jar()
    sys.exit(0 if success else 1)
