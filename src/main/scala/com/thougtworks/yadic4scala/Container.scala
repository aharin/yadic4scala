package com.thougtworks.yadic4scala

trait Container {

  type Scope[C <: Object] = (() => C) => Activator

  def add[C <: Object]()(implicit manifestConcrete: Manifest[C])
  def add[I <: Object, C <: I]()(implicit manifestInterface: Manifest[I], manifestConcrete: Manifest[C] )
  def add[A <: Object](provider:() => A )(implicit manifest: Manifest[A])
  def decorate[I <: Object, C <: I]()(implicit manifestInterface: Manifest[I], manifestConcrete: Manifest[C])

  def add[C <: Object](scope: Scope[C])(implicit manifest: Manifest[C])
  def add[I <: Object, C <: I](scope: Scope[I])(implicit manifestInterface: Manifest[I], manifestConcrete: Manifest[C] )
  def add[A <: Object](provider:() => A, scope: Scope[A])(implicit manifest: Manifest[A])
  def decorate[I <: Object, C <: I](scope: Scope[I])(implicit manifestInterface: Manifest[I], manifestConcrete: Manifest[C])


  def resolve( aClass:Class[_] ): Object
  def resolveType[A <: Object]()(implicit manifest: Manifest[A]): A
}