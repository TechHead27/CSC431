#!/bin/bash
java Compile $1.mini > $1.s
gcc $1.s -o $1.out
