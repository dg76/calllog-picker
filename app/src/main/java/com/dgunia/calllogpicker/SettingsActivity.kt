package com.dgunia.calllogpicker

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceActivity
import android.preference.SwitchPreference

/**
 * A [PreferenceActivity] that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 *
 * See [Android Design: Settings](http://developer.android.com/design/patterns/settings.html)
 * for design guidelines and the [Settings API Guide](http://developer.android.com/guide/topics/ui/settings.html)
 * for more information on developing a Settings UI.
 */
class SettingsActivity : AppCompatPreferenceActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    companion object {
        val REQUEST_READ_PHONE_STATE = 1
    }
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            "showcurrentcall" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && sharedPreferences.getBoolean(key, false)) {
                    requestPermissions(arrayOf(Manifest.permission.READ_PHONE_STATE), REQUEST_READ_PHONE_STATE)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            (findPreference("showcurrentcall") as SwitchPreference).isChecked = false
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        setupSimplePreferencesScreen()
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences
            .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences
            .unregisterOnSharedPreferenceChangeListener(this)
    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    private fun setupSimplePreferencesScreen() {
        addPreferencesFromResource(R.xml.pref_general)
    }

    /**
     * {@inheritDoc}
     */
    override fun onIsMultiPane(): Boolean = false
}
