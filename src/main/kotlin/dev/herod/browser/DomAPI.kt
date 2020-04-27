package dev.herod.browser

import org.w3c.dom.Document
import org.w3c.dom.Element

val Document.readyStateString: String
    get() = readyState.unsafeCast<String>()

val Element.innerText: String
    get() = asDynamic().innerText as String

inline fun Element.click() {
    asDynamic().click()
}
