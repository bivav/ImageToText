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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 2;
    private StorageReference storageRef;

    ImageView imageData;
    Button take_picture;
    Button get_text;

    Bitmap imageBitmap;

    TextView text_processed;


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

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("IMAGE_ANALYZE", e.getMessage());

            }
        });


        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

            Uri fil = Uri.fromFile(file);
            StorageReference riversRef = storageRef.child("images/rivers1.jpg");

            riversRef.putFile(fil)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
//                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            Log.i("IMAGE_UPLOAD", taskSnapshot.getMetadata().getPath());
                            Log.i("IMAGE_UPLOAD", "File Uploaded");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.i("IMAGE_UPLOAD", "File NOT Uploaded");
                        }
                    });


        } catch (FileNotFoundException e) {
            e.printStackTrace();
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
