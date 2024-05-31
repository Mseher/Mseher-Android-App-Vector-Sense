package com.example.vectorsense;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.vectorsense.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 100 ;
    private static final int REQUEST_IMAGE_UPLOAD = 120;
    private static final int PERMISSION_REQUEST_CODE = 110;
    private static final int REQUEST_LOCATION_ENABLE = 200;

    private ProgressDialog progressDialog;
    public Uri imageUri;
    private ProgressBar progressBar;

    private final String[] permissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };
    ActivityMainBinding binding;
    private LocationManager locationManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //permissions
        if (!checkPermissions()) {
            // Request permissions
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        } else {
            // Permissions are already granted
            // Check if location is enabled
            if (!isLocationEnabled()) {
                // Location is not enabled, prompt user to enable it
                requestLocationEnable();
            }
            // Proceed with your code logic
        }
        //
        binding.TakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        // Set onClickListener for UploadPicture button
        binding.UploadPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchUploadPictureIntent();
            }
        });
    }

    // Method to check if location is enabled
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // Method to request location enable
    public void requestLocationEnable() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Location Required");
        builder.setMessage("Please enable location services to use this feature.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent, REQUEST_LOCATION_ENABLE);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle cancellation, if needed
            }
        });
        builder.create().show();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            // Check if all permissions are granted
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                // All permissions granted, proceed with your code logic
            } else {
                // Permission denied, handle this situation
            }
        }
    }

    private boolean checkPermissions() {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void dispatchTakePictureIntent() {
        try{
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "New Picture");
                values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
                imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }catch (Exception exp){
            Toast.makeText(this, "GetLoc" + exp, Toast.LENGTH_SHORT).show();
        }

    }

    private void dispatchUploadPictureIntent() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, REQUEST_IMAGE_UPLOAD);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            binding.capture.setImageURI(imageUri);
            Log.d("Image", imageUri.toString());
            binding.progressBar.setVisibility(View.VISIBLE);
            getLocation(imageUri);
            // Save the image to the gallery
            saveImageToGallery(imageUri);

        }
        else if (requestCode == REQUEST_IMAGE_UPLOAD && resultCode == RESULT_OK && data != null) {

            Uri selectedImage = data.getData();  // This is the line that extracts the URI

            if (selectedImage == null) {
                Toast.makeText(this, "Error: Image URI is null", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                binding.capture.setImageBitmap(bitmap);
                binding.progressBar.setVisibility(View.VISIBLE);
                getLocation(selectedImage); // Ensure this method can handle the URI correctly
                // Save the image to the gallery
                saveImageToGallery(selectedImage);
            } catch (IOException e) {
                Toast.makeText(this, "Failed to load image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImageToFirebase(Bitmap bitmap, double lati, double longi) {
        try{

            // Show progress dialog
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Uploading image...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] byteArray = baos.toByteArray();
            String base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);

            CurrentAttLocation loc = new CurrentAttLocation(lati, longi, base64Image);

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference();
            myRef.child("images").child(getRandomId()).setValue(loc).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d("Upload", "Image uploaded successfully");
                        Toast.makeText(MainActivity.this, "Image uploaded", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("Upload", "Image upload failed: " + task.getException());
                        Toast.makeText(MainActivity.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                    }
                    // Dismiss progress dialog regardless of success or failure
                    progressDialog.dismiss();
                }
            });
        }catch (Exception ex){
            Toast.makeText(this,"Image Upload" + ex.toString(), Toast.LENGTH_SHORT).show();
        }


    }

    private  String getRandomId() {
        String ID = "";
        try{
            LocalDateTime now = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                now = LocalDateTime.now();
            }

            // Define the desired format
            DateTimeFormatter formatter = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            }

            // Format date and time in the desired format
            String formattedDateTime = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                formattedDateTime = now.format(formatter);
            }

            ID =   formattedDateTime;
        }catch (Exception ex){
            Toast.makeText(this,"random ID" + ex.toString(), Toast.LENGTH_SHORT).show();
        }
        return ID;
    }

    private void getLocation(Uri image)  {
        try {
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {

                    double lati = location.getLatitude();
                    double longi = location.getLongitude();
                    //uploadImageToFirebase(image, lati, longi);
                    uploadImageToServer(image, lati, longi);
                }

                @Override
                public void onProviderEnabled(@NonNull String provider) {
                    Log.d("Location", "Data set changed");
                }

                @Override
                public void onProviderDisabled(@NonNull String provider) {

                    Log.d("Location", "Data set changed");
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                    Log.d("Location", "Data set changed");
                }
                //

            };
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
        }catch (Exception exception){
            Toast.makeText(this,"Image upload" + exception.toString(), Toast.LENGTH_SHORT).show();

        }


    }

    //reto
    private void uploadImageToServer(Uri imageUri, double latitude, double longitude) {
        // Convert the URI to a File object
        File imageFile = new File(getRealPathFromURI(imageUri));

        // Create a request body with the image file
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image_path", imageFile.getName(), requestFile);

        // Create request body for latitude and longitude
        RequestBody latitudeBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(latitude));
        RequestBody longitudeBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(longitude));

        // Retrofit service instance
        UploadService service = RetroClient.getClient().create(UploadService.class);

        // Call the appropriate method in your service interface to upload the image with location data
        Call<ResponseBody> call = service.uploadImage(imagePart, latitudeBody, longitudeBody);

        // Execute the call asynchronously
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Image Uploaded Sucessfully", Toast.LENGTH_LONG).show();
                 Log.d("Data", "Uploaded Sucessfully");
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "upload failed please try later", Toast.LENGTH_LONG).show();

            }
        });
    }
    private void saveImageToGallery(Uri imageUri) {

        if (imageUri == null) {
            Toast.makeText(this, "Error: Image URI is null", Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "Image_" + System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg"); // or "image/png" depending on the image format
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "/storage/Pictures/vectorsense"); // Change 'VectorSense' to your application's name

        Uri uri = null;
        try {
            uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    if (inputStream != null) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        inputStream.close();
                    } else {
                        getContentResolver().delete(uri, null, null);
                        Toast.makeText(this, "Failed to read image file", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                Toast.makeText(this, "Image saved to gallery", Toast.LENGTH_LONG).show();
                // Request a media scan for the saved image file
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            } else {
                Toast.makeText(this, "Failed to create new MediaStore record", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            if (uri != null) {
                getContentResolver().delete(uri, null, null); // Clean up
            }
            Toast.makeText(this, "Failed to save image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    //utility functions
    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor == null)
            return contentUri.getPath();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }
}