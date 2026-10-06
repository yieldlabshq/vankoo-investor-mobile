package com.liquilabs.vankoo.investor.core.platform

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.ui.graphics.toArgb
import com.liquilabs.vankoo.investor.core.designsystem.VankooPalette

/**
 * Shows the URL as a Custom Tab: Stripe's page as a sheet over our own screen.
 *
 * A Custom Tab *is* the browser — same engine, same cookies, same autofill and Link —
 * so the card form still belongs to Stripe and our app never sits between the person
 * and it, which is what rules out a WebView. What changes is the frame: our colour on
 * the toolbar, no address bar, no tabs, and it opens in our task, so when Stripe
 * redirects to `vankoo://` the sheet is what gets closed and the person lands back on
 * the screen they left.
 *
 * That needs the activity on screen; without one, or on a device with no browser
 * that speaks Custom Tabs, it falls back to handing the URL to whatever handles
 * `https`, from the application context and in a task of its own.
 */
class AndroidUrlOpener(
    private val context: Context,
    private val currentActivity: CurrentActivity,
) : UrlOpener {

    override fun open(url: String): Boolean {
        val uri = Uri.parse(url)
        val activity = currentActivity.value
        val customTabsPackage = CustomTabsClient.getPackageName(context, null)

        return if (activity != null && customTabsPackage != null) {
            openAsCustomTab(activity, uri, customTabsPackage)
        } else {
            openInBrowser(uri)
        }
    }

    private fun openAsCustomTab(activity: Context, uri: Uri, browserPackage: String): Boolean = try {
        val colours = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(VankooPalette.navy950.toArgb())
            .build()
        val customTab = CustomTabsIntent.Builder()
            .setDefaultColorSchemeParams(colours)
            // The page's own header already says where the person is.
            .setShowTitle(false)
            .setUrlBarHidingEnabled(true)
            // Nothing here is worth sharing, bookmarking or downloading.
            .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
            .setBookmarksButtonEnabled(false)
            .setDownloadButtonEnabled(false)
            .setStartAnimations(activity, android.R.anim.fade_in, android.R.anim.fade_out)
            .setExitAnimations(activity, android.R.anim.fade_in, android.R.anim.fade_out)
            .build()
        // Pin it to the browser that answered, so a chooser never appears.
        customTab.intent.setPackage(browserPackage)
        customTab.launchUrl(activity, uri)
        true
    } catch (_: ActivityNotFoundException) {
        openInBrowser(uri)
    }

    /** The old way, kept as the floor: any browser, in its own task. */
    private fun openInBrowser(uri: Uri): Boolean = try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}
