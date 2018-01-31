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

## Getting Started: Android

