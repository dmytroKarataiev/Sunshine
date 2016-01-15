Sunshine v2.0
========

This is the learning program written following an excellent Udacity course: [Developing Android Apps: Android Fundamentals](https://www.udacity.com/course/ud853)  and all the improvements from course [Advanced Android App Development](https://www.udacity.com/course/ud855-nd).

## Fundamentals
* RecyclerView, onClick implementation, LayoutManager
* Threading and ASyncTask, JSON parsing
* Intents, PlacePickerIntent (Google PlacePicker API)
* Broadcast Intents and Broadcast Receivers
* Content Provider, SQLite databases and JUnit tests
* Supporting localization and variable screen sizes (Ukrainian language, tablet design)
* Accessibility Features, Custom views (EditText)
* Background services, SyncAdapters
* Notification

## Advanced topics
* Google Cloud Messaging
* Material Design, Shared Transitions, Animations, Parallax Scroll
* Error handling
* Widget ([ce229c](https://github.com/dmytroKarataiev/Sunshine/commit/ce229c652e80eb025337ac248498ec43d2467446))

## Important (API Keys)
You need OpenWeather and Google Maps API keys: 
* OpenWeatherMap Key: <http://openweathermap.org/>
* Google Maps Key (PlacePicker): <https://developers.google.com/places/android-api/placepicker>

OpenWeather key you should add to the gradle.properties file:</br>
MyOpenWeatherMapApiKey = "**YOUR API KEY**"

Google Maps API key should be added to the api_key.xml in values folder as a string with name: youtube_api_key



