# Copy jar files
scp target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar behrouz@cloud-11.dima.tu-berlin.de:/home/behrouz/jar
cp /home/behrouz/jar/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar /share/hadoop/behrouz/jars/



#Cluster
/share/hadoop/behrouz/spark/stable/bin/spark-submit --class de.dfki.experiments.DeploymentModesQualityAndTime --master "spark://cloud-11.dima.tu-berlin.de:7077" /share/hadoop/behrouz/jars/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar "result=/share/hadoop/behrouz/experiments/nyc-taxi/results/deployment-modes" "profile=taxi-cluster" "slack=6" "sample-size=360"

# copy the results to local machine
scp -r behrouz@cloud-11.dima.tu-berlin.de:/share/hadoop/behrouz/experiments/nyc-taxi/results/deployment-modes/ /Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/nyc-taxi/

~/Documents/frameworks/spark/2.2.0/bin/spark-submit --class de.dfki.ml.pipelines.nyc_taxi.NYCTaxiPipeline --master "spark://berlin-138.b.dfki.de:7077" --driver-memory 2G --executor-memory 7G /Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar "input-path=/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/data/nyc-taxi/raw/yellow_tripdata_2015-01.csv,/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/data/nyc-taxi/raw/yellow_tripdata_2015-02.csv" "test-path=/Users/bede01/Documents/work/phd-papers/continuous-training/code/spark/continuous-training/data/nyc-taxi/raw/yellow_tripdata_2016-06.csv" "pipeline=/Users/bede01/Documents/work/phd-papers/continuous-training/experiment-results/nyc-taxi-local/pipelines/test/janfeb" 

