# calllog-picker
This is an Android app to let the user pick an entry from his call log.

When it is started directly you can tap on an entry to either
 
1. create an event from it 
2. or to call that number.

When it is started by another app using a `startActivityForResult(new Intent(Intent.ACTION_PICK, CallLog.CONTENT_URI), REQUEST_ID)` call, then it returns the entry that the user has selected. It returns the extras

- name - the name of the contact
- number - the phone number
- date - the date and time of the call
- duration - the duration of the call
- type - the type of the call

This is a very basic app. The same functionality could be included in all phone and call log apps but unfortunately
I haven't found any that supports the general ACTION_PICK request and I also did not find a way to search the store
for apps that support a certain Intent.

