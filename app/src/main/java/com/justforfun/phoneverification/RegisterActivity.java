package com.justforfun.phoneverification;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class RegisterActivity extends AppCompatActivity {


    private EditText name, email, password, phone_number;
    private Button register_btn;
    private FirebaseAuth fAuth;
    private FirebaseUser fUser;
    private FirebaseFirestore fStore;
    private AuthCredential fCredential;
    private ProgressBar progressBar;
    private String userID;
    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activty);

        name = findViewById(R.id.name);
        email = findViewById(R.id.remail);
        password = findViewById(R.id.rpassword);
        phone_number = findViewById(R.id.rphonenumber);
        phone_number.setText(getIntent().getExtras().getString("phoneNo."));
        phone_number.setFocusable(false);
        register_btn = findViewById(R.id.bregister);

        fAuth = FirebaseAuth.getInstance();
        fUser = fAuth.getCurrentUser();
        fStore = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.progressBar);

   /*     if (fAuth != null){
            startActivity(new Intent(getApplicationContext(), ProfilePage.class));
            finish();
        }*/

        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String mail = email.getText().toString().trim();
                String passcode = password.getText().toString().trim();
                final String uname = name.getText().toString().trim();
                final String uphone = phone_number.getText().toString().trim();


                if (TextUtils.isEmpty(uname)){
                    name.setError("Name field cannot be empty");
                    return;
                }
                if (TextUtils.isEmpty(mail)){
                    email.setError("Email field cannot be empty");
                    return;
                }
                if (TextUtils.isEmpty(passcode)){
                    password.setError("Password field cannot be empty");
                    return;
                }
                if (passcode.length() < 6){
                    password.setError("Password must contain atleast 6 characters");
                }
                /*if (TextUtils.isEmpty(uphone)){
                    phone_number.setError("Phone number field cannot be empty");
                    return;
                }*/

                progressBar.setVisibility(View.VISIBLE);

                fCredential = EmailAuthProvider.getCredential(mail, passcode);
                fUser.linkWithCredential(fCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            //updateUI(task.getResult().getUser());
                            Toast.makeText(getApplicationContext(), "User Created.", Toast.LENGTH_SHORT).show();

                            FirebaseUser fUser = fAuth.getCurrentUser();
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

                            userID = fAuth.getCurrentUser().getUid();
                            DocumentReference docuRef = fStore.collection("users").document(userID);
                            Map<String, Object> user = new HashMap<>();
                            user.put("fName", uname);
                            user.put("email", mail);
                            user.put("uphone", uphone);
                            docuRef.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "onSuccess: User profile is created for"+userID);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure:" + e.toString());
                                }
                            });
                            startActivity(new Intent(getApplicationContext(), StoreListings.class));
                            finish();
                        }
                        else {
                            //updateUI(null);
                            fAuth.signInWithCredential(fCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                }
                            });
                        }
                    }
                });

                /*fAuth.createUserWithEmailAndPassword(mail, passcode).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "User Created.", Toast.LENGTH_SHORT).show();

                            FirebaseUser fUser = fAuth.getCurrentUser();
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

                            userID = fAuth.getCurrentUser().getUid();
                            DocumentReference docuRef = fStore.collection("users").document(userID);
                            Map<String, Object> user = new HashMap<>();
                            user.put("fName", uname);
                            user.put("email", mail);
                            user.put("uphone", uphone);
                            docuRef.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "onSuccess: User profile is created for"+userID);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure:" + e.toString());
                                }
                            });
                            startActivity(new Intent(getApplicationContext(), ProfilePage.class));
                            finish();
                        }
                        else {
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(getApplicationContext(), "Error!! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });*/
            }
        });
    }

    /*private void updateUI(FirebaseUser user) {

        TextView idView = findViewById(R.id.anonymousStatusId);
        TextView emailView = findViewById(R.id.anonymousStatusEmail);
        boolean isSignedIn = (user != null);

        // Status text
        if (isSignedIn) {
            idView.setText(getString(R.string.id_fmt, user.getUid()));
            emailView.setText(getString(R.string.email_fmt, user.getEmail()));
        } else {
            idView.setText(R.string.signed_out);
            emailView.setText(null);
        }

        // Button visibility
        findViewById(R.id.buttonAnonymousSignIn).setEnabled(!isSignedIn);
        findViewById(R.id.buttonAnonymousSignOut).setEnabled(isSignedIn);
        findViewById(R.id.buttonLinkAccount).setEnabled(isSignedIn);
    }*/

    private void start() {
        startActivity(new Intent(this, LoginActivity.class));
    }
}
