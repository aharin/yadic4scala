package com.thougtworks.yadic4scala

import org.scalatest.matchers.{BePropertyMatchResult, BePropertyMatcher}


trait CustomMatchers {

  def anInstanceOf[T](implicit manifest: Manifest[T]) = {
    val clazz = manifest.erasure.asInstanceOf[Class[T]]
    new BePropertyMatcher[AnyRef] {
      def apply(left: AnyRef) =
        BePropertyMatchResult(clazz.isAssignableFrom(left.getClass), "an instance of " + clazz.getName)
    }
  }
}