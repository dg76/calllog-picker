package com.dgunia.calllogpicker

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.BaseColumns
import android.provider.CalendarContract
import android.provider.CallLog
import android.provider.ContactsContract
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import android.widget.Toast
import com.dgunia.calllogpicker.list.CallArrowDrawable
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.listviewcell.view.*
import java.text.DateFormat
import java.util.*
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private val REQUEST_CALLLOG: Int = 1
    val executor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions(arrayOf(Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_CONTACTS), REQUEST_CALLLOG)
        } else {
            buildList()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CALLLOG) {
            buildList()
        }
    }

    private val broadcastReceiverRefresh = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            buildList()
        }
    }

    override fun onResume() {
        super.onResume()

        buildList()

        registerReceiver(broadcastReceiverRefresh, IntentFilter(BroadcastActions.REFRESH))
    }

    override fun onPause() {
        super.onPause()

        unregisterReceiver(broadcastReceiverRefresh)
    }

    class Call(var name: String?, val number: String?, val date: Date?, val duration: Long, val type: Int)

    private fun buildList() {
        val calls = ArrayList<Call>()
        if (Build.VERSION.SDK_INT < 23 || checkSelfPermission(Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
            IncomingCallReceiver.currentlyActiveCallNumber?.let { phoneNumber ->
                calls.add(Call(null, phoneNumber, Date(), 0, 0))
            }

            var cursor: Cursor? = null
            try {
                cursor = contentResolver.query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE + " DESC")
                while (cursor != null && cursor.moveToNext() && calls.size < 20) {
                    val name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME))
                    val number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER))
                    val date = Date(cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)))
                    val duration = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION))
                    val type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE))

                    calls.add(Call(name, number, date, duration, type))
                }
            } finally {
                cursor?.close()
            }
        } else if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder(this)
                .setMessage(getString(R.string.cannotaccesscalllog))
                .show()
        }

        if (calls.size == 0) {
            Toast.makeText(this, R.string.nolastcall, Toast.LENGTH_LONG).show()
        } else {
            listView.onItemClickListener = object : AdapterView.OnItemClickListener {
                override fun onItemClick(adapterView: AdapterView<*>, view: View, pos: Int, l: Long) {
                    val selectedCall = calls[pos]
                    if (intent.action == Intent.ACTION_PICK) {
                        setResult(Activity.RESULT_OK, Intent().apply {
                            putExtra("name", selectedCall.name)
                            putExtra("number", selectedCall.number)
                            putExtra("date", selectedCall.date?.time ?: Date().time)
                            putExtra("duration", selectedCall.duration)
                            putExtra("type", selectedCall.type)
                        })
                        finish()
                    } else {
                        AlertDialog.Builder(this@MainActivity)
                            .setItems(arrayOf(getString(R.string.createevent), getString(R.string.callnumber)), { dialog, which ->
                                when (which) {
                                    0 -> onCreateEvent(selectedCall)
                                    else -> onCall(selectedCall)
                                }
                            })
                            .show()
                    }
                }

                fun onNothingSelected(adapterView: AdapterView<*>) {}
            }

            val dfDate = DateFormat.getDateInstance(DateFormat.SHORT)
            val dfTime = DateFormat.getTimeInstance(DateFormat.SHORT)

            listView.adapter = object : ArrayAdapter<Call>(this, R.layout.listviewcell, calls), ListAdapter {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                    val view = convertView ?: layoutInflater.inflate(R.layout.listviewcell, null)

                    getItem(position)?.let { call ->
                        view.name.text = call.name ?: call.number
                        view.date.text = dfDate.format(call.date)
                        view.time.text = dfTime.format(call.date)
                        view.imageView.setImageDrawable(CallArrowDrawable(call.type != CallLog.Calls.OUTGOING_TYPE, call.type == CallLog.Calls.MISSED_TYPE))
                        view.duration.text = "${call.duration / 60} ${view.context.getString(R.string.minutes)}"

                        if (call.name == null) {
                            call.number?.let { number ->
                                if (Build.VERSION.SDK_INT < 23 || checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                                    // Nachladen des Namens, wenn er in der Telefonliste nicht drin stand.
                                    executor.submit {
                                        val contacts = loadContactsByPhoneNumberQuery(layoutInflater.context.contentResolver, number)
                                        if (contacts.size > 0) {
                                            call.name = contacts.get(0).displayName // Damit der Name nicht nochmal geladen wird.
                                            view.name.text = call.name
                                        }
                                    }
                                }
                            }
                        }
                    }

                    return view
                }
            }
        }
    }

    private fun onCall(selectedCall: Call) {
        val intentCall = Intent(Intent.ACTION_VIEW, Uri.parse("tel://${selectedCall.number}"))
        if (Utils.isIntentAvailable(applicationContext, intentCall)) {
            startActivity(intentCall)
        } else {
            AlertDialog.Builder(this).setMessage(R.string.notelephoneapp).setNegativeButton(android.R.string.cancel, null).show()
        }
    }

    private fun onCreateEvent(selectedCall: Call) {
        val intentCreateEvent = Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI).apply {
            putExtra(CalendarContract.Events.TITLE, selectedCall.name)
            putExtra(CalendarContract.Events.DESCRIPTION, selectedCall.number)
        }
        if (Utils.isIntentAvailable(applicationContext, intentCreateEvent)) {
            startActivity(intentCreateEvent)
        } else {
            AlertDialog.Builder(this).setMessage(R.string.nocalendarapp).setNegativeButton(android.R.string.cancel, null).show()
        }
    }

    fun loadContactsByPhoneNumberQuery(cr: ContentResolver, phonenumber: String): List<Contact> {
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phonenumber))
        var name = "?"
        val phoneNumbers = ArrayList<Contact>()

        val contactLookup =
            cr.query(uri, arrayOf(BaseColumns._ID, ContactsContract.PhoneLookup.DISPLAY_NAME), null, null, null)

        try {
            if (contactLookup != null && contactLookup.count > 0) {
                contactLookup.moveToNext()
                name = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME))
                val contactId = contactLookup.getString(contactLookup.getColumnIndex(BaseColumns._ID))
                val contact = loadContactStructuredNameByID(cr, contactId)
                contact.displayName = name

                phoneNumbers.add(contact)
            }
        } finally {
            contactLookup?.close()
        }

        return phoneNumbers
    }

    fun loadContactStructuredNameByID(cr: ContentResolver, contactID: String): Contact {
        val result = Contact(contactID, "")
        // projection
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
            ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
            ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME
        )

        val where = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?"
        val whereParameters = arrayOf(contactID, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)

        //Request
        val cursor = cr.query(ContactsContract.Data.CONTENT_URI, projection, where, whereParameters, null)

        if (cursor != null) {
            //Iteration
            if (cursor.moveToFirst()) {
                result.displayName =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME))
                result.givenName =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME))
                result.middleName =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME))
                result.familyName =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME))
            }
            cursor.close()
        }
        return result
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menusettings -> {
                onSettings()
                return true
            }
            R.id.menuprivacypolicy -> {
                onPrivacyPolicy()
                return true
            }
            R.id.menuabout -> {
                onAbout()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onPrivacyPolicy() {
        AlertDialog.Builder(this).setMessage(getString(R.string.privacypolicytext)).setPositiveButton(android.R.string.ok, null).show()
    }

    private fun onSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    private fun onAbout() {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/dg76/calllog-picker")))
    }
}
