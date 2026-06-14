package example

object NativeMain:
  // References shared code and native-only dependency
  val sharedValue: String = Shared.message
  val epollRuntime: String = classOf[epollcat.EpollRuntime].getName
