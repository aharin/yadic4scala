package com.thougtworks.yadic4scala

import com.thougtworks.yadic4scala.SimpleContainerTest._
import java.util.ArrayList
import java.util.List
import java.util.concurrent.{TimeUnit, Future, Executors, Callable}
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

class SimpleContainerTest extends FunSuite with ShouldMatchers with CustomMatchers {

  test("resolveShouldThrowExceptionIfConstructorIsNotSatifiable") {
    val container = new SimpleContainer
    container.add(classOf[MyThing])
    evaluating {
      container.resolve(classOf[MyThing])
    } should produce[ContainerException]
  }

  test("shouldOnlyCallCreationLambdaOnceEvenFromDifferentThreads") {
    var count = 0
    val container = new SimpleContainer

    container.add(classOf[Thing], () => {
      count = count + 1
      Thread.sleep(10)
      new ThingWithNoDependencies
    })

    val service = Executors.newFixedThreadPool(2)

    val collection = new ArrayList[Callable[Thing]]
    collection.add(new Creator(container))
    collection.add(new Creator(container))
    val results: List[Future[Thing]] = service.invokeAll(collection)
    service.shutdown()
    service.awaitTermination(50, TimeUnit.MILLISECONDS)

    count should equal(1)
    results.get(0).get should be theSameInstanceAs results.get(1).get
  }

  test("shouldResolveUsingConstructorWithMostDependenciesThatIsSatisfiable") {
    val container = new SimpleContainer
    container.add(classOf[MyThingWithReverseConstructor])

    val myThing: MyThingWithReverseConstructor = container.resolveType(classOf[MyThingWithReverseConstructor])

    myThing.dependency should be(null)
  }

  test("shouldChainContainersThroughMissingAction") {
    val parent = new SimpleContainer
    parent.add(classOf[Thing], classOf[ThingWithNoDependencies])

    val child = new SimpleContainer(parent.resolve)

    val thing = child.resolveType(classOf[Thing])

    thing should be(anInstanceOf[ThingWithNoDependencies])
  }

  test("shouldResolveByType") {
    val container = new SimpleContainer
    container.add(classOf[Thing], classOf[ThingWithNoDependencies])

    val thing = container.resolveType(classOf[Thing])

    thing should be(anInstanceOf[ThingWithNoDependencies])
  }

  test("shouldCallMissingMethodWhenItemNotFound") {
    var wasCalled = false
    val container = new SimpleContainer((_) => {
      wasCalled = true
      null
    })
    container.resolveType(classOf[Thing])

    wasCalled should be(true)
  }

  test("shouldOnlyCallCreationLambdaOnce") {
    var count = 0
    val container = new SimpleContainer

    container.add(classOf[Thing], () => {
      count = count + 1
      new ThingWithNoDependencies
    })

    container.resolveType(classOf[Thing])
    container.resolveType(classOf[Thing])
    count should be(1)
  }

  test("shouldDecorateAnExistingComponent") {
    val container = new SimpleContainer
    container.add(classOf[Thing], classOf[ThingWithNoDependencies])
    container.decorate(classOf[Thing], classOf[DecoratedThing])

    val thing = container.resolveType(classOf[Thing])

    thing should be(anInstanceOf[DecoratedThing])
    thing.dependency should be(anInstanceOf[ThingWithNoDependencies])
  }

  test("shouldAddAndReolveByConcrete") {
    val container = new SimpleContainer
    container.add(classOf[Thing], () => new ThingWithNoDependencies)

    val thing = container.resolveType(classOf[Thing])
    thing should be(anInstanceOf[ThingWithNoDependencies])
  }

  test("shouldAddAndResolveByInterface") {
    val container = new SimpleContainer
    container.add(classOf[Thing], classOf[ThingWithNoDependencies])

    val thing = container.resolveType(classOf[Thing])

    thing should be(anInstanceOf[ThingWithNoDependencies])
  }

  test("shouldThrowExceptionIfAddSameTypeTwice") {
    val container = new SimpleContainer
    container.add(classOf[MyThing])
    evaluating {
      container.add(classOf[MyThing])
    } should produce[ContainerException]
  }

  test("resolveShouldThrowExceptionIfTypeNotInContainer") {
    val container = new SimpleContainer
    evaluating {
      container.resolveType(classOf[MyThing])
    } should produce[ContainerException]
  }

  test("shouldAddAndResolveByClass") {
    val container = new SimpleContainer
    container.add(classOf[ThingWithNoDependencies])

    val result = container.resolveType(classOf[ThingWithNoDependencies])
    result should be(anInstanceOf[ThingWithNoDependencies])
  }

  test("resolveShouldReturnSameInstanceWhenCalledTwice") {
    val container = new SimpleContainer
    container.add(classOf[ThingWithNoDependencies])

    val result1 = container.resolveType(classOf[ThingWithNoDependencies])
    val result2 = container.resolveType(classOf[ThingWithNoDependencies])

    result1 should be theSameInstanceAs result2
  }
  test("shouldResolveDependencies") {
    val container = new SimpleContainer
    container.add(classOf[MyDependency])
    container.add(classOf[ThingWithNoDependencies])

    val myThing = container.resolveType(classOf[MyDependency])
    myThing should be(anInstanceOf[MyDependency])
  }

  test("shouldRecursivelyResolveDependencies") {
    val container = new SimpleContainer
    container.add(classOf[MyThing])
    container.add(classOf[MyDependency])
    container.add(classOf[ThingWithNoDependencies])

    val myThing = container.resolveType(classOf[MyThing])

    myThing.dependency should be(anInstanceOf[MyDependency])
    myThing.dependency.dependency should be(anInstanceOf[ThingWithNoDependencies])
  }

  test("shouldResolveWithDependenciesInAnyOrder") {
    val container = new SimpleContainer
    container.add(classOf[MyDependency])
    container.add(classOf[MyThing])
    container.add(classOf[ThingWithNoDependencies])

    val myThing = container.resolveType(classOf[MyThing])

    withClue("1st level Dependency was not fulfilled") {
      myThing.dependency should be(anInstanceOf[MyDependency])
    }
    withClue("2nd level Dependency was not fulfilled") {
      myThing.dependency.dependency should be(anInstanceOf[ThingWithNoDependencies])
    }

  }

  test("shouldResolveUsingConstructorWithMostDependencies") {
    val container = new SimpleContainer
    container.add(classOf[MyThingWithReverseConstructor])
    container.add(classOf[ThingWithNoDependencies])

    val myThing: MyThingWithReverseConstructor = container.resolveType(classOf[MyThingWithReverseConstructor])

    withClue("Wrong constructor was used") {
      myThing.dependency should not be (null)
    }

    myThing.dependency should be(anInstanceOf[ThingWithNoDependencies])
  }

  test("shouldResolveConcreteClassWithoutRegistration") {
    val container = new AutoResolvingContainer()

    val myThing = container.resolveType(classOf[MyThing])
    myThing should not be (null)
  }
}

object SimpleContainerTest {

  class Creator(container: SimpleContainer) extends Callable[Thing] {
    def call = container.resolveType(classOf[Thing])
  }

  class MyThingWithReverseConstructor(val dependency: ThingWithNoDependencies) extends Thing {
    def this() = this (null)
  }

  class MyThing(val dependency: MyDependency) extends Thing

  class MyDependency(val dependency: ThingWithNoDependencies) extends Thing

  class ThingWithNoDependencies extends Thing {
    val dependency: Thing = null
  }

  class DecoratedThing(val dependency: Thing) extends Thing

  trait Thing {
    val dependency: Thing
  }

}