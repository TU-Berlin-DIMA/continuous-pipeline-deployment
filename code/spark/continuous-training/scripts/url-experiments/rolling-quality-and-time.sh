# Copy jar files
scp target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar behrouz@cloud-11.dima.tu-berlin.de:/home/behrouz/jar
cp /home/behrouz/jar/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar /share/hadoop/behrouz/jars/


# local
~/Documents/frameworks/spark/2.2.0/bin/spark-submit --class de.dfki.experiments.RollingQualityAndTime --master "spark://berlin-164.b.dfki.de:7077" --driver-memory 2G --executor-memory 6G /Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar "profile=url" "training-frequency=100" "slack=5" "online=false" "rolling-window=700" "result=/Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/url-reputation/deployment-modes"


# cluster
/share/hadoop/behrouz/spark/stable/bin/spark-submit --class de.dfki.experiments.RollingQualityAndTime --master "spark://cloud-11.dima.tu-berlin.de:7077" /share/hadoop/behrouz/jars/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar "result=/share/hadoop/behrouz/experiments/url/results/deployment-modes" "profile=url-cluster" 

# copy the results to local machine
scp -r behrouz@cloud-11.dima.tu-berlin.de:/share/hadoop/behrouz/experiments/url/results/deployment-modes/ /Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/url-cluster/
