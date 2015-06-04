#!/bin/bash
# First argument is directory containing benchmarks directory,
# Second argument is compile script
# Can also use environment variables BENCHMARK and COMPILE
${BENCHMARK=$(readlink -f $1)}

shift
${COMPILE=$(readlink -f $1)}

shift
${OUTPUTDIR=$(readlink -f $1)}

gccO0csv=$OUTPUTDIR/gccO0.csv
gccO2csv=$OUTPUTDIR/gccO2.csv
noOptcsv=$OUTPUTDIR/NoOpt.csv
allOptcsv=$OUTPUTDIR/AllOpt.csv
echo "Test Name,Seconds Taken,Pass/Fail" > $gccO0csv
echo "Test Name,Seconds Taken,Pass/Fail" > $gccO2csv
echo "Test Name,Seconds Taken,Pass/Fail" > $noOptcsv
echo "Test Name,Seconds Taken,Pass/Fail" > $allOptcsv

cd $BENCHMARK/benchmarks

c0=0
c2=0
zero=0
three=0

for benchtest in `ls`; do
   cd $benchtest
   echo "Testing $benchtest..."
   bash $COMPILE $benchtest
   
   temp0=$(date +%s%N)
   ./$benchtest.gcc0.out < input 3>test_out 2>&1 1>&3
   temp0=$(echo "scale=9;$(($(date +%s%N)-temp0))/1000000000"|bc -l)
   diff -q output test_out
   echo "$benchtest,$temp0,$?" >> $gccO0csv
   
   temp1=$(date +%s%N)
   ./$benchtest.gcc2.out < input 3>test_out 2>&1 1>&3
   temp1=$(echo "scale=9;$(($(date +%s%N)-temp1))/1000000000"|bc -l)
   diff -q output test_out
   echo "$benchtest,$temp1,$?" >> $gccO2csv
   
   temp2=$(date +%s%N)
   ./$benchtest.0.out < input 3>test_out 2>&1 1>&3
   temp2=$(echo "scale=9;$(($(date +%s%N)-temp2))/1000000000"|bc -l)
   diff -q output test_out
   echo "$benchtest,$temp2,$?" >> $noOptcsv
   
   temp3=$(date +%s%N)
   ./$benchtest.3.out < input 3>test_out 2>&1 1>&3
   temp3=$(echo "scale=9;$(($(date +%s%N)-temp3))/1000000000"|bc -l)
   diff -q output test_out
   echo "$benchtest,$temp3,$?" >> $allOptcsv
   
   c0=$(echo $c0 + $temp0|bc)
   c2=$(echo $c2 + $temp1|bc)
   zero=$(echo $zero + $temp2|bc)
   three=$(echo $three + $temp3|bc)
   
   cd ..
done

echo "GCC -O0 took $c0 seconds"
echo "GCC -O2 took $c2 seconds"
echo "No opts took $zero seconds"
echo "All opts took $three seconds"
