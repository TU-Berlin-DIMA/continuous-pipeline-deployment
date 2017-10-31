#local
~/Documents/frameworks/spark/2.2.0/bin/spark-submit --class de.dfki.experiments.TrainingTimes --master "spark://dhcp-214-87.vpn.tu-berlin.de:7077" --driver-memory 3g --executor-memory 5G /Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar "input=/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/data/criteo-full/experiments/initial-training/day_0" "stream=/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/data/criteo-full/experiments/stream" "result=/Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/criteo-full/training-time/local" "slack=5" "features=3000" "day_duration=100" "days=1,2"



# cluster
/share/hadoop/behrouz/spark/stable/bin/spark-submit --class de.dfki.experiments.TrainingTimes --master "spark://cloud-11.dima.tu-berlin.de:7077" /share/hadoop/behrouz/jars/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar "input=hdfs://cloud-11:44000/user/behrouz/criteo/experiments/initial-training/day_0" "stream=hdfs://cloud-11:44000/user/behrouz/criteo/experiments/stream" "result=/share/hadoop/behrouz/experiments/criteo-full/training-time" "delimiter=\t" "features=3000" "slack=10" "iterations=500" "days=1,2" "day_duration=1440"

scp -r behrouz@cloud-11.dima.tu-berlin.de:/share/hadoop/behrouz/experiments/criteo-full/training-time/* /Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/criteo-full/training-time/cluster/