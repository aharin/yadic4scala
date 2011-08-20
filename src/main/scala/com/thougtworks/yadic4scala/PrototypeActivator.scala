package com.thougtworks.yadic4scala

class PrototypeActivator(provider: () => Object) extends Activator {
  def activate() = {
    provider()
  }
}