package app.simple.inure.extensions.activities

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.transition.Fade
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.os.ConfigurationCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import app.simple.inure.R
import app.simple.inure.decorations.transitions.compat.DetailsTransitionArc
import app.simple.inure.dialogs.miscellaneous.Error
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.preferences.SharedPreferences
import app.simple.inure.preferences.ShellPreferences.getHomePath
import app.simple.inure.preferences.ShellPreferences.setHomePath
import app.simple.inure.themes.data.MaterialYou
import app.simple.inure.themes.data.MaterialYou.presetMaterialYouDynamicColors
import app.simple.inure.themes.interfaces.ThemeChangedListener
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.util.ContextUtils
import app.simple.inure.util.LocaleHelper
import app.simple.inure.util.ThemeUtils
import app.simple.inure.util.ThemeUtils.setTheme

@SuppressLint("Registered")
open class BaseActivity : AppCompatActivity(), ThemeChangedListener, android.content.SharedPreferences.OnSharedPreferenceChangeListener {

    override fun attachBaseContext(newBaseContext: Context) {
        SharedPreferences.init(newBaseContext)
        SharedPreferences.getSharedPreferences().registerOnSharedPreferenceChangeListener(this)
        super.attachBaseContext(ContextUtils.updateLocale(newBaseContext, ConfigurationPreferences.getAppLanguage()!!))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            presetMaterialYouDynamicColors()

            if (AppearancePreferences.isMaterialYouAccent()) {
                AppearancePreferences.setAccentColor(ContextCompat.getColor(baseContext, MaterialYou.materialYouAccentResID))
            }
        }

        ThemeUtils.setAppTheme(resources)

        with(window) {
            requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)
            sharedElementEnterTransition = DetailsTransitionArc()
            sharedElementReturnTransition = DetailsTransitionArc()
            enterTransition = Fade()
            exitTransition = Fade()
            setBackgroundDrawable(ColorDrawable(ThemeManager.theme.viewGroupTheme.background))
        }

        setTheme()
        setContentView(R.layout.activity_main)

        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                                   .detectLeakedClosableObjects()
                                   .penaltyLog()
                                   .build())

        /**
         * Sets window flags for keeping the screen on
         */
        if (ConfigurationPreferences.isKeepScreenOn()) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        if (!AppearancePreferences.isTransparentStatusDisabled()) {
            makeAppFullScreen()
            fixNavigationBarOverlap()
        }

        /**
         * Keeps the instance of current locale of the app
         */
        LocaleHelper.setAppLocale(ConfigurationCompat.getLocales(resources.configuration)[0]!!)

        ThemeUtils.setBarColors(resources, window)
        setNavColor()

        // Terminal home path
        val defValue = getDir("HOME", MODE_PRIVATE).absolutePath
        val homePath = getHomePath(defValue)
        setHomePath(homePath!!)
    }

    private fun makeAppFullScreen() {
        if (AppearancePreferences.isTransparentStatusDisabled()) {
            window.statusBarColor = ThemeManager.theme.viewGroupTheme.background
            WindowCompat.setDecorFitsSystemWindows(window, true)
        } else {
            window.statusBarColor = Color.TRANSPARENT
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.navigationBarDividerColor = Color.TRANSPARENT
        }
    }

    /**
     * Making the Navigation system bar not overlapping with the activity
     */
    private fun fixNavigationBarOverlap() {
        /**
         * Root ViewGroup of this activity
         */
        val root = findViewById<CoordinatorLayout>(R.id.app_container)

        if (AppearancePreferences.isTransparentStatusDisabled()) {
            root.layoutParams = (root.layoutParams as FrameLayout.LayoutParams).apply {
                leftMargin = 0
                bottomMargin = 0
                rightMargin = 0
            }

            root.requestLayout()
        } else {
            ViewCompat.setOnApplyWindowInsetsListener(root) { view, windowInsets ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

                /**
                 * Apply the insets as a margin to the view. Here the system is setting
                 * only the bottom, left, and right dimensions, but apply whichever insets are
                 * appropriate to your layout. You can also update the view padding
                 * if that's more appropriate.
                 */
                view.layoutParams = (view.layoutParams as FrameLayout.LayoutParams).apply {
                    leftMargin = insets.left
                    bottomMargin = insets.bottom
                    rightMargin = insets.right
                }

                /**
                 * Return CONSUMED if you don't want want the window insets to keep being
                 * passed down to descendant views.
                 */
                WindowInsetsCompat.CONSUMED
            }
        }
    }

    private fun setNavColor() {
        if (AppearancePreferences.isAccentOnNavigationBar()) {
            window.navigationBarColor = AppearancePreferences.getAccentColor()
        } else {
            window.navigationBarColor = Color.TRANSPARENT
        }

        ThemeUtils.updateNavAndStatusColors(resources, window)
    }

    protected fun showError(error: String) {
        try {
            val e = Error.newInstance(error)
            e.show(supportFragmentManager, "error_dialog")
            e.setOnErrorDialogCallbackListener(object : Error.Companion.ErrorDialogCallbacks {
                override fun onDismiss() {
                    onBackPressedDispatcher.onBackPressed()
                }
            })
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: android.content.SharedPreferences?, key: String?) {
        when (key) {
            AppearancePreferences.transparentStatus -> {
                makeAppFullScreen()
                fixNavigationBarOverlap()
            }
            AppearancePreferences.accentColor,
            AppearancePreferences.accentOnNav -> {
                setNavColor()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        SharedPreferences.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this)
    }
}