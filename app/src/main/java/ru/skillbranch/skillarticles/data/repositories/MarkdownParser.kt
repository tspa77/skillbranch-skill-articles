package ru.skillbranch.skillarticles.data.repositories

import java.lang.StringBuilder
import java.util.regex.Pattern

object MarkdownParser {
    private val LINE_SEPARATOR = System.getProperty("line.separator") ?: "\n"

    // group regex
    private const val UNORDERED_LIST_ITEM_GROUP = "(^[*+-] .+$)"
    private const val HEADER_GROUP = "(^#{1,6} .+?$)"
    private const val QUOTE_GROUP = "(^> .+?$)"
    private const val ITALIC_GROUP = "((?<!\\*)\\*[^*].*?[^*]?\\*(?!\\*)|(?<!_)_[^_].*?[^_]?_(?!_))"
    private const val BOLD_GROUP =
        "((?<!\\*)\\*{2}[^*].*?[^*]?\\*{2}(?!\\*)|(?<!_)_{2}[^_].*?[^_]?_{2}(?!_))"
    private const val STRIKE_GROUP = "((?<!~)~{2}[^~].*?[^~]?~{2}(?!~))"
    private const val RULE_GROUP = "(^[-_*]{3}$)"
    private const val INLINE_GROUP = "((?<!`)`[^`\\s].*?[^`\\s]?`(?!`))"
    private const val LINK_GROUP = "(\\[[^\\[\\]]*?]\\(.+?\\)|^\\[*?]\\(.*?\\))"
    private const val BLOCK_CODE_GROUP = "(^```[\\s\\S]+?```$)"         // GROUP 10
    private const val ORDER_LIST_GROUP = "(^\\d{1,2}\\.\\s.+?$)"        // GROUP 11
    private const val IMAGE_GROUP = "(^!\\[[^\\[\\]]*?\\]\\(.*?\\)$)"   // GROUP 12

    //result regex
    private const val MARKDOWN_GROUPS = "$UNORDERED_LIST_ITEM_GROUP|$HEADER_GROUP|$QUOTE_GROUP" +
            "|$ITALIC_GROUP|$BOLD_GROUP|$STRIKE_GROUP|$RULE_GROUP|$INLINE_GROUP|$LINK_GROUP|$BLOCK_CODE_GROUP|$ORDER_LIST_GROUP"

    private val elementsPattern by lazy { Pattern.compile(MARKDOWN_GROUPS, Pattern.MULTILINE) }


    /**
     * parse markdown text to elements
     */
    fun parse(string: String): MarkdownText {
        val elements = mutableListOf<Element>()
        elements.addAll(findElements(string))
        return MarkdownText(elements)
    }

    /**
     * clear markdown text to string without markdown characters
     */
    fun clear(string: String?): String? {
        string ?: return null
        val mt = parse(string)
        val element = Element.Text("", mt.elements)
        return getInnerText(element)
    }

    private fun getInnerText(element: Element): String {
        if (element.elements.size <= 1) return element.text.toString()

        val stringBuilder = StringBuilder()
        element.elements.forEach {
            stringBuilder.append(getInnerText(it))
        }
        return stringBuilder.toString()
    }

    /**
     * find markdown elements in markdown text
     */
    private fun findElements(string: CharSequence): List<Element> {
        val parents = mutableListOf<Element>()
        val matcher = elementsPattern.matcher(string)
        var lastStartIndex = 0

        loop@ while (matcher.find(lastStartIndex)) {
            val startIndex = matcher.start()
            val endIndex = matcher.end()

            // if something is found then everything before - TEXT
            if (lastStartIndex < startIndex) {
                parents.add((Element.Text(string.subSequence(lastStartIndex, startIndex))))
            }

            // found text
            var text: CharSequence

            // groups range for iterate by groups
            val groups = 1..11
            var group = -1
            // цикл чтобы итереироваться по группам
            for (gr in groups) {
                if (matcher.group(gr) != null) {
                    group = gr
                    break
                }
            }

            when (group) {              // 01:03:50
                // NOT FOUND -> BREAK
                -1 -> break@loop

                //UNORDERED LIST
                1 -> {
                    // text without "*. "
                    text = string.subSequence(startIndex.plus(2), endIndex)

                    // find inner elements
                    val subs = findElements(text)
                    val element = Element.UnorderedListItem(text, subs)
                    parents.add(element)

                    // next find start from position "endIndex" (last regex character)
                    lastStartIndex = endIndex

                }

                //HEADER
                2 -> {                  // 01:09
                    val reg = "^#{1,6}".toRegex()
                        .find(string.subSequence(startIndex, string.length))
                    val level = reg!!.value.length

                    // text without "{#} "
                    text = string.subSequence(startIndex.plus(level.inc()), endIndex)
                    val element = Element.Header(level, text)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //QUOTE
                3 -> {                  // 01:11
                    //text without "> "
                    text = string.subSequence(startIndex.plus(2), endIndex)
                    val subelements = findElements(text)
                    val element = Element.Quote(text, subelements)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //ITALIC
                4 -> {                  // 01:13
                    //text without "*{}*"
                    text = string.subSequence(startIndex.inc(), endIndex.dec())
                    val subelements = findElements(text)
                    val element = Element.Italic(text, subelements)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //BOLD
                5 -> {                  // 01:16
                    //text without "**{}**"
                    text = string.subSequence(startIndex.plus(2), endIndex.minus(2))
                    val subelements = findElements(text)
                    val element = Element.Bold(text, subelements)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //STRIKE
                6 -> {
                    //text without "~~{}~~"
                    text = string.subSequence(startIndex.plus(2), endIndex.minus(2))
                    val subelements = findElements(text)
                    val element = Element.Strike(text, subelements)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //RULE
                7 -> {                    // 01:21
                    //text without "***" insert empty character
                    val element = Element.Rule()
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //INLINE_CODE
                8 -> {
                    //text without "`{}`"
                    text = string.subSequence(startIndex.inc(), endIndex.dec())
                    val element = Element.InlineCode(text)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                //LINK
                9 -> {                      // 01:24
                    //full text for regex
                    text = string.subSequence(startIndex, endIndex)
                    val (title: kotlin.String, link: kotlin.String) = "\\[(.*)]\\((.*)\\)".toRegex()
                        .find(text)!!.destructured
                    val element = Element.Link(link, title)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                // 10 -> BLOCK CODE - optionally
                10 -> {
                    text = string.subSequence(startIndex.plus(3), endIndex.minus(3)).toString()
                    val element = Element.BlockCode(text)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                // 11 -> NUMERIC LIST
                11 -> {
                    val reg = "(^\\d{1,2}.)".toRegex().find(string.substring(startIndex, endIndex))
                    val order = reg!!.value
                    text =
                        string.subSequence(startIndex.plus(order.length.inc()), endIndex).toString()

                    val subs = findElements(text)
                    val element = Element.OrderedListItem(order, text.toString(), subs)
                    parents.add(element)
                    lastStartIndex = endIndex
                }

                // 12 -> IMAGE GROUP
                12 -> {
                    text = string.subSequence(startIndex, endIndex)
                    val (alt, url, title) = "^!\\[([^\\[\\]]*?)?]\\((.*?) \"(.*?)\"\\)$".toRegex()
                        .find(text)!!.destructured

                    val element = Element.Image(url, alt, title)
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

sealed class MarkdownElement() {         // 01:40:40
    abstract val offset: Int
    val bounds: Pair<Int, Int> by lazy {    // 01:42:30
        when (this) {
            is Text -> {
                val end = elements.fold(offset) { acc, el ->
                    acc + el.spread().map { it.text.length }.sum()
                }
                offset to end
            }
            is Image -> offset to image.text.length + offset
            is Scroll -> offset to blockCode.text.length + offset
        } // 1:44:27
    }

    data class Text(
        val elements: MutableList<Element>,
        override val offset: Int = 0
    ) : MarkdownElement()

    data class Image(
        val image: Element.Image,
        override val offset: Int = 0
    ) : MarkdownElement()

    data class Scroll(
        val blockCode: Element.BlockCode,
        override val offset: Int = 0
    ) : MarkdownElement()


}

sealed class Element() {
    abstract val text: CharSequence
    abstract val elements: List<Element>

    data class Text(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class UnorderedListItem(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Header(
        val level: Int = 1,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Quote(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Italic(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Bold(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Strike(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Rule(
        override val text: CharSequence = " ", //for insert span
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class InlineCode(
        override val text: CharSequence, //for insert span
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Link(
        val link: String,
        override val text: CharSequence, //for insert span
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class OrderedListItem(
        val order: String,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class BlockCode(
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()

    data class Image(
        val url: String,
        val alt: String?,
        override val text: CharSequence,
        override val elements: List<Element> = emptyList()
    ) : Element()
}


private fun Element.spread(): List<Element> {
    val elements = mutableListOf<Element>()
    if (this.elements.isNotEmpty()) elements.addAll(this.elements.spread())
    else elements.add(this)
    return elements
}

private fun List<Element>.spread(): List<Element> {
    val elements = mutableListOf<Element>()
    forEach { elements.addAll(it.spread()) }
    return elements
}

private fun Element.clearContent(): String {
    return StringBuilder().apply {
        val element = this@clearContent
        if (element.elements.isEmpty()) append(element.text)
        else element.elements.forEach { append((it.clearContent())) }
    }.toString()
}
