package dev.herod.browser.githubprhelper

import org.w3c.dom.Element
import org.w3c.dom.asList
import kotlin.browser.document

object ViewedCheckBoxMap : LinkedHashMap<String, Element>() {

    init {
        freshElements()
    }

    override fun get(key: String): Element? = freshElements()
        .firstOrNull { diff(it)?.id == key }

    fun freshElements(): List<Element> = document
        .getElementsByClassName("mr-1 js-reviewed-checkbox")
        .asList()
        .onEach { element ->
            diff(element)?.id?.let { diffId ->
                put(diffId, element)
            }
        }
}
