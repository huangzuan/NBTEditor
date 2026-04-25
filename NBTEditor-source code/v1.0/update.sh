#!/bin/sh

APP_DIR="$1"
DATA_DIR="$2"

cd "$APP_DIR"

sleep 2

while pgrep -f "NBTEditor.jar" > /dev/null; do
    sleep 1
done

unzip -o "$DATA_DIR/downloads/update.zip" -d .

java -jar NBTEditor.jar &
