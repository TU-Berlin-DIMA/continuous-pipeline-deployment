# Copy jar files
scp target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar behrouz@cloud-11.dima.tu-berlin.de:/home/behrouz/jar
cp /home/behrouz/jar/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar /share/hadoop/behrouz/jars/


# preprocess url files
local~/Documents/frameworks/spark/2.2.0/bin/spark-submit --class de.dfki.preprocessing.datasets.URLRepPreprocessing --master "spark://berlin-138.b.dfki.de:7077" --driver-memory 2g --executor-memory 5G /Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar "input-path=/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/data/url-reputation/raw" "output-path=/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/data/url-reputation/processed"

