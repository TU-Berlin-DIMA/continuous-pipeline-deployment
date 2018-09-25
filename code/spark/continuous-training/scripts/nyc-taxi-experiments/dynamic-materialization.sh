#!/usr/bin/env bash
# Copy jar files
scp target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar behrouz@cloud-11.dima.tu-berlin.de:/home/behrouz/jar
cp /home/behrouz/jar/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar /share/hadoop/behrouz/jars/


# cluster time-based materialization 0.0
/share/hadoop/behrouz/spark/stable/bin/spark-submit --class de.dfki.experiments.RunContinuousDeployment --master "spark://cloud-11.dima.tu-berlin.de:7077" \
/share/hadoop/behrouz/jars/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar \
"profile=taxi-cluster" "result=/share/hadoop/behrouz/experiments/nyc-taxi/results/dynamic-optimization/rate-0.0" \
"sampling-strategy=time-based" "materialized-window=0"

# cluster time-based materialization 0.2
/share/hadoop/behrouz/spark/stable/bin/spark-submit --class de.dfki.experiments.RunContinuousDeployment --master "spark://cloud-11.dima.tu-berlin.de:7077" \
/share/hadoop/behrouz/jars/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar \
"profile=taxi-cluster" "result=/share/hadoop/behrouz/experiments/nyc-taxi/results/dynamic-optimization/rate-0.2" \
"sampling-strategy=time-based" "materialized-window=2400"

# cluster time-based materialization 0.6
/share/hadoop/behrouz/spark/stable/bin/spark-submit --class de.dfki.experiments.RunContinuousDeployment --master "spark://cloud-11.dima.tu-berlin.de:7077" \
/share/hadoop/behrouz/jars/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar \
"profile=taxi-cluster" "result=/share/hadoop/behrouz/experiments/nyc-taxi/results/dynamic-optimization/rate-0.6" \
"sampling-strategy=time-based" "materialized-window=7200"

# cluster time-based materialization 1.0
/share/hadoop/behrouz/spark/stable/bin/spark-submit --class de.dfki.experiments.RunContinuousDeployment --master "spark://cloud-11.dima.tu-berlin.de:7077" \
/share/hadoop/behrouz/jars/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar \
"profile=taxi-cluster" "result=/share/hadoop/behrouz/experiments/nyc-taxi/results/dynamic-optimization/rate-1.0" \
"sampling-strategy=time-based" "materialized-window=12000"


# cluster uniform materialization 0.0
/share/hadoop/behrouz/spark/stable/bin/spark-submit --class de.dfki.experiments.RunContinuousDeployment --master "spark://cloud-11.dima.tu-berlin.de:7077" \
/share/hadoop/behrouz/jars/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar \
"profile=taxi-cluster" "result=/share/hadoop/behrouz/experiments/nyc-taxi/results/dynamic-optimization/rate-0.0" \
"sampling-strategy=uniform" "materialized-window=0"

# cluster uniform materialization 0.2
/share/hadoop/behrouz/spark/stable/bin/spark-submit --class de.dfki.experiments.RunContinuousDeployment --master "spark://cloud-11.dima.tu-berlin.de:7077" \
/share/hadoop/behrouz/jars/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar \
"profile=taxi-cluster" "result=/share/hadoop/behrouz/experiments/nyc-taxi/results/dynamic-optimization/rate-0.2" \
"sampling-strategy=uniform" "materialized-window=2400"

# cluster uniform materialization 0.6
/share/hadoop/behrouz/spark/stable/bin/spark-submit --class de.dfki.experiments.RunContinuousDeployment --master "spark://cloud-11.dima.tu-berlin.de:7077" \
/share/hadoop/behrouz/jars/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar \
"profile=taxi-cluster" "result=/share/hadoop/behrouz/experiments/nyc-taxi/results/dynamic-optimization/rate-0.6" \
"sampling-strategy=uniform" "materialized-window=7200"

# cluster uniform materialization 1.0
/share/hadoop/behrouz/spark/stable/bin/spark-submit --class de.dfki.experiments.RunContinuousDeployment --master "spark://cloud-11.dima.tu-berlin.de:7077" \
/share/hadoop/behrouz/jars/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar \
"profile=taxi-cluster" "result=/share/hadoop/behrouz/experiments/nyc-taxi/results/dynamic-optimization/rate-1.0" \
"sampling-strategy=uniform" "materialized-window=12000"


# cluster window-based materialization 0.0
/share/hadoop/behrouz/spark/stable/bin/spark-submit --class de.dfki.experiments.RunContinuousDeployment --master "spark://cloud-11.dima.tu-berlin.de:7077" \
/share/hadoop/behrouz/jars/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar \
"profile=taxi-cluster" "result=/share/hadoop/behrouz/experiments/nyc-taxi/results/dynamic-optimization/rate-0.0" \
"sampling-strategy=window-based" "materialized-window=0"

# cluster window-based materialization 0.2
/share/hadoop/behrouz/spark/stable/bin/spark-submit --class de.dfki.experiments.RunContinuousDeployment --master "spark://cloud-11.dima.tu-berlin.de:7077" \
/share/hadoop/behrouz/jars/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar \
"profile=taxi-cluster" "result=/share/hadoop/behrouz/experiments/nyc-taxi/results/dynamic-optimization/rate-0.2" \
"sampling-strategy=window-based" "materialized-window=2400"

# cluster window-based materialization 0.6
/share/hadoop/behrouz/spark/stable/bin/spark-submit --class de.dfki.experiments.RunContinuousDeployment --master "spark://cloud-11.dima.tu-berlin.de:7077" \
/share/hadoop/behrouz/jars/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar \
"profile=taxi-cluster" "result=/share/hadoop/behrouz/experiments/nyc-taxi/results/dynamic-optimization/rate-0.6" \
"sampling-strategy=window-based" "materialized-window=7200"

# cluster window-based materialization 1.0
/share/hadoop/behrouz/spark/stable/bin/spark-submit --class de.dfki.experiments.RunContinuousDeployment --master "spark://cloud-11.dima.tu-berlin.de:7077" \
/share/hadoop/behrouz/jars/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar \
"profile=taxi-cluster" "result=/share/hadoop/behrouz/experiments/nyc-taxi/results/dynamic-optimization/rate-1.0" \
"sampling-strategy=window-based" "materialized-window=12000"


scp -r behrouz@cloud-11.dima.tu-berlin.de:/share/hadoop/behrouz/experiments/nyc-taxi/results/dynamic-optimization/ /Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/nyc-taxi/
