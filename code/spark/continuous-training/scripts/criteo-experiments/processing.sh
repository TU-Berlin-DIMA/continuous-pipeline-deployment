
# Copy jar files
scp target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar behrouz@cloud-11.dima.tu-berlin.de:/home/behrouz/jar
cp /home/behrouz/jar/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar /share/hadoop/behrouz/jars/

# Process files
val data = sc.textFile("hdfs://cloud-11:44000/criteo/day_0")
data.repartition(1000).saveAsTextFile("hdfs://cloud-11:44000/user/behrouz/criteo/experiments/initial-training/day_0/")

val data = sc.textFile("hdfs://cloud-11:44000/criteo/day_1")
data.repartition(1000).saveAsTextFile("hdfs://cloud-11:44000/user/behrouz/criteo/experiments/stream/day_1/")

val data = sc.textFile("hdfs://cloud-11:44000/criteo/day_2")
data.repartition(1000).saveAsTextFile("hdfs://cloud-11:44000/user/behrouz/criteo/experiments/stream/day_2/")

val data = sc.textFile("hdfs://cloud-11:44000/criteo/day_3")
data.repartition(1000).saveAsTextFile("hdfs://cloud-11:44000/user/behrouz/criteo/experiments/stream/day_3/")

val data = sc.textFile("hdfs://cloud-11:44000/criteo/day_4")
data.repartition(1000).saveAsTextFile("hdfs://cloud-11:44000/user/behrouz/criteo/experiments/stream/day_4/")

val data = sc.textFile("hdfs://cloud-11:44000/criteo/day_5")
data.repartition(1000).saveAsTextFile("hdfs://cloud-11:44000/user/behrouz/criteo/experiments/stream/day_5/")

val data = sc.textFile("hdfs://cloud-11:44000/criteo/day_6")
data.repartition(1000).saveAsTextFile("hdfs://cloud-11:44000/user/behrouz/criteo/experiments/evaluation/day_6/")
