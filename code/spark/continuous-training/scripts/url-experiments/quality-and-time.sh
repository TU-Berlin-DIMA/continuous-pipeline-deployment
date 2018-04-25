# Copy jar files
scp target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar behrouz@cloud-11.dima.tu-berlin.de:/home/behrouz/jar
cp /home/behrouz/jar/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar /share/hadoop/behrouz/jars/


home: spark://MacBook-Pro-5.fritz.box:7077
# local
~/Documents/frameworks/spark/2.2.0/bin/spark-submit --class de.dfki.experiments.DeploymentModesQualityAndTime --master "spark://MacBook-Pro-5.fritz.box:7077" --driver-memory 1g --executor-memory 5G /Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar "profile=url" "result=/Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/url-reputation/deployment-modes"

rm -r /Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/data/url-reputation/processed/materialized