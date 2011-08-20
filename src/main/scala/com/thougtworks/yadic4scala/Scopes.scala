package com.thougtworks.yadic4scala

object Scopes {

  private def prototypeActivator[A <: Object](provider: () => A): Activator = {
    new PrototypeActivator(provider)
  }

  private def singletonActivator[A <: Object](provider: () => A): Activator = {
    new SingletonActivator(provider)
  }

  val prototype = prototypeActivator _

  val singleton = singletonActivator _


}