const express = require('express');
const mysql = require('mysql');
const multer = require('multer');
const path = require('path');

const app = express();
const PORT = process.env.PORT || 3000;

// MySQL connection
const connection = mysql.createConnection({
  host: 'localhost',
  user: 'root',
  password: '',
  port: 3306,
  database: 'vector_sense_species'
});

connection.connect((err) => {
  if (err) throw err;
  console.log('Connected to MySQL database');
});

// Multer setup for handling file uploads
const storage = multer.diskStorage({
  destination: function(req, file, cb) {
    cb(null, 'uploads/');
  },
  filename: function(req, file, cb) {
    cb(null, file.originalname);
  }
});

const upload = multer({ storage: storage });

// API endpoint for uploading image with longitude and latitude
app.post('/upload', upload.single('image_path'), (req, res) => {
  const { longitude, latitude } = req.body;
  const imagePath = req.file.path;

//   Save the image path along with longitude and latitude to MySQL
  const insertQuery = 'INSERT INTO image_colletion (longitude, latitude, image_path) VALUES (?, ?, ?)';
  connection.query(insertQuery, [longitude, latitude, imagePath], (err, result) => {
    if (err) {
      console.error('Error inserting image into database:', err);
      return res.status(500).json({ error: 'Failed to upload image' });
    }
    res.status(200).json({ message: 'Image uploaded successfully' });
  });
});

app.listen(PORT, () => {
  console.log(`Server is running on http://localhost:${PORT}`);
});
