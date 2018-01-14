#!/bin/bash

#SBATCH --mem=60G
#SBATCH --time=8:00:00
#SBATCH --nodes=1
#SBATCH --cpus-per-task=12
#SBATCH --job-name=select-251

module try-load jdk/1.8.0_111
module try-load pbzip2/intel/1.1.13

module try-load apache-ant/1.9.8
ant clean compile || exit 1

corpus=`mktemp --directory --tmpdir=$BEEGFS`
run=$SCRATCH/zrt/wsj/2017_1126

cp=(
    bin
    lib/lucene-core-7.2.0.jar
    lib/lucene-queryparser-7.2.0.jar
    lib/lucene-analyzers-common-7.2.0.jar
)

output=$run/selection
if [ ! -d $output ]; then
    mkdir $output
fi

tar \
    --extract \
    --use-compress-prog=pbzip2 \
    --directory=$corpus \
    --file=$run/pseudoterms/05.tar.bz
find $corpus -name 'WSJQ*' -delete

java \
    -Xmx$(printf "%0.f" $(bc -l <<< "60 * .95"))g \
    -XX:+UseParallelGC \
    -XX:ParallelGCThreads=12 \
    -classpath `sed -e's/ /:/g'<<< ${cp[@]}` \
    exec.InteractiveRetriever \
    $BEEGFS/corpus/pause_clobber_lower_symbol/WSJQ00251-0000 \
    $corpus/05 \
    `mktemp --directory --tmpdir=\$SLURM_TMPDIR` \
    $BEEGFS/qrels/251 \
    $output/sequential \
    1000 \
    12

rm --recursive --force $corpus
