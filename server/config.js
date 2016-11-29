module.exports = {
    'secret': process.env.WEB_TOKEN_SECRET,
    'database': 'mongodb://' + process.env.MONGO_USER + ':' + process.env.MONGO_PASS + '@ds017514.mlab.com:17514/doki'
};
