@file:JsQualifier("chrome.extension")
@file:Suppress("unused")

package chrome.extension

external fun sendMessage(
    extensionId: String = definedExternally,
    message: dynamic = definedExternally,
    options: dynamic = definedExternally,
    responseCallback: dynamic = definedExternally
)
