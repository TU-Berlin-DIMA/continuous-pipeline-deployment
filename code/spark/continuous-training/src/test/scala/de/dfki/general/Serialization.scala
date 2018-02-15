package de.dfki.general

import java.io._

import breeze.linalg.DenseVector
import de.dfki.ml.LinearAlgebra
import de.dfki.ml.optimization.updater.SquaredL2UpdaterWithMomentum
import de.dfki.ml.streaming.models.{HybridLR, HybridModel}
import org.scalatest.{BeforeAndAfterEach, FunSuite}

/**
  * @author behrouz
  */
class Serialization extends FunSuite with BeforeAndAfterEach {
  test("Updater Serialization") {
    val updater = new SquaredL2UpdaterWithMomentum(0.9).withUpdateVector(DenseVector.zeros(10))

    val file = new File("target/test/serializable/updater")
    file.getParentFile.mkdirs()
    file.createNewFile()
    val oos = new ObjectOutputStream(new FileOutputStream(file, false))
    oos.writeObject(updater)
    oos.close()

    val ois = new ObjectInputStream(new FileInputStream(file))
    val readUpdater = ois.readObject.asInstanceOf[SquaredL2UpdaterWithMomentum]
    ois.close()
    readUpdater.compute(LinearAlgebra.fromBreeze(DenseVector.zeros[Double](10)), LinearAlgebra.fromBreeze(DenseVector.zeros[Double](10)), 1.0, 1)
    println(readUpdater.toString)
  }

  ignore("Model Serialization") {
    val model = new HybridLR(1.0, 100, 0.0, 1.0, new SquaredL2UpdaterWithMomentum(0.9))

    HybridModel.saveToDisk("target/test/serializable/lr-model", model)
    val deserialized = HybridModel.loadFromDisk("target/test/serializable/lr-model")

    println(deserialized.toString())
  }

  ignore("Model Serialization 2"){
    val deserialized = HybridModel.loadFromDisk("data/criteo-full/model/500/model")
    println(deserialized.toString())
  }

}
