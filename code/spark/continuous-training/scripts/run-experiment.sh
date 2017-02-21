Local: 
~/Documents/frameworks/spark-2.1.0-bin-hadoop2.7/bin/spark-submit --class de.dfki.classification.ContinuousClassifier --master "spark://berlin-189.b.dfki.de:7077" target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar "batch-duration=1" "slack=10" "result-path=results/cover-types/continuous" "initial-training-path=data/cover-types/initial-training" "streaming-path=data/cover-types/stream-training" "temp-path=data/cover-types/temp-data"

~/Documents/frameworks/spark-2.1.0-bin-hadoop2.7/bin/spark-submit --class de.dfki.preprocessing.CriteoFeatureEngineering --master "spark://berlin-189.b.dfki.de:7077" target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar "input-path=data/criteo-sample/raw/" "output-path=data/criteo-sample/"




Cluster:
scp target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar behrouz@cloud-11.dima.tu-berlin.de:/home/behrouz/jar

cp /home/behrouz/jar/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar /share/hadoop/behrouz/jars/

/share/hadoop/stable/hadoop-2.7.1/bin/hdfs dfs -rmr hdfs://cloud-11:44000/user/behrouz/criteo/stream-training
/share/hadoop/stable/hadoop-2.7.1/bin/hdfs dfs -rmr hdfs://cloud-11:44000/user/behrouz/criteo/initial-training

/share/hadoop/behrouz/spark/stable/bin/spark-submit --class de.dfki.preprocessing.CriteoFeatureEngineering --master "spark://cloud-11.dima.tu-berlin.de:7077" /share/hadoop/behrouz/jars/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar "input-path=hdfs://cloud-11:44000/user/behrouz/criteo/raw/" "output-path=hdfs://cloud-11:44000/user/behrouz/criteo/" "file-count=2000"

/share/hadoop/behrouz/spark/stable/bin/spark-submit --class de.dfki.classification.ContinuousClassifier --master "spark://cloud-11.dima.tu-berlin.de:7077" /share/hadoop/behrouz/jars/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar "batch-duration=2" "slack=50" "result-path=/share/hadoop/behrouz/experiments/continuous/batch-3-slack-60/" "initial-training-path=hdfs://cloud-11:44000/user/behrouz/criteo/initial-training" "streaming-path=hdfs://cloud-11:44000/user/behrouz/criteo/stream-training" "temp-path=hdfs://cloud-11:44000/user/behrouz/criteo/temp-data"




