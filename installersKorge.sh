#!/bin/bash

set -e

#git fetch --tags git@github.com:JetBrains/intellij-community.git

#./installers.cmd -Dintellij.build.incremental.compilation=true

# save build.txt into environment variable
export BUILD_TXT=$(cat build.txt)

unzip -oj "out/korgeforge/artifacts/korgeforge-$BUILD_TXT-aarch64.win.zip" "product-info.json" -d "out/korgeforge/dist.win.aarch64"
unzip -oj "out/korgeforge/artifacts/korgeforge-$BUILD_TXT.win.zip" "product-info.json" -d "out/korgeforge/dist.win.x64"
unzip -oj "out/korgeforge/artifacts/korgeforge-$BUILD_TXT.sit" "KorGE Forge.app/Contents/Resources/product-info.json" -d "out/korgeforge/dist.mac.x64/Resources"
unzip -oj "out/korgeforge/artifacts/korgeforge-$BUILD_TXT-aarch64.sit" "KorGE Forge.app/Contents/Resources/product-info.json" -d "out/korgeforge/dist.mac.aarch64/Resources"
cp ./out/korgeforge/temp/linux.dist.product-info.json-aarch64/product-info.json ./out/korgeforge/dist.unix.aarch64/
cp ./out/korgeforge/temp/linux.dist.product-info.json/product-info.json ./out/korgeforge/dist.unix.x64/

pushd out/korgeforge
rm dist.tar.zst || true
#tar -cvf - dist.* | zstd -7 -T0 -o dist.tar.zst # 528 MB
#tar -cvf - dist.* | zstd -9 -T0 -o dist.tar.zst # 517 MB
#tar -cvf - dist.* | zstd -14 -T0 -o dist.tar.zst # 511 MB
#tar -cvf - dist.* --exclude='.DS_Store' | zstd -19 -T0 -of dist.tar.zst
tar -cvf - dist.* --exclude='.DS_Store' | zstd --ultra -22 -T0 -of dist.tar.zst
popd