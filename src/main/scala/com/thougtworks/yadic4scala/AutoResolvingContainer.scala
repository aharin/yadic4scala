package com.thougtworks.yadic4scala

import java.lang.Class

class AutoResolvingContainer() extends SimpleContainer() {

  override def resolveMissing(aClass: Class[_]) = {
    createInstance[Object]()(Manifest.classType(aClass))
  }
}