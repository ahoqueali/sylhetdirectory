
const express = require('express')
const router = express.Router();
const path = require('path');
const app = express()
const port = 8080

app.use(express.static('./docs'))

app.get('/listing', function(req, res) {
    res.sendFile(path.join(__dirname + '/listingTemplate.html'));
});

app.get('/', function(req, res) {
    res.sendFile(path.join(__dirname + '/index.html'));
});

app.get('*', function(req, res) {
    res.sendFile(path.join(__dirname + req.originalUrl + "/index.html"));
});



app.listen(port, () => console.log(`Example app listening on port ${port}!`))
