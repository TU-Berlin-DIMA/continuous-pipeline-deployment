#!/usr/bin/env bash
# Copy jar files
scp target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar behrouz@cloud-11.dima.tu-berlin.de:/home/behrouz/jar
cp /home/behrouz/jar/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar /share/hadoop/behrouz/jars/

#Local
~/Documents/frameworks/spark/2.2.0/bin/spark-submit --class de.dfki.experiments.OptimizationTimes --master "spark://wlan-141-23-215-63.tubit.tu-berlin.de:7077" --driver-memory 3G --executor-memory 6G /Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar "profile=url" "result=/Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/url-reputation/optimization-effect"

# cluster
/share/hadoop/behrouz/spark/stable/bin/spark-submit --class de.dfki.experiments.OptimizationTimes --master "spark://cloud-11.dima.tu-berlin.de:7077" /share/hadoop/behrouz/jars/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar "result=/share/hadoop/behrouz/experiments/url/results/optimization-effect" "profile=url-cluster" 


# copy the results to local machine
scp -r behrouz@cloud-11.dima.tu-berlin.de:/share/hadoop/behrouz/experiments/url/results/optimization-effect/ /Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/url-cluster/
