package dev.herod.browser

import kotlinx.coroutines.*
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.events.Event
import kotlin.browser.document
import kotlin.browser.window
import kotlin.properties.Delegates

fun onPageReady(function: suspend () -> Unit) {
    var handle by Delegates.notNull<Int>()
    handle = window.setInterval({
        if (document.readyStateString == "complete") {
            window.clearInterval(handle)
            GlobalScope.launch { function() }
        }
    }, 10)
}

fun onUrlChange(function: suspend () -> Unit): Job = GlobalScope.launch {
    var href = window.location.href
    while (true) {
        launch {
            val newLocation = window.location
            val newHref = newLocation.href
            if (href != newHref) {
                println("url changed from $href to $newHref")
                launch { function() }
                href = newHref
            }
        }
        delay(300)
    }
}

fun onScrollDebounced(function: suspend (Event) -> Unit) {
    document.onscroll = { event ->
        GlobalScope.launch {
            println("on ${event.type} $event")
            if (event.type == "scroll") {
                delay(100)
                withTimeout(3000) {
                    function(event)
                }
            }
        }
    }
}

tailrec fun Node.findParent(predicate: (Element) -> Boolean): Element? {
    val parentNode = parentElement ?: return null
    if (predicate.invoke(parentNode)) return parentNode
    return parentNode.findParent(predicate)
}
