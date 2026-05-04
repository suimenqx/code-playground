#!/usr/bin/env bash
set -euo pipefail
# Requires Android Studio or Android SDK + Gradle installed locally.
gradle assembleDebug
ls -lh app/build/outputs/apk/debug/*.apk
