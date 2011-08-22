package com.thougtworks.yadic4scala

import java.lang.Class
import collection.mutable.Map

class SimpleContainer(missingHandler: (Class[_]) => Object) extends Container {

  def this() = this ((aClass: Class[_]) => {
    throw new ContainerException(aClass.getName + " not found in container")
  })

  val defaultScope = Scopes.prototype

  val activators = Map[Class[_], Activator]()

  def resolve(aClass: Class[_]): Object = {
    activators.get(aClass) match {
      case None => resolveMissing(aClass)
      case Some(activator: Activator) => activator.activate()
    }
  }

  def resolveMissing(aClass: Class[_]) = missingHandler(aClass)

  def resolveType[A <: Object]()(implicit manifest: Manifest[A]): A = {
    val aClass = manifest.erasure.asInstanceOf[Class[A]]
    resolve(aClass).asInstanceOf[A]
  }

  def add[C <: Object]()(implicit manifest: Manifest[C]) {
    add[C](defaultScope)
  }

  def add[I <: Object, C <: I]()(implicit manifestInterface: Manifest[I], manifestConcrete: Manifest[C]) {
    add[I, C](defaultScope)
  }

  def add[A <: Object](provider: () => A)(implicit manifest: Manifest[A]) {
    add[A](provider, defaultScope)
  }

  def decorate[I <: Object, C <: I]()(implicit manifestInterface: Manifest[I], manifestConcrete: Manifest[C]) {
    decorate[I, C](defaultScope)
  }

  def add[C <: Object](scope: Scope[C])(implicit manifestConcrete: Manifest[C]) {
    add[C](() => createInstance[C](), scope)
  }

  def add[I <: Object, C <: I](scope: Scope[I])(implicit manifestInterface: Manifest[I], manifestConcrete: Manifest[C]) {
    add[I](() => createInstance[C](), scope)
  }

  def add[A <: Object](provider: () => A, scope: Scope[A])(implicit manifest: Manifest[A]) {
    val aClass = manifest.erasure.asInstanceOf[Class[A]]
    activators.contains(aClass) match {
      case true => throw new ContainerException(aClass.getName + " already added to container")
      case false => activators += (aClass -> scope(provider))
    }
  }

  def decorate[I <: Object, C <: I](scope: Scope[I])(implicit manifestInterface: Manifest[I], manifestConcrete: Manifest[C]) {
    val interface = manifestInterface.erasure.asInstanceOf[Class[I]]
    val existing = activators(interface)
    activators += (interface -> scope(() => createInstance[C]((aClass: Class[_]) => {
      if (aClass.equals(interface)) existing.activate() else resolve(aClass)
    })))
  }

  def createInstance[T <: Object]()(implicit manifestInterface: Manifest[T]): T = createInstance[T](resolve(_))

  def createInstance[T <: Object](resolver: (Class[_]) => Object)(implicit manifestInterface: Manifest[T]): T = {
    val aClass = manifest.erasure.asInstanceOf[Class[T]]
    val constructors = aClass.getConstructors.toList.sortWith(_.getParameterTypes.length > _.getParameterTypes.length)
    constructors.foreach(constructor => {
      try {
        val instances = constructor.getParameterTypes.map(resolver(_))
        return constructor.newInstance(instances: _*).asInstanceOf[T]
      } catch {
        case e: ContainerException =>
      }
    })
    throw new ContainerException(aClass.getName + " does not have a satisfiable constructor")
  }
}
