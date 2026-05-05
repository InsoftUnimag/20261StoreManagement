#!/bin/bash
echo " Configurando entorno..."
git config core.hooksPath .githooks
echo "Hooks configurados"
git flow init -d
echo "Git-flow inicializado"
echo "Listo para trabajar"
