package com.imagetotext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 2;
    private StorageReference storageRef;

    ImageView imageData;
    Button take_picture;
    Button get_text;
    String image_storage_location = "", processed_image_text = "";

    Bitmap imageBitmap;

    TextView text_processed;

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        take_picture = findViewById(R.id.take_picture);
        get_text = findViewById(R.id.get_text);

        text_processed = findViewById(R.id.text_processed);
        imageData = findViewById(R.id.imageData);
        storageRef = FirebaseStorage.getInstance().getReference();

        take_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        get_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImageToText();
            }
        });

    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void getImageToText(){

        final String key = myRef.child("imagetotext-cc").push().getKey();
        final Map<String, Object> childUpdates = new HashMap<>();

        try {

            File file = new File(getCacheDir(), "UniqueFileName" + ".jpg");

            Bitmap bitmap = ((BitmapDrawable) imageData.getDrawable()).getBitmap();
            FileOutputStream fos = null;

            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

            FirebaseVisionTextRecognizer textRecognizer = FirebaseVision.getInstance()
                    .getOnDeviceTextRecognizer();

            textRecognizer.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                @Override
                public void onSuccess(FirebaseVisionText firebaseVisionText) {
                    Log.i("IMAGE_ANALYZE", firebaseVisionText.getText());

                    text_processed.setText(firebaseVisionText.getText());

                    processed_image_text = firebaseVisionText.getText();

                    childUpdates.put(key + "/text_processed", processed_image_text);
                    myRef.updateChildren(childUpdates);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i("IMAGE_ANALYZE", e.getMessage());

                }
            });


            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            Uri fil = Uri.fromFile(file);

            Date c = Calendar.getInstance().getTime();
            System.out.println("Current time => " + c);

//            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
            SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhh:mm:ss", Locale.ENGLISH);
            String format = s.format(new Date());


            StorageReference riversRef = storageRef.child("images/" + "img-" + format + ".jpg");

            riversRef.putFile(fil)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
//                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            Log.i("IMAGE_UPLOAD", taskSnapshot.getMetadata().getPath());

                            image_storage_location = taskSnapshot.getMetadata().getPath();

                            Log.i("IMAGE_UPLOAD", "File Uploaded");

                            if (image_storage_location != null) {

                                childUpdates.put(key +"/image_location", image_storage_location);
                                myRef.updateChildren(childUpdates);
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.i("IMAGE_UPLOAD", "File NOT Uploaded");
                        }
                    });




        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Take a picture first!", Toast.LENGTH_LONG).show();
        }





    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            imageData.setImageBitmap(imageBitmap);

        }
    }

}
