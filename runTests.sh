#!/bin/bash
# First argument is directory containing benchmarks directory,
# Second argument is compile script
# Can also use environment variables BENCHMARK and COMPILE
${BENCHMARK=$(readlink -f $1)}
#if [ $? -ne 126 ]; then exit; fi

shift
${COMPILE=$(readlink -f $1)}
#if [ $? -ne 126 ]; then exit; fi

cd $BENCHMARK/benchmarks
if [ $? -ne 0 ]; then exit; fi
total=0

for benchtest in `ls`; do
   cd $benchtest
   echo "Testing $benchtest..."
   bash $COMPILE $benchtest
   timeout --signal=KILL 60 ./$benchtest.out < input 2> /dev/null | head --bytes=1M  > test_out
   diff -q output test_out
   if [ $? -eq 0 ]; then total=$((total+1)); fi
   cd ..
done

echo "$total tests passed."
