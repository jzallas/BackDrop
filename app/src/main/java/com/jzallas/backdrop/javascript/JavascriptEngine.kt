package com.jzallas.backdrop.javascript

/**
 * Contract between [JavascriptEvaluator] and the javascript platform.
 *
 * Javascript execution is expected to always seem synchronous from the perspective of the native application.
 * In other words, the javascript function may be asynchronous, but the engine should wait for the response
 * before resuming execution.
 */
interface JavascriptEngine {
  /**
   * Call a javascript function.
   *
   * @param entryPoint location of a javascript asset
   * @param function function in the javascript file
   * @param args any dynamic arguments required for the [function]
   * @return the expected return value from the function
   */
  fun call(entryPoint: String, function: String, args: String): String
}
