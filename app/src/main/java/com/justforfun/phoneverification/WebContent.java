package com.justforfun.phoneverification;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;

public class WebContent extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    /*VideoView videoView;
    TextView url;
    ProgressDialog progressDialog;*/
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference reference = firebaseDatabase.getReference();
    private DatabaseReference childRefrence = reference.child("webContent");

    private RecyclerView recyclerView;
    private WebContentAdapter webContentAdapter;
    private HashMap<String, WebItems> list;

    //for navigation drawer
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private OutputStream outputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_content);
        /*videoView = findViewById(R.id.content_video);
        progressDialog = new ProgressDialog(WebContent.this);
        url = findViewById(R.id.content_description);
        progressDialog.setMessage("Buffering..... please wait");
        progressDialog.show();*/

        list = new HashMap<>();

        recyclerView = findViewById(R.id.Web_content_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        childRefrence.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){
                    WebItems i = dataSnapshot1.getValue(WebItems.class);
                    if (i.getHeader() == "Forest")
                        continue;
                    if (!list.containsKey(i.getHeader())) {
                        list.put(i.getHeader(), i);
                    }
                }
                webContentAdapter = new WebContentAdapter(WebContent.this, list);
                recyclerView.setAdapter(webContentAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        recyclerView.setAdapter(webContentAdapter);

        ///saveTOFILE();


        //for Navigation drawer:
        drawerLayout = findViewById(R.id.drawer_layout_web_content);
        navigationView = findViewById(R.id.nav_view_web_content);
        toolbar = findViewById(R.id.toolbar_web_content);

        setSupportActionBar(toolbar);
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);

        Menu menu = navigationView.getMenu();
        //menu.findItem(R.id.nav_login).setVisible(false);
        //menu.findItem(R.id.nav_logout).setVisible(false);
    }

    private void saveTOFILE() {


        ImageView imageView = findViewById(R.id.img);
        //Uri uri = Uri.parse("https://firebasestorage.googleapis.com/v0/b/justforexp-38035.appspot.com/o/WebContent%2FImage%2FWater%20Body.jpg?alt=media&token=67e68f9d-ddb5-4948-ab80-802c5a7f7715");
        //Glide.with(WebContent.this).load(uri).placeholder(R.drawable.ic_action_play).into(imageView);
        imageView.setImageResource(R.drawable.water_body);

        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        /*imageView.buildDrawingCache();
        Bitmap bitmap = imageView.getDrawingCache();*/

        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File file = new File(directory, "UniqueFileName" + ".jpg");
        if (!file.exists()) {
            Log.d("path", file.toString());
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.nav_home:
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
                startActivity(new Intent(getApplicationContext(), ProfilePage.class));
                finish();
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
