package com.thougtworks.yadic4scala

trait Container {

  type Scope[C <: Object] = (() => C) => Activator

  def add[C <: Object]()(implicit manifest: Manifest[C])
  def add[I <: Object, C <: I](interface:Class[I], concrete:Class[C])
  def add[A <: Object](aClass:Class[A], provider:() => A )
  def decorate[I <: Object, C <: I](interface:Class[I], concrete:Class[C])

  def add[C <: Object](scope: Scope[C])(implicit manifest: Manifest[C])
  def add[I <: Object, C <: I](interface:Class[I], concrete:Class[C], scope: Scope[I])
  def add[A <: Object](aClass:Class[A], provider:() => A, scope: Scope[A])
  def decorate[I <: Object, C <: I](interface:Class[I], concrete:Class[C], scope: Scope[I])


  def resolve( aClass:Class[_] ): Object
  def resolveType[A <: Object]( aClass:Class[A] ): A
}