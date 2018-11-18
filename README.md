# calllog-picker
This is an Android app to let the user pick an entry from his call log.

When it is started directly you can tap on an entry to either
 
1. create an event from it (using one of your installed calendar apps)
2. or to call that number.

When it is started by another app using a 

```startActivityForResult(new Intent(Intent.ACTION_PICK, CallLog.CONTENT_URI), REQUEST_ID)```

call, then it returns the entry that the user has selected. It returns the extras

- name - the name of the contact
- number - the phone number
- date - the date and time of the call
- duration - the duration of the call
- type - the type of the call

This is a very basic app. The same functionality could be included in all phone and call log apps but unfortunately
I haven't found any that supports the general ACTION_PICK request and I also did not find a way to search the store
for apps that support a certain Intent.

<a href="https://github.com/dg76/calllog-picker/releases/download/v1.0/calllog-picker.apk" style="style="background-color: #CF0001; padding: 7px; padding-left: 12px; padding-right: 12px; color: white; border-radius: 5px; text-decoration: none; font-family: sans-serif; box-shadow: 1px 1px 3px gray;">Download Calllog-Picker</a>