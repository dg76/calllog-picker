package com.dgunia.calllogpicker

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

object Utils {
    /**
     * Checks if the given intent can be called, i.e. if it exists.
     */
    fun isIntentAvailable(context: Context, intent: Intent): Boolean {
        val packageManager = context.packageManager
        val list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return list != null && list.size > 0
    }
}