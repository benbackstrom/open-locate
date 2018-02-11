# open-locate

An Android app that allows you to save memento information to a ready-made Node.js app. You can modify the code for either app for your own purposes.

## Getting Started: Node.js

Following the steps below will get your Node.js server up and running quickly.

### 1. Repository and Node.js

In order to build the Node.js app, you will need to download and install [Node.js and NPM](https://www.npmjs.com/get-npm).

You can ensure they're installed by viewing the version of each:

` npm -v`

` node -v`

Clone the repository to your server. You can discard everything outside the `node` folder if you like.

Run `npm install` to download all NPM dependencies for the project.

In the `node` folder, create a file called `keys.js`. This will hold private data you need to run the server, such as a Google API key. It should look like this to start:

```
module.exports = {
  GOOGLE_STATIC_MAP_BASE_URL: 'https://maps.googleapis.com/maps/api/staticmap?'
}
```

### 2. MongoDB

open-locate uses MongoDB to persist memento data, so you will need to create a MongoDB remote instance and save the URL. There are many options to do this, including [mLab](https://mlab.com/). Add the URL to the file as follows:

```
module.exports = {
  GOOGLE_STATIC_MAP_BASE_URL: 'https://maps.googleapis.com/maps/api/staticmap?',
  MONGO_DB_URL: 'mongodb://...'
}
```

### 3. Google API Key

open-locate uses Google Maps APIs to create map images based on the device's location. In order to use the APIs you will need to generate a key. If you don't have a developer account and key already, you can do so [here](https://developers.google.com/maps/documentation/static-maps/intro). Once you have a key, add it to the `keys.js` file as follows:

```
module.exports = {
  GOOGLE_STATIC_MAP_BASE_URL: 'https://maps.googleapis.com/maps/api/staticmap?',
  MONGO_DB_URL: 'mongodb://...',
  GOOGLE_API_KEY: 'ABC123...`
}
```

### 4. Website's URL

Optionally, you could save your server URL for testing purposes. Add the base URL for the server that will be running open-locate to your `keys.js` file:

```
module.exports = {
  GOOGLE_STATIC_MAP_BASE_URL: 'https://maps.googleapis.com/maps/api/staticmap?',
  MONGO_DB_URL: 'mongodb://...',
  GOOGLE_API_KEY: 'ABC123...',
  NODE_URL: 'https://...'
}
```

### 5. Run it!

Now it's time to test it out. From the same folder as the `index.js` file, run the following command in your terminal or command line:

` node index`

If all is well then you should see the following:

`Express server listening on port 3000 in development mode`

### 6. Register a Username and Password

You will need to set a user in order to access the app remotely. Navigate to the URL of the app + `/register` to enter a username and password. This will only work when no user exists for the app; after that, you will need to go into your remote MongoDB instance and change the username/password.

You're now ready to start saving memento data through the Android app!

## Getting Started: Android

Download OpenLocate for Android from the [Google Play Store](https://play.google.com/store/apps/details?id=com.backstrom.ben.openlocate). Before doing anything, you will need to log into the server successfully.

Tap the Account button in the toolbar, then enter the URL, username, and password for your instance of the Node.js app. Hit Connect. If you see a 'Success!' message then you will be able to start entering data into the app.

When you create a new memento, the following pieces of data are saved to your Node.js app:

1. The image you included
2. A static map image showing where the memento was saved
3. A name for the item
4. Any notes you may have included
5. The latitude/longitude of the device where the memento was saved

Want to fork the project and add your own features? You can build the Android application by downloading [Android Studio and the Android Development SDK](https://developer.android.com/studio/index.html). Then, select File -> Open in Android Studio and point to the `android` directory in the OpenLocate project.

You will need to create an API key for the [Google Maps API](https://developers.google.com/maps/documentation/android-api/signup) in order to . Create a project or choose an existing one, then create a key for that project and save off the key. This should be kept private. Therefore, create a file called `strings_secret` in your Android project in the `res/values` folder. Reference the key in this file as follows:

```
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="maps_api_key">YOUR KEY HERE</string>
</resources>
```

Enjoy!
