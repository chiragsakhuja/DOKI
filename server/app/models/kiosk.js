var mongoose = require('mongoose');
var Schema = mongoose.Schema;

module.exports = mongoose.model('Kiosk', new Schema({
    loc: {
        longitude: Number,
        latitude : Number,
        altitude : Number
    },
    pubkey: String,
    token: String,
    mac: String,
    bluetoothName: String
}));
