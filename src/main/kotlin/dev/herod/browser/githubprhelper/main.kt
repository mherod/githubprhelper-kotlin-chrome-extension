@file:Suppress("unused")

package dev.herod.browser.githubprhelper

import chrome.extension.sendMessage
import dev.herod.browser.*
import kotlinx.coroutines.delay
import org.w3c.dom.Element
import org.w3c.dom.asList
import kotlin.browser.document
import kotlin.browser.window
import kotlin.js.Date

val ignored: MutableList<String> = mutableListOf()

fun main() {
    sendMessage(responseCallback = {
        println("hello world")
        onPageReady {
            redirectForHideWhitespace()
            checkWhitespaceViewed()
            checkImportsChangeViewed()
            checkDeletedFilesViewed()
        }
        onUrlChange {
            redirectForHideWhitespace()
        }
        onScrollDebounced() {
            ViewedCheckBoxMap.freshElements()
            checkWhitespaceViewed()
            checkImportsChangeViewed()
            checkDeletedFilesViewed()
            autoLoadDiffs()
        }
    })
}

private suspend fun expandCode(diff: String) {
    if (diff in ignored) return
    val querySelector =
        document.querySelector("#${diff} > div.file-header.d-flex.flex-items-center.file-header--expandable.js-file-header.sticky-file-header.js-position-sticky > div.file-info.flex-auto > button")
    val expand = querySelector ?: return
    val viewed = ViewedCheckBoxMap[diff]?.hasAttribute("checked") == true
    if (viewed) {

        measureTimeMs {
            while ((expand.getAttribute("real-expanded") != "false")) {
                expand.click()
                expand.setAttribute("real-expanded", "${expand.getAttribute("aria-expanded")?.toBoolean()}")
            }
        }
    }
}

private val indexedUncheckedCheckboxes: Map<String, Element>
    get() = ViewedCheckBoxMap.filterValues { !it.hasAttribute("checked") }

private suspend fun autoLoadDiffs() {

    ViewedCheckBoxMap.keys
        .filter { it !in ignored }
        .forEach { key -> expandCode(key) }

    measureTimeMs {
        document.getElementsByClassName("text-bold f4 mb-3 js-button-text")
            .asList()
            .filter { it.innerText == "Load diff" }
            .forEach { it.click() }
    }
}

suspend fun measureTimeMs(function: suspend () -> Unit): Int {
    val timeBefore = Date()
    function.invoke()
    val timeAfter = Date()
    val diffTime = timeAfter.getMilliseconds() - timeBefore.getMilliseconds()
    if (diffTime > 0)
        console.log("diffTime: $diffTime")
    return diffTime
}

private suspend fun checkDeletedFilesViewed() {
    document.getElementsByClassName("file js-file js-details-container js-targetable-element")
        .asList()
        .filter { it.id !in ignored }
        .filter { it.getAttribute("data-file-deleted") == "true" }
        .forEach { element ->
            val diff = element.id
            markViewed(diff)
        }
}

private suspend fun checkImportsChangeViewed() {
    codeBlobs()
//        .filterKeys { it !in ignored }
//        .filterKeys { it in indexedUncheckedCheckboxes.keys }
        .forEach { (diff: String, elements: List<Element>) ->

            val codeBlob = CodeBlobHolder(
                changedLines = elements.map { element ->
                    val addition = element.findParent { "blob-code-addition" in it.className } != null
                    val deletion = !addition && element.findParent { "blob-code-deletion" in it.className } != null
                    CodeLineElementHolder(
                        element = element,
                        addition = addition,
                        deletion = deletion,
                        innerText = element.innerText
                    )
                }.filter { holder: CodeLineElementHolder ->
                    (holder.addition || holder.deletion) && holder.innerText.isNotBlank()
                }
            )

            val trimmedLines = codeBlob.changedLines.map { it.innerText.trim() }

            console.log(trimmedLines)

            if (trimmedLines.all { it.startsWith("import ") || it.startsWith("package ") }) {
                console.log("import/package only changes for $diff")
                markViewed(diff)
            } else if (codeBlob.changedLines.none { it.addition } && codeBlob.changedLines.all { it.deletion }) {
                console.log("delete only changes for $diff")
                markViewed(diff)
            }
        }
}

private suspend fun markViewed(diff: String) {
    while (indexedUncheckedCheckboxes[diff]?.hasAttribute("checked") == false) {
        indexedUncheckedCheckboxes[diff]?.click()
        delay(100)
    }
//    ignored += diff
}

fun diff(element: Element) = element.findParent { "diff-" in it.id }

private fun codeBlobs(): Map<String, List<Element>> =
    document.getElementsByClassName("blob-code-inner blob-code-marker")
        .asList()
        .groupBy { diff(it)?.id }
        .filterKeys { it != null }
        .unsafeCast<Map<String, List<Element>>>()

private suspend fun checkWhitespaceViewed() {
    measureTimeMs {
        indexedUncheckedCheckboxes
            .filterKeys { it !in ignored }
            .let { map ->
                map.keys.filter { isWhitespace(it) }.forEach { diff ->
                    markViewed(diff)
                }
            }
    }
}

private fun isWhitespace(id: String): Boolean {
    return "Whitespace-only changes" in document.querySelector("#$id > div.js-file-content.Details-content--hidden > div")?.innerHTML.orEmpty()
}

private fun redirectForHideWhitespace() {
    val url = window.location.href
    if ("pull" in url && "files" in url && "w=1" !in url) {
        window.location.href = url + (if ("?" !in url) "?" else "&") + "w=1"
        println(url)
    }
}
