package com.bwater.notebook
package server

import unfiltered.netty.websockets.WebSocket
import java.util.UUID
import com.bwater.notebook.util.Logging
import org.json4s.JsonAST._
import org.json4s.JsonDSL._
import org.json4s.native.JsonMethods

/**
 * Author: Ken
 */

trait WebSockWrapper extends JsonMethods {
  def send(header: JValue, session: JValue, msgType: String, content: JValue)
}

class WebSockWrapperImpl(sock: WebSocket) extends WebSockWrapper with Logging {
  private def send(msg: String) {
    logTrace("Sending " + msg)
    sock.send(msg)
  }

  def send(header: JValue, session: JValue, msgType: String, content: JValue) {
    val respJson = ("parent_header" -> header) ~
      ("msg_type" -> msgType) ~
      ("msg_id" -> UUID.randomUUID().toString) ~
      ("content" -> content) ~
      ("header" -> ("username" -> "kernel") ~
        ("session" -> session) ~
        ("msg_id" -> UUID.randomUUID().toString) ~
        ("msg_type" -> msgType))

    send(pretty(render(respJson)))
  }
}
