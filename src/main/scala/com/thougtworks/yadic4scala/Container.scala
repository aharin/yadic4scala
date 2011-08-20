package com.thougtworks.yadic4scala

trait Container {
  def add[C <: Object](concrete:Class[C])
  def add[I <: Object, C <: I](interface:Class[I], concrete:Class[C])
  def add[A <: Object](aClass:Class[A], provider:() => A )
  def decorate[I <: Object, C <: I](interface:Class[I], concrete:Class[C])
  def resolve( aClass:Class[_] ): Object
  def resolveType[A <: Object]( aClass:Class[A] ): A
}