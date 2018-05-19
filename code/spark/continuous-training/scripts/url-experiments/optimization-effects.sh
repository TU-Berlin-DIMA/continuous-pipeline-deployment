# Copy jar files
scp target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar behrouz@cloud-11.dima.tu-berlin.de:/home/behrouz/jar
cp /home/behrouz/jar/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar /share/hadoop/behrouz/jars/

#Cluster
~/Documents/frameworks/spark/2.2.0/bin/spark-submit --class de.dfki.experiments.OptimizationTimes --master "spark://wlan-141-23-215-63.tubit.tu-berlin.de:7077" --driver-memory 3G --executor-memory 6G /Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar "profile=url" "result=/Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/url-reputation/optimization-effect"

# copy the results to local machine
scp -r behrouz@cloud-11.dima.tu-berlin.de:/share/hadoop/behrouz/experiments/nyc-taxi/results/optimization-effect/ /Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/nyc-taxi/
