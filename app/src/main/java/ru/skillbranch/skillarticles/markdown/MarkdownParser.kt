package ru.skillbranch.skillarticles.markdown

import java.util.regex.Pattern

object MarkdownParser {
    private val LINE_SEPARATOR = System.getProperty("line.separator") ?: "\n"

    // group regex
    private const val UNORDERED_LIST_ITEM = ""

    // result regex
    const val MARKDOWN_GROUPS = "$UNORDERED_LIST_ITEM"

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
    fun clear(string: String): String? {
        return null
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
            val groups = 1..1
            var group = -1
            // цикл чтобы итереироваться по группам
            for (gr in groups) {
                if (matcher.group(gr) != null) {
                    group = gr
                    break
                }
            }

            when (group) {
                // NOT FOUND -> BREAK
                -1 -> break@loop

                //UNORDERED LIST
                1 -> {
                    //text without "*. "
                    text = string.subSequence(startIndex.plus(2), endIndex)

                    // find inner elements
                    val subs = findElements(text)
                    val element = Element.UnorderedListItem(text, subs)
                    parents.add(element)

                    // next find start from position "endIndex" (last regex character)
                    lastStartIndex = endIndex
                    // 01:03:50 - объяснение

                }

                //HEADER
                2 -> {
                    //text without "{#} "
                    //TODO implement me
                }

                //QUOTE
                3 -> {
                    //text without "> "
                    //TODO implement me
                }

                //ITALIC
                4 -> {
                    //text without "*{}*"
                    //TODO implement me
                }

                //BOLD
                5 -> {
                    //text without "**{}**"
                    //TODO implement me
                }

                //STRIKE
                6 -> {
                    //text without "~~{}~~"
                    //TODO implement me
                }

                //RULE
                7 -> {
                    //text without "***" insert empty character
                    //TODO implement me
                }

                //RULE
                8 -> {
                    //text without "`{}`"
                    //TODO implement me
                }

                //LINK
                9 -> {
                    //full text for regex
                    //TODO implement me
                }
                //10 -> BLOCK CODE - optionally
                10 -> {
                    //TODO implement me
                }

                //11 -> NUMERIC LIST
                11 -> {
                    //TODO implement me
                }


            }

        }

        return parents
    }
}


data class MarkdownText(val elements: List<Element>)

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
}
