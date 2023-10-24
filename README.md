# LiteRelease-Maven-Plugin

## Exempo de Uso:

```
python release.py \
    -rv 1.0.0 \
    -rm "[Release 1.0.0]" \
    -sv 1.1.0-SNAPSHOT \
    -sm "[Próximo Snapshot 1.1.0-SNAPSHOT]" \
    -p True
```

## Build do Executável:

```
pyinstaller --clean --onefile release.py
```
