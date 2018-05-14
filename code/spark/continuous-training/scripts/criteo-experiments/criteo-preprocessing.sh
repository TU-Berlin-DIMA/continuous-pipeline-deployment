
# Copy jar files
scp target/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar behrouz@cloud-11.dima.tu-berlin.de:/home/behrouz/jar
cp /home/behrouz/jar/continuous-training-1.0-SNAPSHOT-jar-with-dependencies.jar /share/hadoop/behrouz/jars/

/share/hadoop/behrouz/spark/stable/bin/spark-shell --master "spark://cloud-11.dima.tu-berlin.de:7077"

# Process files
val data = sc.textFile("hdfs://cloud-11:44000/criteo/day_1")
data.saveAsTextFile("hdfs://cloud-11:44000/user/behrouz/criteo/experiments/stream/day=1/")

val data = sc.textFile("hdfs://cloud-11:44000/criteo/day_2")
data.saveAsTextFile("hdfs://cloud-11:44000/user/behrouz/criteo/experiments/stream/day=2/")


val data = sc.textFile("hdfs://cloud-11:44000/criteo/day_3")
data.repartition(1440).saveAsTextFile("hdfs://cloud-11:44000/user/behrouz/criteo/experiments/stream/day=3/")

val data = sc.textFile("hdfs://cloud-11:44000/criteo/day_4")
data.repartition(1440).saveAsTextFile("hdfs://cloud-11:44000/user/behrouz/criteo/experiments/stream/day=4/")

val data = sc.textFile("hdfs://cloud-11:44000/criteo/day_5")
data.repartition(1440).saveAsTextFile("hdfs://cloud-11:44000/user/behrouz/criteo/experiments/stream/day=5/")

val data = sc.textFile("hdfs://cloud-11:44000/criteo/day_6")
data.repartition(1440).saveAsTextFile("hdfs://cloud-11:44000/user/behrouz/criteo/experiments/stream/day=6/")

val data = sc.textFile("hdfs://cloud-11:44000/criteo/day_7")
data.repartition(1440).saveAsTextFile("hdfs://cloud-11:44000/user/behrouz/criteo/experiments/stream/day=7/")

val data = sc.textFile("hdfs://cloud-11:44000/criteo/day_8")
data.repartition(1440).saveAsTextFile("hdfs://cloud-11:44000/user/behrouz/criteo/experiments/stream/day=8/")

val data = sc.textFile("hdfs://cloud-11:44000/criteo/day_9")
data.repartition(1440).saveAsTextFile("hdfs://cloud-11:44000/user/behrouz/criteo/experiments/stream/day=9/")

val data = sc.textFile("hdfs://cloud-11:44000/criteo/day_10")
data.repartition(1440).saveAsTextFile("hdfs://cloud-11:44000/user/behrouz/criteo/experiments/stream/day=10/")

