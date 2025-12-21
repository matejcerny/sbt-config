package com.example

import cats.syntax.all.*

@main def run(): Unit =
  val message = Option("Hello from sbt-config!").getOrElse("No message")
  println(message)
