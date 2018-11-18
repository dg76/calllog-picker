package com.dgunia.calllogpicker

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

object PermissionUtils {
    /**
     * Checks if all necessary permissions were granted.
     */
    fun hasPermissions(context: Context, vararg permissions: String): Boolean {
        if (Build.VERSION.SDK_INT < 23) return true;

        for(permission in permissions) {
            if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) return false
        }

        return true
    }
}