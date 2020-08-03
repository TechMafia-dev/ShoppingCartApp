package com.justforfun.phoneverification;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class ProfilePage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ImageView pic_profile;
    private TextView pName, pEmail, pPhone, verify;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;
    private Button logout, verify_btn, password_reset, profile_update, refresh;
    StorageReference mStorageRef;

    //for navigation drawer
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page_profile);

        pName = findViewById(R.id.profile_name);
        pEmail = findViewById(R.id.profile_email);
        pPhone = findViewById(R.id.profile_phone);

        verify = findViewById(R.id.verifystatus);
        verify_btn = findViewById(R.id.verufybtn);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        StorageReference fileRef = mStorageRef.child("users/"+fAuth.getCurrentUser().getUid()+"/profile.jpg");
        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(pic_profile);
            }
        });

        final FirebaseUser fUser = fAuth.getCurrentUser();
        if (fUser.isEmailVerified()){
            verify.setText("Email address verified!!!");
            verify.setTextColor(5);
            verify_btn.setClickable(false);
            verify_btn.setVisibility(View.INVISIBLE);
        }
        else {
            verify.setText("Email address yet to be verified.");
            verify.setTextColor(4);
        }
        verify_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                fUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "Verification mail sent", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Some unknown error occured. Try after some time", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        userID = fAuth.getCurrentUser().getUid();

        DocumentReference docuref = fStore.collection("users").document(userID);
        docuref.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                pName.setText(documentSnapshot.getString("fName"));
                pEmail.setText(documentSnapshot.getString("email"));
                pPhone.setText(documentSnapshot.getString("uphone"));
            }
        });

        logout = findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }
        });

        password_reset = findViewById(R.id.localresetpassword);
        password_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final EditText resetMail = new EditText(view.getContext());
                final AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(view.getContext());
                passwordResetDialog.setTitle("Reset Password??");
                passwordResetDialog.setMessage("Enter registered email to recieve password reset link");
                passwordResetDialog.setView(resetMail);

                passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String mail = resetMail.getText().toString().trim();
                        fAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getApplicationContext(), "Reset link sent to your registered mail", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "Some glitch!! Couldn't send reset link", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                passwordResetDialog.create().show();
            }
        });

        pic_profile = findViewById(R.id.profilePic);
        profile_update = findViewById(R.id.profile_update);

        profile_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), EditProfile.class);
                intent.putExtra("name", pName.getText().toString());
                intent.putExtra("email", pEmail.getText().toString());
                intent.putExtra("phone", pPhone.getText().toString());
                startActivity(intent);
            }
        });

        refresh = findViewById(R.id.refreshAll);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refresh();
            }
        });

        //for Navigation drawer:
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

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

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void refresh() {
        DocumentReference docuref = fStore.collection("users").document(userID);
        docuref.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if (fAuth!=null) {
                    pName.setText(documentSnapshot.getString("fName"));
                    pEmail.setText(documentSnapshot.getString("email"));
                    pPhone.setText(documentSnapshot.getString("uphone"));
                }
            }
        });
        StorageReference fileRef = mStorageRef.child("users/"+fAuth.getCurrentUser().getUid()+"/profile.jpg");
        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(pic_profile);
            }
        });
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
                finish();
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), phoneOTP.class));
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
