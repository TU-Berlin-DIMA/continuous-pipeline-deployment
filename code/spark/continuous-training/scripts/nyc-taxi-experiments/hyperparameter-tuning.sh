# Copy jar files
scp target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar behrouz@cloud-11.dima.tu-berlin.de:/home/behrouz/jar
cp /home/behrouz/jar/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar /share/hadoop/behrouz/jars/

#cluster
/share/hadoop/behrouz/spark/stable/bin/spark-submit --class de.dfki.experiments.ParameterSelection --master "spark://cloud-11.dima.tu-berlin.de:7077" /share/hadoop/behrouz/jars/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar "pipeline=/share/hadoop/behrouz/experiments/nyc-taxi/pipelines/hyperparameter-tuning" "iterations=10000" "mini-batch=0.1" "result=/share/hadoop/behrouz/experiments/nyc-taxi/results/param-selection" "profile=taxi-cluster" "days=32,120"

scp -r behrouz@cloud-11.dima.tu-berlin.de:/share/hadoop/behrouz/experiments/nyc-taxi/results/param-selection/ /Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/nyc-taxi/
