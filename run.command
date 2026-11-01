#!/bin/zsh
cd -- "$(dirname -- "$0")"
exec mvn javafx:run
