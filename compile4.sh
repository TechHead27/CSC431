#!/bin/bash
TARGET_DIR=$( pwd )
cd "$( dirname "${BASH_SOURCE[0]}" )"
gcc $TARGET_DIR/$1.c -O0 -o $TARGET_DIR/$1.gcc0.out
gcc $TARGET_DIR/$1.c -O2 -o $TARGET_DIR/$1.gcc2.out
java Compile $TARGET_DIR/$1.mini > $TARGET_DIR/$1.s 2>/dev/null
gcc $TARGET_DIR/$1.s -o $TARGET_DIR/$1.0.out
java Compile -const -copy -useless $TARGET_DIR/$1.mini > $TARGET_DIR/$1.s 2>/dev/null
gcc $TARGET_DIR/$1.s -o $TARGET_DIR/$1.3.out
