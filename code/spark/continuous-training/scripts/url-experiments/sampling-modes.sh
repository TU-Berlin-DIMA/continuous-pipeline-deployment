# Copy jar files
scp target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar behrouz@cloud-11.dima.tu-berlin.de:/home/behrouz/jar
cp /home/behrouz/jar/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar /share/hadoop/behrouz/jars/


# local
~/Documents/frameworks/spark/2.2.0/bin/spark-submit --class de.dfki.experiments.SamplingModes --master "spark://berlin-138.b.dfki.de:7077" --driver-memory 1g --executor-memory 5G /Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar "profile=url" "days=1,30" "result=/Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/url-reputation/sampling-modes" "pipeline=/Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/url-reputation/pipelines/best/adam-0.001"
