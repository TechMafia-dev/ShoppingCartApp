 package com.justforfun.phoneverification;

 import android.Manifest;
 import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
 import androidx.appcompat.app.ActionBarDrawerToggle;
 import androidx.appcompat.app.AppCompatActivity;
 import androidx.appcompat.widget.Toolbar;
 import androidx.core.view.GravityCompat;
 import androidx.drawerlayout.widget.DrawerLayout;

 import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
 import com.google.android.material.navigation.NavigationView;
 import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

 public class EditProfile extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

     private ImageView pic_profile;
     private TextView epName, epEmail, epPhone;
     private FirebaseAuth fAuth;
     private FirebaseFirestore fStore;
     private FirebaseUser fUser;
     private String userID;
     private Button save_btn;
     StorageReference mStorageRef;
     private Dialog dialog;
     private OutputStream outputStream;
     private Bitmap bitmap;
     private static final int WRITE_EXTERNAL_STORAGE_CODE = 1;

     //for navigation drawer
     private DrawerLayout drawerLayout;
     private NavigationView navigationView;
     private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_CODE);
            }
        }

        epName = findViewById(R.id.profile_nameEp);
        epEmail = findViewById(R.id.profile_emailEp);
        epPhone = findViewById(R.id.profile_phoneEp);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        pic_profile = findViewById(R.id.profilePicEp);

        StorageReference fileRef = mStorageRef.child("users/"+fAuth.getCurrentUser().getUid()+"/profile.jpg");
        pic_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGalleryIntent, 1000);
            }
        });

        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(pic_profile);
            }
        });

        userID = fAuth.getCurrentUser().getUid();
        fUser = fAuth.getCurrentUser();

        epName.setText(getIntent().getExtras().getString("name"));
        epEmail.setText(getIntent().getExtras().getString("email"));
        epPhone.setText(getIntent().getExtras().getString("phone"));

        final DocumentReference docuref = fStore.collection("users").document(userID);

        save_btn = findViewById(R.id.profile_updateEp);

        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (epName.getText().toString().isEmpty() || epEmail.getText().toString().isEmpty() || epPhone.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(), "One or more fields are empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                fUser.updateEmail(epEmail.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Map<String, Object> edited = new HashMap<>();
                        edited.put("email", epEmail.getText().toString());
                        edited.put("fName", epName.getText().toString());
                        edited.put("uphone", epPhone.getText().toString());
                        docuref.update(edited);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
                finish();
            }
        });

        //for Navigation drawer:
        drawerLayout = findViewById(R.id.drawer_layout_edit_profile);
        navigationView = findViewById(R.id.nav_view_edit_profile);
        toolbar = findViewById(R.id.toolbar_edit_profile);

        setSupportActionBar(toolbar);
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_profile);

        Menu menu = navigationView.getMenu();
        //menu.findItem(R.id.nav_login).setVisible(false);
        //menu.findItem(R.id.nav_logout).setVisible(false);
    }

     private void alertDialog() {
         dialog = new Dialog(EditProfile.this);
         dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
         dialog.setContentView(R.layout.wait);
         dialog.setCancelable(false);
         dialog.show();
     }

     @Override
     protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         if (requestCode == 1000){
             if (resultCode == Activity.RESULT_OK){
                 Uri ImageUri = data.getData();
                 pic_profile.setImageURI(ImageUri);

                 /*try {
                     saveImageLocally(ImageUri);
                 } catch (FileNotFoundException e) {
                     e.printStackTrace();
                 }*/
                 uploadImageToFirebase(ImageUri);
             }
         }
     }

     private void saveImageLocally(Uri imageUri) throws FileNotFoundException {
         bitmap = ((BitmapDrawable)pic_profile.getDrawable()).getBitmap();
         File path = Environment.getExternalStorageDirectory();
         File dir = new File(path.getAbsolutePath() + "/UserInfo/");
         dir.mkdir();
         File file = new File(dir, "ProfilePic.jpg");
         OutputStream outputStream = new FileOutputStream(file);

         try {
             bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
             outputStream.flush();
             outputStream.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }

     private void uploadImageToFirebase(Uri imageUri) {
         final StorageReference fileRef = mStorageRef.child("users/"+fAuth.getCurrentUser().getUid()+"/profile.jpg");
         fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
             @Override
             public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                 fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                     @Override
                     public void onSuccess(Uri uri) {
                         Picasso.get().load(uri).into(pic_profile);

                     }
                 });
             }
         }).addOnFailureListener(new OnFailureListener() {
             @Override
             public void onFailure(@NonNull Exception e) {
                 Log.d("upload_failure", e.toString());
             }
         });
     }

     public void onRequestPermissionsResult(){

     }

     @Override
     public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
         switch (menuItem.getItemId()){
             case R.id.nav_home:
                 startActivity(new Intent(getApplicationContext(), WebContent.class));
                 finish();
                 break;
             case R.id.nav_cycling:
                 break;
             case R.id.nav_bus:
                 break;
             case R.id.nav_plane:
                 break;
             case R.id.nav_feed:
                 break;
             case R.id.nav_store:
                 startActivity(new Intent(getApplicationContext(), StoreListings.class));
                 finish();
                 break;
             case R.id.nav_login:
                 break;
             case R.id.nav_profile:
                 break;
             case R.id.nav_logout:
                 FirebaseAuth.getInstance().signOut();
                 startActivity(new Intent(getApplicationContext(), phoneOTP.class));
                 finish();
                 Toast.makeText(getApplicationContext(), "You need to login in order to continue", Toast.LENGTH_SHORT).show();
                 break;
             case R.id.nav_share:
                 break;
             case R.id.nav_rate:
                 break;
         }
         drawerLayout.closeDrawer(GravityCompat.START);
         return true;
     }
}
