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
import android.widget.ImageView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private StorageReference storageRef;

    ImageView imageData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageData = findViewById(R.id.imageData);

        storageRef = FirebaseStorage.getInstance().getReference();

        dispatchTakePictureIntent();
    }




    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageData.setImageBitmap(imageBitmap);



            File file = new File(getCacheDir(), "UniqueFileName" + ".jpg");

            Bitmap bitmap = ((BitmapDrawable) imageData.getDrawable()).getBitmap();
            FileOutputStream fos = null;

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
    }

}
