#!/bin/bash
TARGET_DIR=$( pwd )
cd "$( dirname "${BASH_SOURCE[0]}" )"
java Compile $TARGET_DIR/$1.mini > $TARGET_DIR/$1.s
gcc $TARGET_DIR/$1.s -o $TARGET_DIR/$1.out
