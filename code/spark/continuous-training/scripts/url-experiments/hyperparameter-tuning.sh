# Copy jar files
scp target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar behrouz@cloud-11.dima.tu-berlin.de:/home/behrouz/jar
cp /home/behrouz/jar/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar /share/hadoop/behrouz/jars/



~/Documents/frameworks/spark/2.2.0/bin/spark-submit --class de.dfki.experiments.ParameterSelection --master "spark://berlin-138.b.dfki.de:7077" --driver-memory 1g --executor-memory 5G /Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar "profile=url" "iterations=10000" "mini-batch=0.1" "result=/Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/url-reputation/param-selection" "pipeline=/Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/url-reputation/pipelines/hyperparameter-tuning" "days=1,30"