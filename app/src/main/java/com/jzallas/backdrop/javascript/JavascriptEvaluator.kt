package com.jzallas.backdrop.javascript

/**
 * Executes blocking javascript requests.
 *
 * Communication to and from javascript is done primarily in json since it is a platform agnostic format.
 *
 * @property engine the underlying implementation that executes javascript
 * @property entryPoint denotes the javascript file that the [engine] should execute against
 */
class JavascriptEvaluator(
  private val engine: JavascriptEngine,
  private val entryPoint: String
) {
  /**
   * Call a function in the script
   *
   * @param function the name of the javascript function to execute
   * @param args the function arguments as a json string
   * @return the response as a json string
   */
  fun call(function: String, args: String): String {
    return engine.call(entryPoint, function, args)
  }
}
