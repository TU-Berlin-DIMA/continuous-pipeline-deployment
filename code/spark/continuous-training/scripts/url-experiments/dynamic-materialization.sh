#!/usr/bin/env bash

# Copy jar files
scp target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar behrouz@cloud-11.dima.tu-berlin.de:/home/behrouz/jar
cp /home/behrouz/jar/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar /share/hadoop/behrouz/jars/

#Local
~/Documents/frameworks/spark/2.2.0/bin/spark-submit --class de.dfki.experiments.DynamicMaterialization \
--master "spark://berlin-160.b.dfki.de:7077" --driver-memory 3G --executor-memory 6G \
/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar \
"profile=url" "result=/Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/url-reputation/dynamic-optimization" \
"days=1,20"

# cluster
/share/hadoop/behrouz/spark/stable/bin/spark-submit --class de.dfki.experiments.DynamicMaterialization --master "spark://cloud-11.dima.tu-berlin.de:7077" \
/share/hadoop/behrouz/jars/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar "result=/share/hadoop/behrouz/experiments/url/results/dynamic-optimization" \
"profile=url-cluster"


# copy the results to local machine
scp -r behrouz@cloud-11.dima.tu-berlin.de:/share/hadoop/behrouz/experiments/url/results/dynamic-optimization/ /Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/url-cluster/



# local one-by-one
# timebased , materialization 0
~/Documents/frameworks/spark/2.2.0/bin/spark-submit --class de.dfki.experiments.RunContinuousDeployment \
--master "spark://berlin-166.b.dfki.de:7077" --driver-memory 3G --executor-memory 6G \
/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar \
"profile=url" "result=/Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/url-reputation/dynamic-optimization/rate-0.0" \
 "sampling-strategy=time-based" "materialized-window=0"

# timebased , materialization 0.2
~/Documents/frameworks/spark/2.2.0/bin/spark-submit --class de.dfki.experiments.RunContinuousDeployment \
--master "spark://berlin-166.b.dfki.de:7077" --driver-memory 3G --executor-memory 6G \
/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar \
"profile=url" "result=/Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/url-reputation/dynamic-optimization/rate-0.2" \
"sampling-strategy=time-based" "materialized-window=2400"

# timebased , materialization 0.4
~/Documents/frameworks/spark/2.2.0/bin/spark-submit --class de.dfki.experiments.RunContinuousDeployment \
--master "spark://berlin-166.b.dfki.de:7077" --driver-memory 3G --executor-memory 6G \
/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar \
"profile=url" "result=/Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/url-reputation/dynamic-optimization/rate-0.4" \
"sampling-strategy=time-based" "materialized-window=4800"

# timebased , materialization 0.6
~/Documents/frameworks/spark/2.2.0/bin/spark-submit --class de.dfki.experiments.RunContinuousDeployment \
--master "spark://berlin-166.b.dfki.de:7077" --driver-memory 3G --executor-memory 6G \
/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar \
"profile=url" "result=/Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/url-reputation/dynamic-optimization/rate-0.6" \
"sampling-strategy=time-based" "materialized-window=7200"

# timebased , materialization 0.8
~/Documents/frameworks/spark/2.2.0/bin/spark-submit --class de.dfki.experiments.RunContinuousDeployment \
--master "spark://berlin-166.b.dfki.de:7077" --driver-memory 3G --executor-memory 6G \
/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar \
"profile=url" "result=/Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/url-reputation/dynamic-optimization/rate-0.8" \
"sampling-strategy=time-based" "materialized-window=9600"

# timebased , materialization 1.0
~/Documents/frameworks/spark/2.2.0/bin/spark-submit --class de.dfki.experiments.RunContinuousDeployment \
--master "spark://berlin-166.b.dfki.de:7077" --driver-memory 2G --executor-memory 8G \
/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar \
"profile=url" "result=/Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/url-reputation/dynamic-optimization/rate-1.0" \
"sampling-strategy=time-based" "materialized-window=12000"


# uniform , materialization 0
~/Documents/frameworks/spark/2.2.0/bin/spark-submit --class de.dfki.experiments.RunContinuousDeployment \
--master "spark://berlin-166.b.dfki.de:7077" --driver-memory 3G --executor-memory 6G \
/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar \
"profile=url" "result=/Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/url-reputation/dynamic-optimization/rate-0.0" \
 "sampling-strategy=uniform" "materialized-window=0"

# uniform , materialization 0.2
~/Documents/frameworks/spark/2.2.0/bin/spark-submit --class de.dfki.experiments.RunContinuousDeployment \
--master "spark://berlin-166.b.dfki.de:7077" --driver-memory 3G --executor-memory 6G \
/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar \
"profile=url" "result=/Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/url-reputation/dynamic-optimization/rate-0.2-non-blocking" \
"sampling-strategy=uniform" "materialized-window=2400"

# uniform , materialization 0.4
~/Documents/frameworks/spark/2.2.0/bin/spark-submit --class de.dfki.experiments.RunContinuousDeployment \
--master "spark://berlin-166.b.dfki.de:7077" --driver-memory 3G --executor-memory 6G \
/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar \
"profile=url" "result=/Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/url-reputation/dynamic-optimization/rate-0.4" \
"sampling-strategy=uniform" "materialized-window=4800"

# uniform , materialization 0.6
~/Documents/frameworks/spark/2.2.0/bin/spark-submit --class de.dfki.experiments.RunContinuousDeployment \
--master "spark://berlin-166.b.dfki.de:7077" --driver-memory 3G --executor-memory 6G \
/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar \
"profile=url" "result=/Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/url-reputation/dynamic-optimization/rate-0.6-non-blocking" \
"sampling-strategy=uniform" "materialized-window=7200"

# uniform , materialization 0.8
~/Documents/frameworks/spark/2.2.0/bin/spark-submit --class de.dfki.experiments.RunContinuousDeployment \
--master "spark://berlin-166.b.dfki.de:7077" --driver-memory 3G --executor-memory 6G \
/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar \
"profile=url" "result=/Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/url-reputation/dynamic-optimization/rate-0.8" \
"sampling-strategy=uniform" "materialized-window=9600"

# uniform , materialization 1.0
~/Documents/frameworks/spark/2.2.0/bin/spark-submit --class de.dfki.experiments.RunContinuousDeployment \
--master "spark://berlin-166.b.dfki.de:7077" --driver-memory 2G --executor-memory 8G \
/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar \
"profile=url" "result=/Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/url-reputation/dynamic-optimization/rate-1.0" \
"sampling-strategy=uniform" "materialized-window=12000"


# window , materialization 0
~/Documents/frameworks/spark/2.2.0/bin/spark-submit --class de.dfki.experiments.RunContinuousDeployment \
--master "spark://berlin-166.b.dfki.de:7077" --driver-memory 3G --executor-memory 6G \
/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar \
"profile=url" "result=/Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/url-reputation/dynamic-optimization/rate-0.0" \
 "sampling-strategy=window-based" "materialized-window=0"

# window , materialization 0.2
~/Documents/frameworks/spark/2.2.0/bin/spark-submit --class de.dfki.experiments.RunContinuousDeployment \
--master "spark://berlin-166.b.dfki.de:7077" --driver-memory 3G --executor-memory 6G \
/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar \
"profile=url" "result=/Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/url-reputation/dynamic-optimization/rate-0.2" \
"sampling-strategy=window-based" "materialized-window=2400"

# window , materialization 0.4
~/Documents/frameworks/spark/2.2.0/bin/spark-submit --class de.dfki.experiments.RunContinuousDeployment \
--master "spark://berlin-166.b.dfki.de:7077" --driver-memory 3G --executor-memory 6G \
/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar \
"profile=url" "result=/Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/url-reputation/dynamic-optimization/rate-0.4" \
"sampling-strategy=window-based" "materialized-window=4800"

# window , materialization 0.6
~/Documents/frameworks/spark/2.2.0/bin/spark-submit --class de.dfki.experiments.RunContinuousDeployment \
--master "spark://berlin-166.b.dfki.de:7077" --driver-memory 3G --executor-memory 6G \
/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar \
"profile=url" "result=/Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/url-reputation/dynamic-optimization/rate-0.6" \
"sampling-strategy=window-based" "materialized-window=7200"

# window , materialization 0.8
~/Documents/frameworks/spark/2.2.0/bin/spark-submit --class de.dfki.experiments.RunContinuousDeployment \
--master "spark://berlin-166.b.dfki.de:7077" --driver-memory 3G --executor-memory 6G \
/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar \
"profile=url" "result=/Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/url-reputation/dynamic-optimization/rate-0.8" \
"sampling-strategy=window-based" "materialized-window=9600"

# window , materialization 1.0
~/Documents/frameworks/spark/2.2.0/bin/spark-submit --class de.dfki.experiments.RunContinuousDeployment \
--master "spark://berlin-166.b.dfki.de:7077" --driver-memory 2G --executor-memory 8G \
/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar \
"profile=url" "result=/Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/url-reputation/dynamic-optimization/rate-1.0" \
"sampling-strategy=window-based" "materialized-window=12000"



# cluster node 36 one-by-one materialization 0.0
/share/hadoop/behrouz/spark/stable/bin/spark-submit --class de.dfki.experiments.RunContinuousDeployment --master "spark://cloud-36.dima.tu-berlin.de:7077" \
/share/hadoop/behrouz/jars/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar \
"profile=url-cluster" "result=/share/hadoop/behrouz/experiments/url/results/dynamic-optimization/rate-0.0" \
"sampling-strategy=time-based" "materialized-window=0" "input=file:///data/users/behrouz/url/initial-training/day=0" "stream=file:///data/users/behrouz/url/stream"


# cluster node 36 one-by-one materialization 0.2
/share/hadoop/behrouz/spark/stable/bin/spark-submit --class de.dfki.experiments.RunContinuousDeployment --master "spark://cloud-36.dima.tu-berlin.de:7077" \
/share/hadoop/behrouz/jars/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar \
"profile=url-cluster" "result=/share/hadoop/behrouz/experiments/url/results/dynamic-optimization/rate-0.2" \
"sampling-strategy=time-based" "materialized-window=2400" "input=file:///data/users/behrouz/url/initial-training/day=0" "stream=file:///data/users/behrouz/url/stream"


# cluster node 36 one-by-one materialization 0.6
/share/hadoop/behrouz/spark/stable/bin/spark-submit --class de.dfki.experiments.RunContinuousDeployment --master "spark://cloud-36.dima.tu-berlin.de:7077" \
/share/hadoop/behrouz/jars/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar \
"profile=url-cluster" "result=/share/hadoop/behrouz/experiments/url/results/dynamic-optimization/rate-0.6" \
"sampling-strategy=time-based" "materialized-window=7200" "input=file:///data/users/behrouz/url/initial-training/day=0" "stream=file:///data/users/behrouz/url/stream"



# cluster node 36 one-by-one materialization 1.0
/share/hadoop/behrouz/spark/stable/bin/spark-submit --class de.dfki.experiments.RunContinuousDeployment --master "spark://cloud-36.dima.tu-berlin.de:7077" \
/share/hadoop/behrouz/jars/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar \
"profile=url-cluster" "result=/share/hadoop/behrouz/experiments/url/results/dynamic-optimization/rate-1.0" \
"sampling-strategy=time-based" "materialized-window=12000" "input=file:///data/users/behrouz/url/initial-training/day=0" "stream=file:///data/users/behrouz/url/stream"