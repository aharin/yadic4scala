package com.thougtworks.yadic4scala

import java.lang.Class
import java.util.HashMap

class SimpleContainer(missingHandler: (Class[_]) => Object) extends Container {

  def this() = this ((aClass:Class[_]) => {throw new ContainerException(aClass.getName + " not found in container")})

  val defaultScope = Scopes.singleton

  val activators = new HashMap[Class[_], Activator]

  def resolve(aClass: Class[_]): Object = {
    activators.get(aClass) match {
      case null => resolveMissing(aClass)
      case activator:Activator => activator.activate()
    }
  }

  def resolveMissing(aClass: Class[_]) = {
    missingHandler(aClass)
  }

  def resolveType[A <: Object]( aClass:Class[A] ): A = resolve(aClass).asInstanceOf[A]

  def add[C <: Object](concrete: Class[C]) {
    add(concrete, defaultScope)
  }
            
  def add[I <: Object, C <: I](interface: Class[I], concrete: Class[C]) {
    add(interface, concrete, defaultScope)
  }

  def add[A <: Object](aClass: Class[A], provider: () => A) {
    add(aClass, provider, defaultScope)
  }

  def decorate[A <: Object, B <: A](interface: Class[A], concrete: Class[B]) {
    decorate(interface, concrete, defaultScope)
  }

  def add[C <: Object](concrete: Class[C], scope: Scope[C]) {
    add(concrete, () => createInstance(concrete), scope)
  }

  def add[I <: Object, C <: I](interface: Class[I], concrete: Class[C], scope: Scope[I]) {
    add(interface, () => createInstance(concrete), scope)
  }

  def add[A <: Object](aClass: Class[A], provider: () => A, scope: Scope[A]) {
    activators.containsKey(aClass) match {
      case true => throw new ContainerException(aClass.getName + " already added to container")
      case false => activators.put(aClass, scope(provider))
    }
  }

  def decorate[I <: Object, C <: I](interface: Class[I], concrete: Class[C], scope: Scope[I]) {
    val existing = activators.get(interface)
    activators.put(interface, scope(() => createInstance(concrete, (aClass: Class[_]) => {
      if(aClass.equals(interface)) existing.activate() else resolve(aClass)
    })))
  }

  def createInstance[T <: Object](aClass: Class[T]): T = createInstance(aClass, resolve(_))

  def createInstance[T <: Object](aClass: Class[T], resolver: (Class[_]) => Object ): T = {
    val constructors = aClass.getConstructors.toList.sortWith(_.getParameterTypes.length > _.getParameterTypes.length)
    constructors.foreach( constructor => {
      try {
        val instances = constructor.getParameterTypes.map( resolver(_) )
        return constructor.newInstance(instances: _*).asInstanceOf[T]
      } catch {
        case e:ContainerException =>
      }
    })
    throw new ContainerException(aClass.getName + " does not have a satisfiable constructor")
  }
}