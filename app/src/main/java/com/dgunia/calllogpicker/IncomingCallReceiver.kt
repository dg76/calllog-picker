package com.dgunia.calllogpicker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager

/**
 * Gets the number of the currently active call.
 */
class IncomingCallReceiver : BroadcastReceiver() {
    companion object {
        var currentlyActiveCallNumber: String? = null
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let { context ->
            when (intent?.action) {
                TelephonyManager.ACTION_PHONE_STATE_CHANGED -> {
                    val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)

                    when (state) {
                        TelephonyManager.EXTRA_STATE_RINGING -> {
                            context.getSharedPreferences("incomingcallreceiver", Context.MODE_PRIVATE).edit()
                                .putBoolean("ringing", true)
                                .putBoolean("offhook", false)
                                .apply()
                        }
                        TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                            context.getSharedPreferences("incomingcallreceiver", Context.MODE_PRIVATE).edit()
                                .putBoolean("offhook", true)
                                .apply()
                            currentlyActiveCallNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                        }
                        TelephonyManager.EXTRA_STATE_IDLE -> {
                            val prefs = context.getSharedPreferences("incomingcallreceiver", Context.MODE_PRIVATE)
                            val ringing = prefs.getBoolean("ringing", false)
                            val offhook = prefs.getBoolean("offhook", false)
                            currentlyActiveCallNumber = null

                            val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                            if (ringing && !offhook && phoneNumber != null && phoneNumber.isNotBlank()) {
                                onMissedCall(context, phoneNumber)
                            }
                        }
                    }

                    context.sendBroadcast(Intent(BroadcastActions.REFRESH).apply { setPackage(BuildConfig.APPLICATION_ID) })
                }
            }
        }
    }

    private fun onMissedCall(context: Context, phoneNumber: String) {
    }
}