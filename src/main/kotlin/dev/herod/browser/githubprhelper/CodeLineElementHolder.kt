package dev.herod.browser.githubprhelper

import org.w3c.dom.Element

data class CodeLineElementHolder(
    val element: Element,
    val addition: Boolean,
    val deletion: Boolean,
    val innerText: String
) {
    override fun toString(): String {
        return "CodeLineElementHolder(element=$element, addition=$addition, deletion=$deletion, innerText='$innerText')"
    }
}

data class CodeBlobHolder(
    val changedLines: List<CodeLineElementHolder>
) {
    override fun toString(): String {
        return "CodeBlobHolder(changedLines=$changedLines)"
    }
}
