package com.thougtworks.yadic4scala

import java.lang.Class
import java.util.HashMap

class SimpleContainer(missingHandler: (Class[_]) => Object) extends Container {

  def this() = this ((aClass: Class[_]) => {
    throw new ContainerException(aClass.getName + " not found in container")
  })

  val defaultScope = Scopes.prototype

  val activators = new HashMap[Class[_], Activator]

  def resolve(aClass: Class[_]): Object = {
    activators.get(aClass) match {
      case null => resolveMissing(aClass)
      case activator: Activator => activator.activate()
    }
  }

  def resolveMissing(aClass: Class[_]) = {
    missingHandler(aClass)
  }

  def resolveType[A <: Object](aClass: Class[A]): A = resolve(aClass).asInstanceOf[A]

  def add[C <: Object]()(implicit manifest: Manifest[C]) {
    add[C](defaultScope)
  }

  def add[I <: Object, C <: I]()(implicit manifestInterface: Manifest[I], manifestConcrete: Manifest[C]) {
    add[I, C](defaultScope)
  }

  def add[A <: Object](provider: () => A)(implicit manifest: Manifest[A]) {
    add[A](provider, defaultScope)
  }

  def decorate[A <: Object, B <: A](interface: Class[A], concrete: Class[B]) {
    decorate(interface, concrete, defaultScope)
  }

  def add[C <: Object](scope: Scope[C])(implicit manifestConcrete: Manifest[C]) {
    val concrete = manifestConcrete.erasure.asInstanceOf[Class[C]]
    add[C](() => createInstance(concrete), scope)
  }

  def add[I <: Object, C <: I](scope: Scope[I])(implicit manifestInterface: Manifest[I], manifestConcrete: Manifest[C] ) {
    val concrete = manifestConcrete.erasure.asInstanceOf[Class[C]]
    add[I](() => createInstance(concrete), scope)
  }

  def add[A <: Object](provider: () => A, scope: Scope[A])(implicit manifest: Manifest[A]) {
    val aClass = manifest.erasure.asInstanceOf[Class[A]]
    activators.containsKey(aClass) match {
      case true => throw new ContainerException(aClass.getName + " already added to container")
      case false => activators.put(aClass, scope(provider))
    }
  }

  def decorate[I <: Object, C <: I](interface: Class[I], concrete: Class[C], scope: Scope[I]) {
    val existing = activators.get(interface)
    activators.put(interface, scope(() => createInstance(concrete, (aClass: Class[_]) => {
      if (aClass.equals(interface)) existing.activate() else resolve(aClass)
    })))
  }

  def createInstance[T <: Object](aClass: Class[T]): T = createInstance(aClass, resolve(_))

  def createInstance[T <: Object](aClass: Class[T], resolver: (Class[_]) => Object): T = {
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