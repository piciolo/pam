#!/usr/bin/env python3
"""
Estrae le classi dal JAR e le copia in una cartella 'classes'
"""
import zipfile
from pathlib import Path

def extract_classes():
    """Estrae le classi dal JAR"""
    
    jar_file = Path("dist/PAM-PDF-to-Excel.jar")
    classes_dir = Path("classes")
    
    # Crea cartella classes
    classes_dir.mkdir(exist_ok=True)
    
    try:
        with zipfile.ZipFile(jar_file, 'r') as z:
            # Estrai solo i .class file
            for name in z.namelist():
                if name.endswith('.class'):
                    # Estrai il file
                    data = z.read(name)
                    class_file = classes_dir / name
                    class_file.parent.mkdir(parents=True, exist_ok=True)
                    class_file.write_bytes(data)
                    print(f"[+] Estratto: {name}")
        
        print(f"\n[OK] Classi estratte in: classes\\")
        return True
        
    except Exception as e:
        print(f"[ERROR] Errore: {e}")
        return False

if __name__ == "__main__":
    extract_classes()
