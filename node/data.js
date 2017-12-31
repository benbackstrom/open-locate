'use strict';
var fs = require('fs');
var request = require('request');
var auth = require('basic-auth');
var MongoClient = require('mongodb').MongoClient
    , assert = require('assert');
var keys = require('./keys.js');

const REQUIRE_AUTH = true;

module.exports = {

    download: function(uri, filename, callback){
        request.head(uri, function(err, res, body) {
            request(uri).pipe(fs.createWriteStream(filename)).on('close', callback);
        });
    },
    
    // download('https://www.google.com/images/srpr/logo3w.png', 'public/images/google.png', () => {
    //     console.log('done');
    // });

    /*
     * Authenticate the auth credentials and run the success callback
     * function if the credentials match. If no credentials are found,
     * route the client to a page where they can enter a username and password.
     * 
     * This should be run on every route that is received so that we can
     * authenticate all requests before serving content.
     */
    authenticateAndRun: function(req, res, successCallback) {
        if (!REQUIRE_AUTH) {
            successCallback(res);
            return;
        }
    
        module.exports.hasUserPassword((user) => {
            if (user && 
                user.username && user.username.length > 0 &&
                user.password && user.password.length > 0) 
            {
                var credentials = auth(req);
    
                if (!credentials || 
                    credentials.name !== user.username || 
                    credentials.pass !== user.password) {
                    res.statusCode = 401;
                    res.send('Access denied');
                } else {
                    successCallback(res);
                }
            } else {
                // a username/password hasn't been set yet, so set it.
                fs.readFile('public/set_credentials.html', (err, html) => {
                    if (err) {
                        throw err;
                    }
    
                    res.statusCode = 200;
                    res.set('Content-Type', 'text/html');
                    res.send(html);
                });
            }
        });
    },
    
    /*
     * Check if a username and password has been set on this instance
     * of the server. Pass the username and password into the callback
     * if found.
     */
    hasUserPassword: function(callback) {
        MongoClient.connect(keys.MONGO_DB_URL, (err, db) => {
            assert.equal(null, err);
    
            var collection = db.collection('documents');
            collection.find({ user: { $exists: true } }).toArray((err, docs) => {
                assert.equal(null, err);
                if (docs.length > 0 && docs[0].user)
                    callback(docs[0].user);
                else
                    callback(undefined);
            });
    
            db.close();
        });
    },
    
    /*
     * Update username and password on server. To create a username and
     * password for the first time, user writeUserPassword()
     */
    updateUserPassword: function(uname, pword) {
        MongoClient.connect(keys.MONGO_DB_URL, (err, db) => {
            assert.equal(null, err);
    
            var collection = db.collection('documents');
            collection.updateOne(
                { user: { $exists: true } },
                {
                    $set: { username: uname, password: pword }
                } 
            );
    
            db.close();
        });
    },
    
    /*
     * Write an initial username and password to database
     */
    writeUserPassword: function(uname, pword) {
        MongoClient.connect(keys.MONGO_DB_URL, (err, db) => {
            assert.equal(null, err);
    
            var collection = db.collection('documents');
            collection.insertOne(
                {
                    user: { username: uname, password: pword }
                } 
            );
    
            db.close();
        });
    },
    
    /*
     * Save an image file in base64 to a set uri/filename on the server.
     */
    saveImageToDisk: function(uri, imageBase64, callback) {
        if (imageBase64) {
            var imageBuffer = new Buffer(imageBase64, 'base64');
            fs.writeFile(uri, imageBuffer, function(err) {
                assert.equal(err, null);
                callback();
            });
        } else {
            callback();
        }
    },
    
    /*
     * Download and save map image at given lat and lng to the given
     * path/filename.
     */
    requestMapImage: function(lat, lng, uri, callback) {
        var url = keys.GOOGLE_STATIC_MAP_BASE_URL +
            'center='+lat+','+lng+
            '&size=100x100'+
            '&zoom=17'+
            '&format=PNG'+
            '&markers='+lat+','+lng+
            '&key='+keys.GOOGLE_API_KEY
        module.exports.download(url, uri, callback);
    },
    
    /*
     * Insert the points list if it doesn't exist already.
     */
    initializePointsList: function(point, callback) {
        MongoClient.connect(keys.MONGO_DB_URL, (err, db) => {
            assert.equal(null, err);
    
            var collection = db.collection('documents');
            collection.find({ user: { $exists: true } }).toArray((err, docs) => {
                assert.equal(null, err);
    
                if (docs.length <= 0 || !docs[0].user) {
                    collection.updateOne(
                        { user: { $exists: true } },
                        { $set: {
                            points: [] }
                        },
                        (err, result) => {
                            assert.equal(err, null);
    
                            db.close();
                            callback(docs);
                        }
                    );
                } else {
                    db.close();
                    callback(docs);
                }
            });
        });
    },
    
    /*
     * Save all member fields of this point to the database.
     */
    addPointToDatabase: function(point, callback) {
        MongoClient.connect(keys.MONGO_DB_URL, (err, db) => {
            assert.equal(null, err);
    
            var collection = db.collection('documents');
            collection.updateOne(
                { user: { $exists: true } }, 
                { 
                    $push: { 
                        points: {
                            id: point.id,
                            name: point.name,
                            mapUri: point.map,
                            timestamp: point.timestamp,
                            latitude: point.latitude,
                            longitude: point.longitude,
                            notes: point.notes,
                            imageUri: point.image
                        } 
                    } 
                },
                (err, result) => {
                    assert.equal(err, null);
                    
                    db.close();
                    callback();
                }
            );
        });
    },
    
    /*
     * Remove all points from the database.
     */
    removeAllPoints: function(callback) {
        MongoClient.connect(keys.MONGO_DB_URL, (err, db) => {
            assert.equal(null, err);
    
            var collection = db.collection('documents');
            collection.updateOne(
                { points: { $exists: true } }, 
                { $set: { points: [] } },
                (err, result) => {
                    assert.equal(err, null);
    
                    collection.find({ user: { $exists: true } }).toArray((err, docs) => {
                        assert.equal(null, err);
                    
                        db.close();
                        callback(docs);
                    });
                }
            );
        });
    },
    
    /*
     * Remove a point with the given id from the database, then remove 
     * the corresponding image files and run the callback.
     */
    removePointAtId: function(givenId, callback) {
        MongoClient.connect(keys.MONGO_DB_URL, (err, db) => {
            assert.equal(null, err);
    
            var collection = db.collection('documents');
            collection.update(
                { points: { $exists: true} },
                { $pull: { points: { id: givenId } } }
            );
    
            db.close();
    
            module.exports.removeMapAtId(givenId);
            module.exports.removeImageAtId(givenId);
    
            callback();
        });
    },
    
    /*
     * Query list of points and send results to the callback.
     */
    queryPoints: function(callback) {
        MongoClient.connect(keys.MONGO_DB_URL, (err, db) => {
            assert.equal(null, err);
    
            var collection = db.collection('documents');
            collection.find({ points: { $exists: true } }).toArray((err, docs) => {
                callback(docs);
                db.close();
            });
        });
    },
    
    /*
     * Log the current database contents to the console.
     */
    logPoints: function() {
        MongoClient.connect(keys.MONGO_DB_URL, (err, db) => {
            assert.equal(null, err);
    
            var collection = db.collection('documents');
            collection.find().toArray((err, docs) => {
                console.log('---logPoints:');
                console.log(docs);
                db.close();
            });
        });
    },
    
    respondWithPoints: function(req, res) {
        module.exports.queryPoints((docs) => {
            if (docs && docs.length > 0 && docs[0].points) {
                let points = docs[0].points;
                res.send(points);
                // let collector = new PointsCollector(points, 
                //     (result) => {
                //         // Success!
                //         if (!res.headersSent) {
                //             res.send(result);
                //         }
                //     }, 
                //     () => {
                //         // Failure
                //         if (!res.headersSent) {
                //             res.status(500);
                //             res.send();
                //         }
                //     }
                // );
                // for (let i=0; i<points.length; i++) {
                //     let id = points[i].id;
                //     fs.readFile(points[i].mapUri, (error, data) => {
                //         collector.addMapData(id, data);
                //     });
                //     fs.readFile(points[i].imageUri, (error, data) => {
                //         collector.addImageData(id, data);
                //     });
                // }
            } else {
                console.log('---ERROR: cannot find points');
                res.send('{ERROR: cannot find points}');
            }
        });
    },
    
    /*
     * Create and return a path/filename for a map
     */
    createMapFilename: function(id) {
        return 'public/images/a'+id+'.png';
    },
    
    /*
     * Create and return a path/filename for an image
     */
    createImageFilename: function(id) {
        return 'public/images/b'+id+'.jpg';
    },
    
    /*
     * Remove image file with id
     */
    removeImageAtId: function(id) {
        let uri = module.exports.createImageFilename(id);
        fs.unlink(uri, (err) => {
            console.log(err);
        });
    },
    
    /*
     * Remove map file with id
     */
    removeMapAtId: function(id) {
        let uri = module.exports.createMapFilename(id);
        fs.unlink(uri, (err) => {
            console.log(err);
        });
    }

};