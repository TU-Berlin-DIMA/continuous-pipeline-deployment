package de.dfki.ml

import breeze.linalg.{DenseVector => BDV, SparseVector => BSV, Vector => BV}
import com.github.fommil.netlib.{F2jBLAS, BLAS => NetlibBLAS}
import org.apache.spark.mllib.linalg.{DenseVector, SparseVector, Vector}


/**
  * @author bede01.
  */
object LinearAlgebra {

  @transient private var _f2jBLAS: NetlibBLAS = _
 // @transient private var _nativeBLAS: NetlibBLAS = _

  // For level-1 routines, we use Java implementation.
  private def f2jBLAS: NetlibBLAS = {
    if (_f2jBLAS == null) {
      _f2jBLAS = new F2jBLAS
    }
    _f2jBLAS
  }

  def fromBreeze(breezeVector: BV[Double]): Vector = {
    breezeVector match {
      case v: BDV[Double] =>
        if (v.offset == 0 && v.stride == 1 && v.length == v.data.length) {
          new DenseVector(v.data)
        } else {
          new DenseVector(v.toArray) // Can't use underlying array directly, so make a new one
        }
      case v: BSV[Double] =>
        if (v.index.length == v.used) {
          new SparseVector(v.length, v.index, v.data)
        } else {
          new SparseVector(v.length, v.index.slice(0, v.used), v.data.slice(0, v.used))
        }
      case v: BV[_] =>
        sys.error("Unsupported Breeze vector type: " + v.getClass.getName)
    }
  }

  def asBreeze(v: Vector): BV[Double] = {
    v match {
      case v2: DenseVector => new BDV[Double](v2.values)
      case v2: SparseVector => new BSV[Double](v2.indices, v2.values, v2.size)
    }
    new BDV[Double](v.toArray)
  }

  def scal(a: Double, x: Vector): Unit = {
    x match {
      case sx: SparseVector =>
        f2jBLAS.dscal(sx.values.length, a, sx.values, 1)
      case dx: DenseVector =>
        f2jBLAS.dscal(dx.values.length, a, dx.values, 1)
      case _ =>
        throw new IllegalArgumentException(s"scal doesn't support vector type ${x.getClass}.")
    }
  }

}