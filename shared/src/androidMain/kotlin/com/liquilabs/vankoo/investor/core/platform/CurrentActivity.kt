package com.liquilabs.vankoo.investor.core.platform

import android.app.Activity

/**
 * The activity that is on screen right now, for the one thing that needs it.
 *
 * A Custom Tab has to be launched from an Activity to land in the same task as the
 * app — launched from the Application it would need NEW_TASK and open as a separate
 * task, which is the browser-hopping we are trying to avoid. Koin only knows the
 * Application, so VankooAppHost fills this in while its content is composed and
 * clears it when it leaves, and the opener reads whatever is there at that moment.
 */
class CurrentActivity {
    @Volatile
    var value: Activity? = null
}
