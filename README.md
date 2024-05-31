

# Project Documentation

## Table of Contents

1. [Project Overview](#project-overview)
2. [Directory Structure](#directory-structure)
3. [Installation](#installation)
4. [Configuration](#configuration)
5. [API Endpoints](#api-endpoints)
6. [Database Schema](#database-schema)
7. [Running the Application](#running-the-application)
8. [Usage](#usage)
9. [Contributing](#contributing)
10. [License](#license)

## Project Overview

This project is an Android application with a Node.js backend and an SQL database. The app allows users to take pictures using the camera or select them from the gallery. The images are then uploaded to a directory named `uploads` via a Node.js API. The path to the uploaded image, along with the device's coordinates where the picture was taken, is stored in the SQL database.

## Directory Structure

```
Project_Root/
│
├── Node_Backend/
│   ├── uploads/
│   │   └── [uploaded_images]
│   ├── api.js
│   ├── config.js
│   ├── package-lock.json
│   ├── package.json
│   └── VectorSense/
│
├── .gradle/
├── .idea/
├── app/
├── gradle/
│   ├── wrapper/
│   ├── build.gradle
│   ├── gradle.properties
│   ├── gradlew
│   ├── gradlew.bat
│   └── settings.gradle
├── README.md
└── [other_files]
```

### Node_Backend

- **uploads/**: Directory where the uploaded images are stored.
- **api.js**: Contains the API endpoints for image upload and other backend functionalities.
- **config.js**: Configuration file for the backend settings.
- **package-lock.json**: Automatically generated for any operations where npm modifies either the node_modules tree or package.json.
- **package.json**: Contains metadata about the project and its dependencies.
- **VectorSense/**: Additional backend files or modules.

### Other Directories

- **.gradle/**: Gradle-related files and caches.
- **.idea/**: IDE-specific settings and metadata.
- **app/**: The main Android application source code.
- **gradle/**: Gradle build files and scripts.
- **README.md**: Project documentation file.

## Installation

### Prerequisites

- Node.js and npm installed on your system.
- Android Studio installed for developing the Android application.
- SQL database setup (e.g., MySQL, PostgreSQL).

### Backend Installation

1. Navigate to the `Node_Backend` directory.
2. Install the dependencies:

    ```sh
    npm install
    ```

### Frontend Installation

1. Open the project in Android Studio.
2. Sync the project with Gradle files.

## Configuration

### Backend Configuration

Configure the database connection and other settings in the `config.js` file. Example configuration:

```javascript
module.exports = {
    database: {
        host: 'localhost',
        user: 'root',
        password: '',
        database: 'your_database_name'
    },
    uploadPath: 'uploads/'
};
```

## API Endpoints

### POST /upload

Uploads an image to the server.

- **Request**:
    - `Content-Type`: `multipart/form-data`
    - Parameters:
        - `image`: The image file to be uploaded.
        - `latitude`: The latitude where the image was taken.
        - `longitude`: The longitude where the image was taken.

- **Response**:
    - `200 OK`: On successful upload.
    - `400 Bad Request`: If the image or coordinates are missing.

### Example Request

```sh
curl -X POST http://localhost:3000/upload \
     -F "image=@/path/to/image.jpg" \
     -F "latitude=37.7749" \
     -F "longitude=-122.4194"
```

## Database Schema

The database should have a table to store image paths and coordinates. Example schema:

```sql
CREATE TABLE images (
    id INT AUTO_INCREMENT PRIMARY KEY,
    path VARCHAR(255) NOT NULL,
    latitude DECIMAL(9,6) NOT NULL,
    longitude DECIMAL(9,6) NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## Running the Application

### Backend

1. Start the backend server:

    ```sh
    node api.js
    ```

### Frontend

1. Run the Android app from Android Studio.

## Usage

- Open the Android app.
- Take a picture using the camera or select one from the gallery.
- The app will upload the image to the server, and the backend will store the image path and coordinates in the database.

## Contributing

Contributions are welcome! Please fork the repository and submit pull requests.

## License

This project is licensed under the MIT License.

---
