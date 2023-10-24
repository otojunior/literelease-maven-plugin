# --- Maven Release ---
# Cria um Release Maven
# Autor: Oto Soares Coelho Junior
# Data: 24/10/2023

import argparse
import os

psr = argparse.ArgumentParser(description="Cria um Release Maven")
psr.add_argument("-rv", "--releaseVersion", required=True, help="Versão de Release")
psr.add_argument("-rm", "--releaseMessage", required=True, help="Mensagem de Release")
psr.add_argument("-sv", "--snapshotVersion", required=True, help="Versão do próximo Snapshot")
psr.add_argument("-sm", "--snapshotMessage", required=True, help="Mensagem do próximo Snapshot")
psr.add_argument("-p", "--push", required=True, default=False, help="Executa um Git Push na árvore e tags")
args = psr.parse_args()

comandos=(
    f"mvn versions:set -DgenerateBackupPoms=false -DnewVersion={args.releaseVersion}",
    f"git add pom.xml",
    f"git commit -m \"{args.releaseMessage}\"",
    f"git tag v{args.releaseVersion}",
    f"mvn versions:set -DgenerateBackupPoms=false -DnewVersion={args.snapshotVersion}",
    f"git add pom.xml",
    f"git commit -m \"{args.snapshotMessage}\"")

for comando in comandos:
    os.system(comando)

if args.push:
    os.system("git push")
    os.system("git push --tags")