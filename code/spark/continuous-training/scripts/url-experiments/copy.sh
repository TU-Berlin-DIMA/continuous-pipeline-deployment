
# Copy jar files
scp target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar behrouz@cloud-11.dima.tu-berlin.de:/home/behrouz/jar
cp /home/behrouz/jar/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar /share/hadoop/behrouz/jars/


# CLuster
/share/hadoop/behrouz/spark/stable/bin/spark-submit --class de.dfki.preprocessing.datasets.URLRepPreprocessing --master "spark://cloud-11.dima.tu-berlin.de:7077" /share/hadoop/behrouz/jars/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar "input-path=/share/hadoop/behrouz/data/url_svmlight" "output-path=hdfs://cloud-11:44000/user/behrouz/url/experiments/processed/"
