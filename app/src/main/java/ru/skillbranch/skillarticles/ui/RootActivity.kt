package ru.skillbranch.skillarticles.ui

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.activity_root.*
import kotlinx.android.synthetic.main.layout_bottombar.*
import kotlinx.android.synthetic.main.layout_submenu.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.viewmodels.ArticleState
import ru.skillbranch.skillarticles.viewmodels.ArticleViewModel
import ru.skillbranch.skillarticles.viewmodels.ViewModelFactory

class RootActivity : AppCompatActivity() {

    private lateinit var viewModel: ArticleViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root)
        setupToolbar()
        setupBottombar()
        setupSubmenu()

        val vmFactory = ViewModelFactory("0")
        viewModel = ViewModelProviders.of(this, vmFactory).get(ArticleViewModel::class.java)
        viewModel.observeState(this) {
            renderUi(it)
        }
    }

    private fun setupSubmenu() {
        btn_text_up.setOnClickListener { viewModel.handleUpText }
        btn_text_down.setOnClickListener { viewModel.handleDownText }
        switch_mode.setOnClickListener { viewModel.handleNightText }

    }

    private fun setupBottombar() {
        btn_like.setOnClickListener { viewModel.handleLike }
        btn_bookmark.setOnClickListener { viewModel.handleBookmark }
        btn_share.setOnClickListener { viewModel.handleShare }
        btn_settings.setOnClickListener { viewModel.handleToggleMenu() }

    }


    private fun renderUi(data: ArticleState) {


    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // проверяем что тулбаер есть и в нём есть лого и настраиваем его
        val logo = if (toolbar.childCount > 2) toolbar.getChildAt(2) as ImageView else null
        logo?.scaleType = ImageView.ScaleType.CENTER_CROP
        val lp = logo?.layoutParams as? Toolbar.LayoutParams
        lp?.let {
            it.width = this.dpToIntPx(4)
            it.height = this.dpToIntPx(4)
            it.marginEnd = this.dpToIntPx(160) // Отступ от вью до тайтла
            logo.layoutParams = it
        }
    }
}
