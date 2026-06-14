package example

import com.google.gson.Gson

object JvmMain:
  val gson: Gson = new Gson()
  val json: String = gson.toJson(Shared.message)
