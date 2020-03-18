package ru.skillbranch.skillarticles.ui

import java.text.ParsePosition

interface IArticleView {
    /**
     * отрисовать все вхождения поискового запроса в контент (spannable)
     */
    fun renderSearchResult(searchResult: List<Pair<Int, Int>>)

    /**
     * отриосвать текущее пложение поиска и перевести фокус на него (spannable)
     */
    fun renderSearchPosition(searchPosition: Int)

    /**
     * очистить результаты поиска (удалить все spannable)
     */
    fun clearSearchResult()

    /**
     * показать search bar
     */
    fun showSearchBar()

    /**
     * скрыть search bar
     */
    fun hideSearchBar()
}