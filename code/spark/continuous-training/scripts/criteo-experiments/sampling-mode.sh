#


#Cluster
/share/hadoop/behrouz/spark/stable/bin/spark-submit --class de.dfki.experiments.SamplingModes --master "spark://cloud-11.dima.tu-berlin.de:7077" /share/hadoop/behrouz/jars/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar "result=/share/hadoop/behrouz/experiments/criteo/results/sampling-modes" "profile=criteo-cluster" "days=1,2"

# copy the results to local machine
scp -r behrouz@cloud-11.dima.tu-berlin.de:/share/hadoop/behrouz/experiments/criteo/results/sampling-modes/ /Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/criteo/
