package com.thougtworks.yadic4scala

class LazyActivator(activator: () => Object) extends Activator {
  lazy val activate = activator()
}