package ru.skillbranch.skillarticles.viewmodels

import androidx.lifecycle.LiveData
import ru.skillbranch.skillarticles.data.ArticleData
import ru.skillbranch.skillarticles.data.ArticlePersonalInfo

class ArticleViewModel(articleId: String) : BaseViewModel<ArticleState>(ArticleState()) {

    init {
        // TODO
    }

    fun getArticleContent(): LiveData<List<Any>?> {

    }

    fun getArticleData(): LiveData<ArticleData?> {

    }

    fun getArticlePersonalInfo(): LiveData<ArticlePersonalInfo?> {

    }

    fun handleNightMode() {
    }

    fun handleUpText() {
    }

    fun handleDownText() {
    }

    fun handleBookmark() {
    }

    fun handleShare() {
    }

    fun handleToggleMenu() {
        updateState { it.copy(isShowMenu = !it.isShowMenu) }
    }

    fun handleSearchMode(isSearch: Boolean) {
    }


    fun handleSearch(query: String?) {
    }
}

data class ArticleState(
    val isAuth: Boolean = false, //пользователь авторизован
    val isLoadingContent: Boolean = true, //контент загружается
    val isLoadingReviews: Boolean = true, //отзывы загружаются
    val isLike: Boolean = false, //отмечено как Like
    val isBookmark: Boolean = false, //в закладках
    val isShowMenu: Boolean = false, //отображается меню
    val isBigText: Boolean = false, //шрифт увеличен
    val isDarkMode: Boolean = false, //темный режим
    val isSearch: Boolean = false, //режим поиска
    val searchQuery: String? = null, // поисковы запрос
    val searchResults: List<Pair<Int, Int>> = emptyList(), //результаты поиска (стартовая и конечная позиции)
    val searchPosition: Int = 0, //текущая позиция найденного результата
    val shareLink: String? = null, //ссылка Share
    val title: String? = null, //заголовок статьи
    val category: String? = null, //категория
    val categoryIcon: Any? = null, //иконка категории
    val date: String? = null, //дата публикации
    val author: Any? = null, //автор статьи
    val poster: String? = null, //обложка статьи
    val content: List<Any> = emptyList(), //контент
    val reviews: List<Any> = emptyList() //комментарии
)