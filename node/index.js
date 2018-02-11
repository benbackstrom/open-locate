'use strict';
var express = require('express');
var app = express();
var fs = require('fs');
var request = require('request');
var bodyParser = require('body-parser');
var keys = require('./keys');
var data = require('./data');

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({
    limit: '10mb',
    extended: true
})); 
app.use(express.static('public'));

class Point {
    constructor(id, name, map, timestamp, latitude, longitude, notes, image) {
        this.id = id;
        this.name = name;
        this.map = map;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.notes = notes;
        this.image = image;
    }
}

/*
 * Tests
 */
app.get('/test-basic-auth', (req, res) => {
    
    // test
    var request = require('request'),
        username = keys.TEST_USERNAME,
        password = keys.TEST_PASSWORD,
        url = keys.NODE_URL,
        authen = "Basic " + new Buffer(username + ":" + password).toString("base64");

    request(
        {
            url : url,
            headers : {
                "Authorization" : authen
            }
        },
        function (error, response, body) {
            console.log(response.body);
        }
    );
    res.send();
});

app.get('/test-static-map', (req, res) => {
    var url = data.GOOGLE_STATIC_MAP_BASE_URL +
        'center=40.714728,-73.998672'+
        '&size=300x300'+
        '&zoom=18'+
        '&format=PNG'+
        '&markers=40.714728,-73.998672'+
        '&key='+data.GOOGLE_API_KEY
    console.log(url);
    res.send();
});

app.get('/test-add-point', (req, res) => {
    let imageData = '/9j/4AAQSkZJRgABAQEAYABgAAD/4QCMRXhpZgAATU0AKgAAAAgABwEaAAUAAAABAAAAYgEbAAUA\
AAABAAAAagEoAAMAAAABAAIAAAExAAIAAAARAAAAclEQAAEAAAABAQAAAFERAAQAAAABAAAAAFES\
AAQAAAABAAAAAAAAAAAAAABgAAAAAQAAAGAAAAABcGFpbnQubmV0IDQuMC4xNgAA/9sAQwACAQEC\
AQECAgICAgICAgMFAwMDAwMGBAQDBQcGBwcHBgcHCAkLCQgICggHBwoNCgoLDAwMDAcJDg8NDA4L\
DAwM/9sAQwECAgIDAwMGAwMGDAgHCAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwM\
DAwMDAwMDAwMDAwMDAwM/8AAEQgAFAAUAwEiAAIRAQMRAf/EAB8AAAEFAQEBAQEBAAAAAAAAAAAB\
AgMEBQYHCAkKC//EALUQAAIBAwMCBAMFBQQEAAABfQECAwAEEQUSITFBBhNRYQcicRQygZGhCCNC\
scEVUtHwJDNicoIJChYXGBkaJSYnKCkqNDU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0\
dXZ3eHl6g4SFhoeIiYqSk5SVlpeYmZqio6Slpqeoqaqys7S1tre4ubrCw8TFxsfIycrS09TV1tfY\
2drh4uPk5ebn6Onq8fLz9PX29/j5+v/EAB8BAAMBAQEBAQEBAQEAAAAAAAABAgMEBQYHCAkKC//E\
ALURAAIBAgQEAwQHBQQEAAECdwABAgMRBAUhMQYSQVEHYXETIjKBCBRCkaGxwQkjM1LwFWJy0QoW\
JDThJfEXGBkaJicoKSo1Njc4OTpDREVGR0hJSlNUVVZXWFlaY2RlZmdoaWpzdHV2d3h5eoKDhIWG\
h4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uLj5OXm5+jp\
6vLz9PX29/j5+v/aAAwDAQACEQMRAD8A/Yb4w+C7b4j+BdZ8K3dteTQ6nFs82BQTburBo5OSBlXR\
WA77a+Jv2PPh3rHw5+JmoXFj4XtdQ8U/2rqGj6dYxXMdisl9DGDe3LO2AxEbhQSct5jf3RX1+f2l\
NGPiPVI7mK+s54Zpl8lLd3ZlUny2yyqF3klVPKEg4chWK8X4jvvBXxI8Pyapp8k+ltb3ltf2GqyX\
FtGst1fOYZSN+8AIUG8lf4coDgFvgeJctxtSopYZOM5NcuycldN8vMmnK1+W8Wk7Nqx+n8I8TLAY\
DEYCvrSnZS3ai7Na2aaUtFJJpuKaTOo8E/Hlba0vLPxJA3h3XNPumtryzkulu9rhVOVkQEEEEd+M\
EehJWL8GdW+HMHheZrWO51SS4uPPubqazSRppWjjJ4hG1MDaCpCnIJI5ySvLwtHOvZR55Qb81d/N\
x5Yt92opX2Vjgxf9kuq5Uqc+V7Wdlt0UlJ27Xk3a12erfFL4VeH/ABz4xZ9W02O88vEZUu6pKo+Y\
LIqkLIoYkgOGCliRgk1z/iD9nTwzrPhe30lIb6xtbe4ecPBdyNM7SbxLukkLMfMEjBzncwOCSOKK\
K/R6c5VMzoubv7Npxvry7/D2+Vj4epJxwNSnHSMtZLo2tm11+ZtfD/4S+HfhZosmnaBpsem2U07X\
DRLK7r5jAAkbmOMhRwMD25NFFFfPL3VaOx6kq1STvKTb9T//2Q==\
'; // image data base64

    let timestamp = Date.now();
    if (!timestamp)
        timestamp = new Date().getTime();
    let imageUri = data.createImageFilename(timestamp);
    let mapUri = data.createMapFilename(timestamp);

    let point = new Point(
        timestamp,
        'Mouse', // req.body.name,
        mapUri, // undefined,
        timestamp, // req.body.timestamp,
        40.714728, // req.body.lat,
        -73.998672, // req.body.lng,
        'This is my mouse', // req.body.notes,
        imageUri // undefined
    );
    
    res.send('OK!');

    data.saveImageToDisk(imageUri, imageData, () => {
        data.requestMapImage(point.latitude, point.longitude, mapUri, () => {
            data.addPointToDatabase(point, () => {
                console.log('---DONE');
            });
        });
    });
});

app.get('/test-remove-all-points', (req, res) => {
    data.removeAllPoints((docs) => {
        res.send('OK!');
    });
});

app.get('/test-log-points', (req, res) => {
    data.logPoints();
    res.send('OK!');
});

app.get('/test-get-points', (req, res) => {
    data.respondWithPoints(req, res);
});


/*
 * Remove a point with a given id in the database.
 */
app.delete('/point/:id', (req, res) => {
    data.authenticateAndRun(req, res, (res) => {
        if (req.params.id) {
            data.removePointAtId(req.params.id, () => {
                res.send('OK!');
            });
        }
    });
});

/*
 * Set a username and password for the first time on this node
 * instance. For security purposes, you will need to manually set
 * the username/password in the database after the first time you
 * set them using this endpoint.
 */
app.get('/register', (req, res) => {
    if (req.query) {
        if (req.query.username && 
            req.query.username.length > 0 &&
            req.query.password &&
            req.query.password.length > 5) 
        {
            var uname = req.query.username;
            var pass = req.query.password;

            data.hasUserPassword((user) => {
                if (user && 
                    user.username && user.username.length > 0 &&
                    user.password && user.password.length > 0) 
                {
                    data.updateUserPassword(uname, pass);
                } else {
                    data.writeUserPassword(uname, pass);
                }
            });
        }
    } 
    res.send();
});

/*
 * Test auth credentials against server. Note that all this does is
 * confirm that the credentials are correct. No login state is saved
 * on the server.
 */
app.get('/login', (req, res) => {
    data.authenticateAndRun(req, res, (res) => {
        res.statusCode = 200;
        res.send('Access granted');
    });
});

/*
 * Add a point to the database.
 */
app.post('/point', (req, res) => {
    data.authenticateAndRun(req, res, (res) => {
        let imageData = req.body.attachment;
        let imageUri = data.createImageFilename(req.body.timestamp);
        let mapUri = data.createMapFilename(req.body.timestamp);

        let point = new Point(
            req.body.timestamp,
            req.body.name,
            mapUri,
            req.body.timestamp,
            req.body.lat,
            req.body.lng,
            req.body.notes,
            imageUri
        );
        
        res.send('OK!');

        data.saveImageToDisk(imageUri, imageData, () => {
            data.requestMapImage(point.latitude, point.longitude, mapUri, () => {
                data.addPointToDatabase(point, () => {
                    
                });
            });
        });
    });
});

/*
 * Get all points in the server's list in json format.
 */
app.get('/points', (req, res) => {
    data.authenticateAndRun(req, res, (res) => {
        data.respondWithPoints(req, res);
    });
});

/*
 * Test authentication with a username and password
 */
app.get('/', (req, res) => {
    data.authenticateAndRun(req, res, (res) => {
        res.statusCode = 200;
        res.send('Access granted');
    });
});

let port = process.env.PORT || 3000;
app.listen(port, () => {
    let images = './public/images';
    if (!fs.existsSync(images))
        fs.mkdirSync(images);

    console.log("Express server listening on port %d in %s mode", 
        port, app.settings.env);
});