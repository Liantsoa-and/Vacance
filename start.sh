#!/bin/bash

echo "========================================="
echo "    PROJET ROUTE - Compilation & Lancement"
echo "========================================="
echo

# Configuration
SRC="src/main/java"
BIN="bin"
LIB="lib"                           # Dossier contenant les jars
MAIN_CLASS="view.MainFrame"         # Classe principale

# Créer le dossier bin si nécessaire
mkdir -p "$BIN"

echo "[1/2] Compilation en cours..."

# Trouver tous les fichiers java
find "$SRC" -name "*.java" > sources.txt

# Compiler
javac -cp "$LIB/*" -d "$BIN" -sourcepath "$SRC" -encoding UTF-8 @sources.txt
COMPILE_STATUS=$?

rm -f sources.txt

if [ $COMPILE_STATUS -ne 0 ]; then
    echo
    echo "========================================="
    echo "XXXXXX ECHEC DE LA COMPILATION XXXXXX"
    echo "========================================="
    exit 1
fi

echo
echo "========================================="
echo "     Compilation réussie !"
echo "========================================="
echo

echo "[2/2] Lancement de l'application..."
java -cp "$BIN:$LIB/*" "$MAIN_CLASS"

echo
echo "========================================="
echo "     Fin de l'application"
echo "========================================="
