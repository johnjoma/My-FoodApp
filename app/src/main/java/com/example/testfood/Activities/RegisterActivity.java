package com.example.testfood.Activities;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.testfood.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class RegisterActivity extends AppCompatActivity {

    ImageView ImgUserPhoto;
    static int  PReqCode = 1;
    static int  REQUESCODE =1;
    Uri pickedImgUri;

    private EditText userEmail,userPassword,userPassword2,userName;
    private ProgressBar loadingProgress;
    private Button regBtn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userEmail= findViewById(R.id.regMail);
        userPassword = findViewById(R.id.regPassword);
        userPassword2 = findViewById(R.id.regPassword2);
        userName = findViewById(R.id.regName);
        loadingProgress = findViewById(R.id.regProgressBar);
        regBtn = findViewById(R.id.regBtn);

        loadingProgress.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                regBtn.setVisibility(View.INVISIBLE);
                loadingProgress.setVisibility(View.VISIBLE);
                final String email = userEmail.getText().toString();
                final String password = userPassword.getText().toString();
                final String password2 = userPassword2.getText().toString();
                final String name = userName.getText().toString();

                if (email.isEmpty() || name.isEmpty() || password.isEmpty() || !password.equals(password2)){

                    //something goes wrong: All fields must be filled.
                    // We need to display error message.

                    showMessage("Please verify all fields");

                    regBtn.setVisibility(View.VISIBLE);
                    loadingProgress.setVisibility(View.INVISIBLE);
                }
                else{

                    // Creating user account.
                    CreateUserAccount(email,name,password);


                }
            }
        });


        ImgUserPhoto=findViewById(R.id.regUserPhoto);

        ImgUserPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Build.VERSION.SDK_INT >= 22){

                    checkAndRequestForPermission();
                }
                else {

                    openGallery();
                }


            }
        });

    }

    private void CreateUserAccount(String email, final String name, String password) {

        // This method creates user account with specific email and password.

        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){

                            //user account created successfully
                            showMessage("Account created Successfully.");
                            // After we created user account we need to update user profile and name.

                            UpdateUserInfo( name,pickedImgUri,mAuth.getCurrentUser());

                        }
                        else {

                            //Account failed.
                            showMessage("Account creation failed." + task.getException() .getMessage());
                            regBtn.setVisibility(View.VISIBLE);
                            loadingProgress.setVisibility(View.INVISIBLE);
                        }
                    }
                });

    }

    //update user photo and name

    private void UpdateUserInfo(final String name, Uri pickedImgUri, final FirebaseUser currentUser) {

        // first we need to upload user photo to firebase storage and get url

        StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("users_photo");
        final StorageReference imageFilepath = mStorage.child(pickedImgUri.getLastPathSegment());
        imageFilepath.putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                //image uploaded successfully
                // now we can get our image url

                imageFilepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        // uri contain user image url
                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .setPhotoUri(uri)
                                .build();

                        currentUser.updateProfile(profileUpdate)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            // user info updated successfully
                                            showMessage("Register Complete");
                                            
                                            updateUI();

                                        }
                                    }
                                });


                    }
                });

            }
        });

    }

    private void updateUI() {
        Intent homeActivity = new Intent(getApplicationContext(),HomeActivity.class);
        startActivity(homeActivity);
        finish();


    }

    // simple method to show toast message.

    private void showMessage(String message) {

        Toast.makeText(getApplicationContext(),message, Toast.LENGTH_LONG).show();
    }

    private void openGallery() {
      //TO DO: Open gallery intent and wait for user to pick an image
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,REQUESCODE);


    }

    private void checkAndRequestForPermission() {

        if(ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)

                != getPackageManager().PERMISSION_GRANTED){

            if (ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)){

                Toast.makeText(RegisterActivity.this,"please accept for required permission",Toast.LENGTH_SHORT).show();
            }

            else {

                ActivityCompat.requestPermissions(RegisterActivity.this,

                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},

                                         PReqCode);
            }
        }

        else

            openGallery();


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUESCODE && data !=null){

            pickedImgUri = data.getData();
            ImgUserPhoto.setImageURI(pickedImgUri);

        }
    }
}
