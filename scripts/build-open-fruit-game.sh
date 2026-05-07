#!/usr/bin/env bash
set -euo pipefail

git clone --depth 1 https://github.com/emmaguy/FruitNinja.git upstream-fruitninja
cd upstream-fruitninja
echo "Upstream commit: $(git rev-parse HEAD)" > UPSTREAM_VERSION.txt

cat > settings.gradle <<'EOF'
pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }
dependencyResolutionManagement { repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS); repositories { google(); mavenCentral() } }
rootProject.name='OpenSourceFruitGame'
EOF

cat > build.gradle <<'EOF'
plugins { id 'com.android.application' version '8.5.2' }

android {
    namespace 'dev.emmaguy.fruitninja'
    compileSdk 35
    defaultConfig {
        applicationId 'dev.emmaguy.fruitninja'
        minSdk 23
        targetSdk 35
        versionCode 1
        versionName '1.0-open-source'
    }
    sourceSets {
        main {
            manifest.srcFile 'AndroidManifest.xml'
            java.srcDirs = ['src']
            res.srcDirs = ['res']
        }
    }
}

dependencies { implementation 'com.android.support:support-v4:28.0.0' }
EOF

cat > gradle.properties <<'EOF'
android.suppressUnsupportedCompileSdk=35
android.enableJetifier=false
android.useAndroidX=false
EOF

python3 - <<'PY'
from pathlib import Path
p = Path('AndroidManifest.xml')
s = p.read_text()
s = s.replace('android:theme="@android:style/Theme.Light.NoTitleBar" >', 'android:theme="@android:style/Theme.Light.NoTitleBar"\n            android:exported="true" >')
p.write_text(s)
PY

gradle assembleDebug
cd ..
mkdir -p dist-opensource-fruit-game
cp upstream-fruitninja/build/outputs/apk/debug/upstream-fruitninja-debug.apk dist-opensource-fruit-game/OpenSource-FruitGame-debug.apk
cp upstream-fruitninja/LICENSE dist-opensource-fruit-game/LICENSE-Apache-2.0.txt
cp upstream-fruitninja/README.md dist-opensource-fruit-game/README-upstream.md
cp upstream-fruitninja/UPSTREAM_VERSION.txt dist-opensource-fruit-game/UPSTREAM_VERSION.txt
ls -lh dist-opensource-fruit-game
