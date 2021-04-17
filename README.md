# ToDo
Todo app with reminders that implements new/less-new Kotlin/Android features such as:  
* View binding
* Room database
* Dagger Hilt
* Data storage
* Coroutine Flow
* Navigation components
* mvvm architecture

Android make life hard to people who want to use AlarmManager with exact time due to the doze mode added with Android 6.0 (API level 23)
that it group alarms  and fire them togather to reduce the amount of wake ups.<br/>
After a lot of reaserch the only solution I found was to add the app to the not optimazed whitelist.<br/>
To do so when an alarm is set, a dialog show up sending the user to the battery settings (if it wasn't already whitelisted).
