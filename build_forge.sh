#!/usr/bin/env bash
set -e
export JAVA_HOME=/home/sauron/.jdks/jbr-21.0.8
export PATH=$JAVA_HOME/bin:$PATH
BUILD_ONLY_CURRENT_PLATFORM=true

echo 'Starting KorGE Forge build script!'

if [ "$BUILD_ONLY_CURRENT_PLATFORM" = "true" ]; then
  echo 'Starting building for current platform...'
  ./installers.cmd -Dintellij.build.target.os=current
else
  echo 'Starting building for all platforms...'
  ./installers.cmd
fi

echo 'Find the built KorGE Forge in out/korgeforge/artifacts/ and extract it'
echo 'Run with ./korge.sh in bin/'
