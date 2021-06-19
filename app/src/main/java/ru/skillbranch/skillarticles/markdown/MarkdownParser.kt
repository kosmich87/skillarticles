package ru.skillbranch.skillarticles.markdown

import android.util.Log
import java.util.regex.Pattern

object MarkdownParser {

    private val LINE_SEPARATOR = "\\"

    //group regex
    private const val UNORDERED_LIST_ITEM_GROUP = "(^[*+-] .+$)"
    private const val ORDERED_LIST_ITEM_GROUP = "(^\\d+\\. .+$)"
    private const val HEADER_GROUP = "(^#{1,6} .+?$)"
    private const val QUOTE_GROUP = "(^> .+?$)"
    private const val ITALIC_GROUP = "((?<!\\*)\\*[^*].*?[^*]?\\*(?!\\*)|(?<!_)_[^_].*?[^_]?_(?!_))"
    private const val BOLD_GROUP =
        "((?<!\\*)\\*{2}[^*].*?[^*]?\\*{2}(?!\\*)|(?<!_)_{2}[^_].*?[^_]?_{2}(?!_))"
    private const val STRIKE_GROUP = "((?<!~)~{2}[^~].*?[^~]?~{2}(?!~))"
    private const val RULE_GROUP = "(^[-_*]{3}$)"
    private const val INLINE_GROUP = "((?<!`)`[^`\\s].*?[^`\\s]?`(?!`))"
    private const val MULTILINE_GROUP = "(```\\S.*?[\\s\\S]*?```)"
    private const val LINK_GROUP = "(\\[[^\\[\\]]*?]\\(.+?\\)|^\\[*?]\\(.*?\\))"
    private const val IMAGE_GROUP = "(!\\[.*\\]\\([^\\)]+\\))"


    //result regex
    private const val MARKDOWN_GROUPS = "$UNORDERED_LIST_ITEM_GROUP" +
            "|$HEADER_GROUP" +
            "|$QUOTE_GROUP" +
            "|$ITALIC_GROUP" +
            "|$BOLD_GROUP" +
            "|$STRIKE_GROUP" +
            "|$RULE_GROUP" +
            "|$INLINE_GROUP" +
            "|$LINK_GROUP" +
            "|$ORDERED_LIST_ITEM_GROUP" +
            "|$IMAGE_GROUP" +
            "|$MULTILINE_GROUP"

    private val elementsPattern by lazy { Pattern.compile(MARKDOWN_GROUPS, Pattern.MULTILINE) }

    fun parse(string: String): MarkdownText {
        val elements = mutableListOf<Element>()
        elements.addAll(findElements(string))
        return MarkdownText(elements)
    }

    fun clear(string: String?): String? {
        val matcher = elementsPattern.matcher(string)
        var lastStartIndex = 0
        var result: String? = null

        loop@while (matcher.find(lastStartIndex)) {
            val startIndex = matcher.start()
            val endIndex = matcher.end()
            if (lastStartIndex < startIndex){
                if (result == null) result = ""
                result = result.plus(string?.subSequence(lastStartIndex, startIndex))
            }

            val groups = 1..12
            var group = -1
            for (gr in groups){
                if (matcher.group(gr) != null){
                    group = gr
                    break
                }
            }

            when (group) {
                //not found
                -1 -> break@loop

                //unordered list
                1 -> {
                    //text without "*. "
                    result = result.plus(string?.subSequence(startIndex.plus(2), endIndex))
                    lastStartIndex = endIndex
                }

                //header
                2 -> {
                    val reg = "^#{1,6}".toRegex().find(string?.subSequence(startIndex, endIndex)!!)
                    val level = reg!!.value.length

                    //text without "{#} "
                    result = result.plus(string?.subSequence(startIndex.plus(level.inc()), endIndex))
                    lastStartIndex = endIndex
                }

                //quote
                3 -> {
                    //text without "> "
                    result = result.plus(string?.subSequence(startIndex.plus(2), endIndex))
                    result = clear(result) ?: ""
                    lastStartIndex = endIndex
                }

                //italic
                4 -> {
                    //text without "*{}*"
                    result = result.plus(string?.subSequence(startIndex.inc(), endIndex.dec()))
                    result = clear(result) ?: ""
                    lastStartIndex = endIndex
                }

                //bold
                5 -> {
                    //text without "**{}**"
                    result = result.plus(string?.subSequence(startIndex.plus(2), endIndex.plus(-2)))
                    result = clear(result) ?: ""
                    lastStartIndex = endIndex
                }

                //strike
                6 -> {
                    //text without "--{}--"
                    result = result.plus(string?.subSequence(startIndex.plus(2), endIndex.plus(-2)))
                    result = clear(result) ?: ""
                    lastStartIndex = endIndex
                }

                //rule
                7 -> {
                    result = result.plus(" ")
                    lastStartIndex = endIndex
                }

                //inline code
                8 -> {
                    //text without "`{}`"
                    result = result.plus(string?.subSequence(startIndex.inc(), endIndex.dec()))
                    lastStartIndex = endIndex
                }

                //link
                9 -> {
                    //full text for regex
                    var text = string?.subSequence(startIndex, endIndex)
                    val (title: String, link: String) =
                        "\\[(.*)]\\((.*)\\)".toRegex().find(text!!)!!.destructured
                    result = result.plus(title)
                    lastStartIndex = endIndex
                }

                //ordered list
                10 -> {
                    //text without "\d. "
                    var text = string?.subSequence(startIndex, endIndex)
                    val (order: String, title: String) = "(\\d+\\.) (.+\$)".toRegex().find(text!!)!!.destructured

                    result = result.plus(title)
                    lastStartIndex = endIndex
                }

                //image
                11 -> {
                    result = result.plus(string?.subSequence(startIndex, endIndex))
                    lastStartIndex = endIndex
                }

                //multiline block
                12 -> {
                    result = result.plus(string?.subSequence(startIndex.plus(3), endIndex.plus(-3)))
                    lastStartIndex = endIndex
                }
            }
        }

        if (lastStartIndex < string?.length!!) {
            if (result == null) result = ""
            result = result.plus(string?.subSequence(lastStartIndex, string.length))
        }

        return result
    }
    private fun findElements(string: CharSequence): List<Element> {
        val parents = mutableListOf<Element>()
        val matcher = elementsPattern.matcher(string)
        var lastStartIndex = 0

        loop@while (matcher.find(lastStartIndex)) {
            val startIndex = matcher.start()
            val endIndex = matcher.end()

            //if something is found then everything before - text
            if (lastStartIndex < startIndex){
                parents.add(Element.Text(string.subSequence(lastStartIndex, startIndex)))
            }

            //found text
            var text: CharSequence

            //groups range for iterate by group
            val groups = 1..12
            var group = -1
            for (gr in groups){
                if (matcher.group(gr) != null){
                    group = gr
                    break
                }
            }

            var count = matcher.groupCount()

            when (group){
                //not found
                -1 -> break@loop

                //unordered list
                1 -> {
                    //text without "*. "
                    text = string.subSequence(startIndex.plus(2), endIndex)

                    //find inner elements
                    val subs = findElements(text)
                    val element = Element.UnorderedListItem(text, subs)
                    parents.add(element)

                    lastStartIndex = endIndex
                }

                //header
                2 -> {
                    val reg = "^#{1,6}".toRegex().find(string.subSequence(startIndex, endIndex))
                    val level = reg!!.value.length

                    //text without "{#} "
                    text = string.subSequence(startIndex.plus(level.inc()), endIndex)

                    val element = Element.Header(level, text)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //quote
                3 -> {
                    //text without "> "
                    text = string.subSequence(startIndex.plus(2), endIndex)
                    val subelements = findElements(text)
                    val element = Element.Quote(text, subelements)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //italic
                4 -> {
                    //text without "*{}*"
                    text = string.subSequence(startIndex.inc(), endIndex.dec())
                    val subelements = findElements(text)
                    val element = Element.Italic(text, subelements)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //bold
                5 -> {
                    //text without "**{}**"
                    text = string.subSequence(startIndex.plus(2), endIndex.plus(-2))
                    val subelements = findElements(text)
                    val element = Element.Bold(text, subelements)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //strike
                6 -> {
                    //text without "--{}--"
                    text = string.subSequence(startIndex.plus(2), endIndex.plus(-2))
                    val subelements = findElements(text)
                    val element = Element.Strike(text, subelements)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //rule
                7 -> {
                    //text without "***" insert empty character
                    val element = Element.Rule()
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //inline code
                8 -> {
                    //text without "`{}`"
                    text = string.subSequence(startIndex.inc(), endIndex.dec())
                    val element = Element.InlineCode(text)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //link
                9 -> {
                    //full text for regex
                    text = string.subSequence(startIndex, endIndex)
                    val (title: String, link: String) =
                        "\\[(.*)]\\((.*)\\)".toRegex().find(text)!!.destructured
                    val element = Element.Link(link, title)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //ordered list
                10 -> {
                    //text without "\d. "
                    text = string.subSequence(startIndex, endIndex)

                    val (order: String, title: String) = "(\\d+\\.) (.+\$)".toRegex().find(text)!!.destructured
                    //find inner elements
                    val subs = findElements(title)
                    val element = Element.OrderedListItem(order, title, subs)
                    parents.add(element)

                    lastStartIndex = endIndex
                }

                //image
                11 -> {
                    text = string.subSequence(startIndex, endIndex)
                    val (alt: String?, urlTitle: String) =
                        "\\[(.*)]\\((.*)\\)".toRegex().find(text)!!.destructured
                    var title = ""
                    var url = urlTitle
                    if (urlTitle.contains("\"")){
                        url = "^(.*? )".toRegex().find(urlTitle)!!.destructured.component1().trim()
                        title = "\"(.*?)\"".toRegex().find(urlTitle)!!.destructured.component1()
                    }

                    val element = Element.Image(url, if (alt.isBlank()) null else alt, title)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //multiline block
                12 -> {
                    text = string.subSequence(startIndex.plus(3), endIndex.plus(-3))
                    val element = Element.BlockCode(Element.BlockCode.Type.MIDDLE, text)
                    parents.add(element)
                    lastStartIndex = endIndex
                }
            }
        }

        if (lastStartIndex < string.length) {
            val text = string.subSequence(lastStartIndex, string.length)
            parents.add(Element.Text(text))
        }

        return parents
    }

}

data class MarkdownText(val elements: List<Element>)

sealed class Element(){
    abstract val text: CharSequence
    abstract val elements: List<Element>

    data class Text(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ): Element()

    data class UnorderedListItem(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ): Element()

    data class Header(
        val level: Int = 1,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ): Element()

    data class Quote(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ): Element()

    data class Italic(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ): Element()

    data class Bold(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ): Element()

    data class Strike(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ): Element()

    data class Rule(
        override val text: CharSequence = " ", //for span
        override val elements: List<Element> = emptyList()
    ): Element()

    data class InlineCode(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ): Element()

    data class Link(
        val link: String,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ): Element()

    data class OrderedListItem(
        val order: String,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ): Element()

    data class BlockCode(
        val type: Type = Type.MIDDLE,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ): Element() {
        enum class Type {START, END, MIDDLE, SINGLE}
    }

    data class Image(
        val url: String,
        val alt: String?,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ): Element()
}