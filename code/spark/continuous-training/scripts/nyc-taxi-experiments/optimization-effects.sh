#!/usr/bin/env bash
# Copy jar files
scp target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar behrouz@cloud-11.dima.tu-berlin.de:/home/behrouz/jar
cp /home/behrouz/jar/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar /share/hadoop/behrouz/jars/

#Cluster
/share/hadoop/behrouz/spark/stable/bin/spark-submit --class de.dfki.experiments.OptimizationTimes --master "spark://cloud-11.dima.tu-berlin.de:7077" /share/hadoop/behrouz/jars/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar "result=/share/hadoop/behrouz/experiments/nyc-taxi/results/optimization-effect" "profile=taxi-cluster"

# copy the results to local machine
scp -r behrouz@cloud-11.dima.tu-berlin.de:/share/hadoop/behrouz/experiments/nyc-taxi/results/optimization-effect/ /Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/nyc-taxi/
