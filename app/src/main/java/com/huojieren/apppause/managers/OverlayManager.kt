package com.huojieren.apppause.managers

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import com.huojieren.apppause.data.repository.LogRepository.Companion.logger
import com.huojieren.apppause.ui.FloatingWindowLifecycleOwner
import com.huojieren.apppause.ui.theme.AppTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class OverlayManager(
    private val context: Context
) {
    companion object {
        private const val FADE_IN_DURATION_FAST = 300L
        private const val FADE_IN_DURATION_SLOW = 2000L
        private const val FADE_OUT_DURATION = 200L
    }

    private val tag = "OverlayManager"
    private var lifecycleOwner: FloatingWindowLifecycleOwner? = null
    private var windowManager: WindowManager? = null
    private var composeView: ComposeView? = null

    private val _fadeInCompleteEvent = MutableSharedFlow<Unit>(replay = 1)
    val fadeInCompleteEvent: SharedFlow<Unit> = _fadeInCompleteEvent.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun showOverlay(
        isSlowFadeIn: Boolean = false,
        content: @Composable () -> Unit
    ) {
        if (composeView != null) {
            logger(tag, "showOverlay: already showing, skip")
            return
        }

        logger(tag, "showOverlay: reset replay cache")
        _fadeInCompleteEvent.resetReplayCache()

        val statusBarHeight = getStatusBarHeight()
        val screenHeight = getScreenHeight()
        val duration = if (isSlowFadeIn) FADE_IN_DURATION_SLOW else FADE_IN_DURATION_FAST

        // 检测系统深色模式
        val isDarkTheme = (context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        logger(
            tag,
            "showOverlay: starting, isSlowFadeIn=$isSlowFadeIn, duration=${duration}ms, isDarkTheme=$isDarkTheme"
        )

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            screenHeight + statusBarHeight,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.OPAQUE
        ).apply {
            gravity = Gravity.CENTER
            alpha = 0f
        }

        lifecycleOwner = FloatingWindowLifecycleOwner().also {
            it.initialize()
        }

        composeView = ComposeView(context).apply {
            setContent {
                AppTheme(darkTheme = isDarkTheme) {
                    content()
                }
            }
        }

        composeView?.let { view ->
            lifecycleOwner?.attachToComposeView(view)
        }

        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager?.addView(composeView, layoutParams)

        fadeIn(layoutParams, isSlowFadeIn)
    }

    private fun fadeIn(
        layoutParams: WindowManager.LayoutParams,
        isSlowFadeIn: Boolean
    ) {
        val duration = if (isSlowFadeIn) FADE_IN_DURATION_SLOW else FADE_IN_DURATION_FAST
        ValueAnimator.ofFloat(0f, 1f).apply {
            this.duration = duration
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                layoutParams.alpha = animator.animatedValue as Float
                windowManager?.updateViewLayout(composeView, layoutParams)
            }
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: android.animation.Animator) {
                    logger(tag, "fadeIn: started, duration=${duration}ms")
                }

                override fun onAnimationEnd(animation: android.animation.Animator) {
                    logger(tag, "fadeIn: completed, emitting fadeInCompleteEvent")
                    _fadeInCompleteEvent.tryEmit(Unit)
                }
            })
            start()
        }
    }

    fun removeOverlay() {
        if (composeView == null) {
            logger(tag, "removeOverlay: not showing, skip")
            return
        }

        logger(tag, "removeOverlay: starting fade out")
        val view = composeView ?: return
        val params = view.layoutParams as? WindowManager.LayoutParams ?: return

        ValueAnimator.ofFloat(1f, 0f).apply {
            duration = FADE_OUT_DURATION
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                params.alpha = animator.animatedValue as Float
                windowManager?.updateViewLayout(view, params)
            }
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    logger(tag, "fadeOut: completed, cleaning up")
                    cleanup()
                }
            })
            start()
        }
    }

    private fun cleanup() {
        composeView?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                // View 可能已经被移除
                logger(tag, "Error removing overlay: ${e.message}", Log.ERROR, e)
            }
            composeView = null
        }
        lifecycleOwner?.destroy()
        lifecycleOwner = null
    }

    private fun getStatusBarHeight(): Int {
        val resources = context.resources
        return resources.getDimensionPixelSize(
            resources.getIdentifier("status_bar_height", "dimen", "android")
        )
    }

    private fun getScreenHeight(): Int {
        val displayMetrics = context.resources.displayMetrics
        return displayMetrics.heightPixels
    }
}