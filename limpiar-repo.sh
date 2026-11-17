#!/usr/bin/env bash
# Deja de versionar archivos generados que no deben estar en el repositorio
# (clases compiladas, reportes de Maven y configuración del IDE).
# Ejecutar UNA vez desde la raíz del repositorio y luego hacer commit.
set -e
git rm -r --cached --ignore-unmatch -q target .idea
echo "Listo. Revisa con 'git status' y haz commit."
