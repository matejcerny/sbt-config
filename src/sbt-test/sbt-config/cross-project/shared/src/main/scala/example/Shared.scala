package example

import cats.syntax.all.*

object Shared:
  val message: String = "hello".some.getOrElse("world")
