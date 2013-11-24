/*
 * Copyright (c) 2013  Bridgewater Associates, LP
 *
 * Distributed under the terms of the Modified BSD License.  The full license is in
 * the file COPYING, distributed as part of this software.
 */

package com.bwater.notebook.widgets

import org.json4s.JsonDSL._
import com.bwater.notebook._


class InputBox(initial: String) extends Widget {
  private[this] val connection = JSBus.createConnection
  val currentValue = connection biMap JsonCodec.strings

  currentValue <-- Connection.just(initial)

  lazy val toHtml = <input data-bind="value: value">{
    scopedScript(
      "require(['observable', 'knockout'], function (Observable, ko) { ko.applyBindings({ value: Observable.makeObservable(valueId) }, this); })",
      ("valueId" -> connection.id)
    )
    }</input>
}