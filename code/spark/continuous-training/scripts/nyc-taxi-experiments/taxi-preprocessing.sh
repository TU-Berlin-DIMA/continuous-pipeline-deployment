# Copy jar files
scp target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar behrouz@cloud-11.dima.tu-berlin.de:/home/behrouz/jar
cp /home/behrouz/jar/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar /share/hadoop/behrouz/jars/


# preprocess nyc taxi files

# local
~/Documents/frameworks/spark/2.2.0/bin/spark-submit --class de.dfki.preprocessing.datasets.NYCTaxiPreprocessing --master "spark://dhcp-215-190.vpn.tu-berlin.de:7077" --driver-memory 2g --executor-memory 5G /Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar "input-path=/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/data/nyc-taxi/raw" "output-path=/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/data/nyc-taxi/processed"


# cluster 
/share/hadoop/stable/hadoop-2.7.1/bin/hdfs dfs -ls /user/behrouz/nyc-taxi/experiments/processed/

/share/hadoop/behrouz/spark/stable/bin/spark-submit --class de.dfki.preprocessing.datasets.NYCTaxiPreprocessing --master "spark://cloud-11.dima.tu-berlin.de:7077" /share/hadoop/behrouz/jars/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar "input-path=hdfs://cloud-11:44000/datasets/nyc-taxi/yellow/rest" "output-path=hdfs://cloud-11:44000/user/behrouz/nyc-taxi/experiments/processed/stream"

/share/hadoop/stable/hadoop-2.7.1/bin/hdfs dfs -mkdir /user/behrouz/nyc-taxi/experiments/processed/initial-training/
/share/hadoop/stable/hadoop-2.7.1/bin/hdfs dfs -cp "hdfs://cloud-11:44000/datasets/nyc-taxi/yellow/jan/yellow_tripdata_2015-01.csv" hdfs://cloud-11:44000/user/behrouz/nyc-taxi/experiments/processed/initial-training/

/share/hadoop/stable/hadoop-2.7.1/bin/hdfs dfs -mkdir /user/behrouz/nyc-taxi/experiments/processed/batch-evaluation/
/share/hadoop/stable/hadoop-2.7.1/bin/hdfs dfs -cp "hdfs://cloud-11:44000/datasets/nyc-taxi/yellow/rest/yellow_tripdata_2015-02.csv" hdfs://cloud-11:44000/user/behrouz/nyc-taxi/experiments/processed/batch-evaluation/

/share/hadoop/stable/hadoop-2.7.1/bin/hdfs dfs -ls /user/behrouz/nyc-taxi/experiments/processed/initial-training/

/share/hadoop/stable/hadoop-2.7.1/bin/hdfs dfs -ls /user/behrouz/nyc-taxi/experiments/processed/batch-evaluation/

/share/hadoop/stable/hadoop-2.7.1/bin/hdfs dfs -ls /user/behrouz/nyc-taxi/experiments/processed/stream/

/share/hadoop/behrouz/spark/stable/bin/spark-shell --master "spark://cloud-11.dima.tu-berlin.de:7077"


/share/hadoop/stable/hadoop-2.7.1/bin/hdfs dfs -cp "hdfs://cloud-11:44000/datasets/nyc-taxi/yellow/jan/yellow_tripdata_2015-01.csv" "hdfs://cloud-11:44000/datasets/nyc-taxi/yellow/rest/yellow_tripdata_2015-01.csv"
