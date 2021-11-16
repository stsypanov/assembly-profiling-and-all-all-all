#!/bin/bash
declare -i count=0;
declare -i count_in_file=0;
for i in *.txt;
do
  count_in_file=$((`grep -o -i 'putfield count' $i | wc -l`));
  count=$((count + count_in_file));
done
echo $count