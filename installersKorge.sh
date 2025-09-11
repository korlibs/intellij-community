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
chmod +x out/korgeforge/dist.mac.x64/MacOS/korge
chmod +x out/korgeforge/dist.mac.aarch64/MacOS/korge
rm -rf ./out/korgeforge/dist.all/lib/jna
rm -rf ./out/korgeforge/dist.all/lib/native
rm -rf ./out/korgeforge/dist.all/lib/pty4j

mkdir -p ./out/korgeforge/dist.mac.aarch64/lib/jna/aarch64
mkdir -p ./out/korgeforge/dist.mac.x64/lib/jna/amd64
mkdir -p ./out/korgeforge/dist.mac.aarch64/lib/native/mac-aarch64
mkdir -p ./out/korgeforge/dist.mac.x64/lib/native/mac-x86_64
mkdir -p ./out/korgeforge/dist.mac.aarch64/lib/pty4j/darwin
mkdir -p ./out/korgeforge/dist.mac.x64/lib/pty4j/darwin

cp -rf ./out/korgeforge/temp/jna/darwin-aarch64/libjnidispatch.jnilib ./out/korgeforge/dist.mac.aarch64/lib/jna/aarch64/
cp -rf ./out/korgeforge/temp/jna/darwin-x86-64/libjnidispatch.jnilib ./out/korgeforge/dist.mac.x64/lib/jna/amd64/
cp -rf ./out/korgeforge/temp/native/mac-aarch64/libsqliteij.jnilib ./out/korgeforge/dist.mac.aarch64/lib/native/mac-aarch64/
cp -rf ./out/korgeforge/temp/native/mac-x86_64/libsqliteij.jnilib ./out/korgeforge/dist.mac.x64/lib/native/mac-x86_64/
cp -rf ./out/korgeforge/temp/pty4j/darwin/libpty.dylib ./out/korgeforge/dist.mac.aarch64/lib/pty4j/darwin/
cp -rf ./out/korgeforge/temp/pty4j/darwin/libpty.dylib ./out/korgeforge/dist.mac.x64/lib/pty4j/darwin/

#cp -rf ./out/korgeforge/temp/jna ./out/korgeforge/dist.all/lib/jna
#cp -rf ./out/korgeforge/temp/native ./out/korgeforge/dist.all/lib/native
#cp -rf ./out/korgeforge/temp/pty4j ./out/korgeforge/dist.all/lib/pty4j
cp ./out/korgeforge/temp/linux.dist.product-info.json-aarch64/product-info.json ./out/korgeforge/dist.unix.aarch64/
cp ./out/korgeforge/temp/linux.dist.product-info.json/product-info.json ./out/korgeforge/dist.unix.x64/

export OUTBASE=forge-2024.2.0.2-dist
export OUT_TAR_ZST=$OUTBASE.tar.zst
export OUT_TAR_XZ=$OUTBASE.tar.xz

pushd out/korgeforge
  rm dist.tar.zst || true
  rm dist.tar.xz || true
  #rm $OUT_TAR_ZST || true
  #time tar -cvf - dist.* --exclude='.DS_Store' | xz -9 -T0 -f -z - > $OUT_TAR_XZ #
  #time tar -cvf - dist.* --exclude='.DS_Store' | xz -5 -T0 -f -z - > $OUT_TAR_XZ # 489,2 MB, 55s
  #time tar -cvf - dist.* --exclude='.DS_Store' | zstd -7 -T0 -of $OUT_TAR_ZST # 540 MB 3s
  #time tar -cvf - dist.* --exclude='.DS_Store' | zstd -9 -T0 -of $OUT_TAR_ZST # 528 MB 4s
  #time tar -cvf - dist.* --exclude='.DS_Store' | zstd -14 -T0 -of $OUT_TAR_ZST # 522 MB 18s
  time tar -cvf - dist.* --exclude='.DS_Store' | zstd -19 -T0 -of $OUT_TAR_ZST # 504 MB, 1m:27s
  #time tar -cvf - dist.* --exclude='.DS_Store' | zstd --ultra -22 -T0 -of $OUT_TAR_ZST #
popd
