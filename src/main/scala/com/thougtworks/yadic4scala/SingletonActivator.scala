package com.thougtworks.yadic4scala

class SingletonActivator(provider: () => Object) extends Activator {
  lazy val activate = provider()
}