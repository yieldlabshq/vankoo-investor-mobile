package com.liquilabs.vankoo.investor.core.platform

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

/** Opens the URL in Safari. Unverified on a device, like the rest of the iOS target. */
class IosUrlOpener : UrlOpener {
    override fun open(url: String): Boolean {
        val nsUrl = NSURL.URLWithString(url) ?: return false
        val application = UIApplication.sharedApplication
        if (!application.canOpenURL(nsUrl)) return false
        application.openURL(nsUrl, options = emptyMap<Any?, Any>(), completionHandler = null)
        return true
    }
}
