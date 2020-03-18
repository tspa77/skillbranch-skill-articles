package ru.skillbranch.skillarticles.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_root.*
import kotlinx.android.synthetic.main.layout_bottombar.*
import kotlinx.android.synthetic.main.layout_submenu.*
import kotlinx.android.synthetic.main.search_view_layout.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.ui.base.BaseActivity
import ru.skillbranch.skillarticles.viewmodels.ArticleState
import ru.skillbranch.skillarticles.viewmodels.ArticleViewModel
import ru.skillbranch.skillarticles.viewmodels.base.Notify
import ru.skillbranch.skillarticles.viewmodels.base.ViewModelFactory

class RootActivity : BaseActivity<ArticleViewModel>() {
    override val layout: Int = R.layout.activity_root
    override lateinit var viewModel: ArticleViewModel

    private var searchQuery: String? = null
    private var isSearching = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_root)

        val vmFactory = ViewModelFactory("0")
        viewModel = ViewModelProviders.of(this, vmFactory).get(ArticleViewModel::class.java)
        viewModel.observeState(this) { renderUi(it) }
        viewModel.observeNotifications(this) { renderNotification(it) }
    }

    override fun setupViews() {
        setupToolbar()
        setupBottombar()
        setupSubmenu()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        val menuItem = menu?.findItem(R.id.action_search)
        val searchView = (menuItem?.actionView as? SearchView)
        searchView?.queryHint = getString(R.string.article_search_placeholder)

        //restore SearchView
        if (isSearching) {
            menuItem?.expandActionView()
            searchView?.setQuery(searchQuery, false)
            searchView?.clearFocus()
        }

        menuItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                viewModel.handleSearchMode(true)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                viewModel.handleSearchMode(false)
                return true
            }
        })
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.handleSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.handleSearch(newText)
                return true
            }
        })

        return super.onCreateOptionsMenu(menu)


//        // https://stackoverflow.com/questions/55537368/filter-for-searchview-in-kotlin
//        with(menu!!.findItem(R.id.action_search)) {
//            setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
//                override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
//                    viewModel.handleSearchMode(true)
//                    hideLogo()
//                    return true
//                }
//
//                override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
//                    viewModel.handleSearchMode(false)
//                    showLogo()
//                    return true
//                }
//            })
//            if (viewModel.currentState.isSearch) expandActionView() // Если был режим поиска - восстановим
//        }
//
//        with(menu.findItem(R.id.action_search).actionView as SearchView) {
//            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//                override fun onQueryTextSubmit(query: String?): Boolean {
//                    viewModel.handleSearch(query)
//                    return true
//                }
//
//                override fun onQueryTextChange(newText: String?): Boolean {
//                    viewModel.handleSearch(newText)
//                    return true
//                }
//            })
//            maxWidth = 9999
//            if (viewModel.currentState.isSearch) setQuery(
//                viewModel.currentState.searchQuery,
//                false
//            ) // Если был режим поиска - восстановим
//        }
//
//
//        return super.onCreateOptionsMenu(menu)
    }

    private fun renderNotification(notify: Notify) {
        val snackbar = Snackbar.make(coordinator_container, notify.message, Snackbar.LENGTH_LONG)
            .setAnchorView(bottombar)
            .setActionTextColor(getColor(R.color.color_accent_dark))

        when (notify) {
            is Notify.TextMessage -> { /* nothing */
            }
            is Notify.ActionMessage -> {
                snackbar.setActionTextColor(getColor(android.R.color.white))
                snackbar.setAction(notify.actionLabel) {
                    notify.actionHandler?.invoke()
                }
            }
            is Notify.ErrorMessage -> {
                with(snackbar) {
                    setBackgroundTint(getColor(R.color.design_default_color_error))
                    setTextColor(getColor(android.R.color.white))
                    setActionTextColor(getColor(android.R.color.white))
                    setAction(notify.errLabel) {
                        notify.errHandler?.invoke()
                    }
                }
            }
        }

        snackbar.show()
    }

    private fun setupSubmenu() {
        btn_text_up.setOnClickListener { viewModel.handleUpText() }
        btn_text_down.setOnClickListener { viewModel.handleDownText() }
        switch_mode.setOnClickListener { viewModel.handleNightMode() }
    }

    private fun setupBottombar() {
        btn_like.setOnClickListener { viewModel.handleLike() }
        btn_bookmark.setOnClickListener { viewModel.handleBookmark() }
        btn_share.setOnClickListener { viewModel.handleShare() }
        btn_settings.setOnClickListener { viewModel.handleToggleMenu() }

        btn_result_up.setOnClickListener {
            if (search_view.hasFocus()) search_view.clearFocus()
            viewModel.handleUpResult()
        }

        btn_result_down.setOnClickListener {
            if (search_view.hasFocus()) search_view.clearFocus()
            viewModel.handleDownResult()
        }

        btn_search_close.setOnClickListener {
            viewModel.handleSearchMode(false)
            invalidateOptionsMenu()
        }
    }


    private fun renderUi(data: ArticleState) {

        bottombar.setSearchState(data.isSearch)

        //bind submenu state
        btn_settings.isChecked = data.isShowMenu
        if (data.isShowMenu) submenu.open() else submenu.close()

        //bind article person data
        btn_like.isChecked = data.isLike
        btn_bookmark.isChecked = data.isBookmark

        //bind submenu views
        switch_mode.isChecked = data.isDarkMode
        delegate.localNightMode =
            if (data.isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO

        if (data.isBigText) {
            tv_text_content.textSize = 18f
            btn_text_up.isChecked = true
            btn_text_down.isChecked = false
        } else {
            tv_text_content.textSize = 14f
            btn_text_up.isChecked = false
            btn_text_down.isChecked = true
        }

        //bind content
        tv_text_content.text =
            if (data.isLoadingContent) "loading" else data.content.first() as String

        //bind toolbar
        toolbar.title = data.title ?: "loading"
        toolbar.subtitle = data.category ?: "loading"
        if (data.categoryIcon != null) toolbar.logo = getDrawable(data.categoryIcon as Int)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // проверяем что тулбаер есть и в нём есть лого и настраиваем его
        val logo = getLogo()
        logo?.scaleType = ImageView.ScaleType.CENTER_CROP
        val lp = logo?.layoutParams as? Toolbar.LayoutParams
        lp?.let {
            it.width = this.dpToIntPx(40)
            it.height = this.dpToIntPx(40)
            it.marginEnd = this.dpToIntPx(16) // Отступ от вью до тайтла
            logo.layoutParams = it
        }
    }

    private fun getLogo(): ImageView? {
        return if (toolbar.childCount > 2) toolbar.getChildAt(2) as ImageView else null
    }

    private fun showLogo() {
        getLogo()?.visibility = VISIBLE
    }

    private fun hideLogo() {
        getLogo()?.visibility = GONE
    }


}
