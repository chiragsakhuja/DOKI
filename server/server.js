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

apiRoutes.get('/users', function(req, res) {
    User.find({}, function(err, users) {
        res.json(users);
    });
});

app.post('/setup', function(req, res) {
    var user = new User({
        name: req.body.user,
        password: bcrypt.hashSync(req.body.pass, salt),
        admin: true
    });

    user.save(function(err) {
        if(err) throw err;
        console.log(req.body.user + ':' + req.body.pass + ' saved successfully.');
        res.json({ success: true });
    });
});

app.use('/api', apiRoutes);

// start the server
spdy.createServer({ key:  fs.readFileSync('./server.key'), cert: fs.readFileSync('./server.crt') }, app).listen(port, function(error) {
    if(error) {
        console.log(error);
        return process.exit(1);
    } else {
        console.log('Listening on port: ' + port);
    }
});
