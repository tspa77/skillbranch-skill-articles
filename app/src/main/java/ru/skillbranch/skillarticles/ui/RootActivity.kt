package ru.skillbranch.skillarticles.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_root.*
import kotlinx.android.synthetic.main.layout_bottombar.*
import ru.skillbranch.skillarticles.viewmodels.base.Notify
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.selectDestination
import ru.skillbranch.skillarticles.ui.base.BaseActivity
import ru.skillbranch.skillarticles.viewmodels.RootViewModel
import ru.skillbranch.skillarticles.viewmodels.base.IViewModelState
import ru.skillbranch.skillarticles.viewmodels.base.NavigationCommand

class RootActivity : BaseActivity<RootViewModel>() {

    override val layout: Int = R.layout.activity_root
    public override val viewModel:RootViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //top level destination
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_articles,
                R.id.nav_bookmarks,
                R.id.nav_transcriptions,
                R.id.nav_profile
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        //nav_view.setupWithNavController(navController)

        nav_view.setOnNavigationItemSelectedListener {
            // if clicked on bottom navigation item -> navigate to destination by item id
            viewModel.navigate(NavigationCommand.To(it.itemId))
            true
        }

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            if (destination.id == R.id.nav_profile && controller.currentDestination?.id == R.id.auth) {
                val authEntry = controller.getBackStackEntry(R.id.auth)

                controller.popBackStack(R.id.nav_profile, true)
            }
            // if destination changes set selected bottom navigation item
            nav_view.selectDestination(destination)
        }
    }

    override fun renderNotification(notify: Notify) {
        val snackbar = Snackbar.make(container,notify.message, Snackbar.LENGTH_LONG)

        if(bottombar != null) snackbar.anchorView = bottombar
        else snackbar.anchorView = nav_view

        when(notify) {
            is Notify.TextMessage -> { /* nothing */}
            is Notify.ActionMessage -> {
                snackbar.setActionTextColor(getColor(R.color.color_accent_dark))
                snackbar.setAction(notify.actionLabel) {
                    notify.actionHandler.invoke()
                }
            }
            is Notify.ErrorMessage -> {
                with(snackbar) {
                    setBackgroundTint(getColor(R.color.design_default_color_error))
                    setTextColor(getColor(android.R.color.white))
                    setActionTextColor(getColor(android.R.color.white))
                    setAction(notify.errLabel){
                        notify.errHandler?.invoke()
                    }
                }
            }
        }

        snackbar.show()
    }

    override fun subscribeOnState(state: IViewModelState) {
        // Do something with state
    }

}
