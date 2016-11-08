const express = require('express')  
const app = express()  
const bodyParser = require('body-parser')
const mongoClient = require('mongodb').MongoClient
const port = 3000
app.use(bodyParser.json())

var db
mongoClient.connect('mongodb://' + process.env.MONGO_USER + ':' + process.env.MONGO_PASS + '@ds017514.mlab.com:17514/doki', (err, database) => {
    if(err) return console.log(err)
    db = database
    db.collection('kiosks').find().toArray(function(err, results) {
        console.log(results)
    })
    app.listen(port, () => {
        console.log('listening on 3000')
    })
})

app.post('/update', (req, res) => {  
    console.log(req.body)
    db.collection('kiosks').save(req.body, (err, result) => {
        if(err) return console.log(err)
        console.log('saved to database')
    })
    res.redirect('/')
})

app.get('/', (req, res) => {
    res.send('success')
})
