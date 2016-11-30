// include packages
var express    = require('express');
var app        = express();
var bodyParser = require('body-parser');
var morgan     = require('morgan');
var mongoose   = require('mongoose');
var spdy       = require('spdy');
var path       = require('path');
var fs         = require('fs');
var bcrypt     = require('bcrypt');
var jwt        = require('jsonwebtoken');
var config     = require('./config');
var User       = require('./app/models/user');
var Kiosk      = require('./app/models/kiosk');

// configuration
var port = process.env.PORT || 8080;
mongoose.connect(config.database);
app.set('superSecret', config.secret);

// setup bcrypt
var salt = bcrypt.genSaltSync(10);

// use body parser so we can get info from POST and/or URL parameters
app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());

// use morgan to log requests to the console
app.use(morgan('dev'));

// routes
// basic route
app.get('/', function(req, res) {
    res.send('Hello! The API is at /api');
});

// api routes
var apiRoutes = express.Router();

apiRoutes.post('/authenticate', function(req, res) {
    User.findOne({ name: req.body.user }, function(err, user) {
        if(err) throw err;

        if(! user) {
            res.json({ success: false, message: 'Authentication failed. User not found.' });
        } else {
            bcrypt.compare(req.body.pass, user.password, function(bc_err, bc_res) {
                if(bc_res) {
                    var token = jwt.sign(user, app.get('superSecret'), { expiresIn: '1440m' });
                    res.json({ success: true, message: 'Enjoy your token!', token: token });
                } else {
                    res.json({ success: false, message: 'Authentication failed. Incorrect password.' });
                }
            });
        }
    });
});

apiRoutes.use(function(req, res, next) {
    var token = req.body.token || req.query.token || req.headers['x-access-token'];

    if(token) {
        jwt.verify(token, app.get('superSecret'), function(err, decoded) {
            if(err) {
                return res.json({ success: false, message: 'Failed to authenticate token.' });
            } else {
                req.decoded = decoded;
                next();
            }
        });
    } else {
        res.status(403).send({
            success: false,
            message: 'No token provided.'
        });
    }
});

apiRoutes.get('/', function(req, res) {
    res.json({ success: true });
});


apiRoutes.post('/adduser', function(req, res) {
    var user = new User({
        name: req.body.user,
        password: bcrypt.hashSync(req.body.pass, salt),
        admin: false
    });

    user.save(function(err) {
        if(err) throw err;
        console.log('user: ' + req.body.user + ':' + req.body.pass + ' added successfully.');
        res.json({ success: true });
    });
});

apiRoutes.post('/addkiosk', function(req, res) {
    var kiosk = new Kiosk({
        loc: {
            longitude: req.body.loc.longitude,
            latitude : req.body.loc.latitude,
            altitude : req.body.loc.altitude
        },
        pubkey: req.body.pubkey,
        token: req.body.token,
        mac: req.body.mac,
        bluetoothName: req.body.bluetoothName
    });

    kiosk.save(function(err) {
        if(err) throw err;
        console.log('kiosk (' + req.body.loc_x + ', ' + req.body.loc_y + ' added successfully.');
        res.json({ success: true });
    });
});

apiRoutes.post('/getkiosk', function(req, res) {
    Kiosk.find({}, function(err, kiosks) {
        kiosks.sort(function(a, b) {
            var long_dist_a = req.body.loc.longitude - a.loc.longitude;
            var lat_dist_a = req.body.loc.latitude - a.loc.latitude;
            var distance_a = Math.sqrt(long_dist_a * long_dist_a + lat_dist_a * lat_dist_a);
            var long_dist_b = req.body.loc.longitude - a.loc.longitude;
            var lat_dist_b = req.body.loc.latitude - a.loc.latitude;
            var distance_b = Math.sqrt(long_dist_b * long_dist_b + lat_dist_b * lat_dist_b);
            return distance_a - distance_b;
        });

        res.json({ success: true, kiosk: kiosks[0] });
    });
});

app.use('/api', apiRoutes);

// start the server
app.listen(port);
/*
 *spdy.createServer({ key:  fs.readFileSync('./server.key'), cert: fs.readFileSync('./server.crt') }, app).listen(port, function(err) {
 *    if(err) {
 *        console.log(err);
 *        return process.exit(1);
 *    } else {
 *        console.log('Listening on port: ' + port);
 *    }
 *});
 */
