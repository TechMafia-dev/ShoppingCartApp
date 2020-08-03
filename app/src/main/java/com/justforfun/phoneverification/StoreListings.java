package com.justforfun.phoneverification;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StoreListings extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;

    private DatabaseReference reference;
    private RecyclerView recyclerView;
    private HashMap<String, Items> items;
    private ListingAdapter adapter;
    private int cart_total=0, sub_count=0, sub_cost=0;
    private Button cartProceed;
    private TextView cartTotal;
    private Button proceedToCheckout;

    //for navigation drawer
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    //for Location
    private LinearLayout yourLocation;
    private EditText[] locationFine = new EditText[7];
    private ProgressBar Address_Progress;
    private FusedLocationProviderClient fusedLocationProviderClient;

    //Search View
    SearchView mySearchView;
    ListView myList;

    ArrayList<String> list;
    ArrayAdapter<String> SearchAdapter;

    HashMap<String, CheckOutListing> checkOutSummary;
    TextView CartSummaryTotal;
    RecyclerView cart_recycler;

    //For Order Updates
    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;
    private FirebaseUser fUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_listings);

        recyclerView = this.findViewById(R.id.recyclerList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        items = new HashMap<>();


        fAuth = FirebaseAuth.getInstance();

        reference = FirebaseDatabase.getInstance().getReference().child("Store");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){
                    Items i = dataSnapshot1.getValue(Items.class);
                    if (!items.containsKey(i.getItemID())) {
                        items.put(i.getItemID(), i);
                        items.get(i.getItemID()).setItemCount(0);
                    }
                }
                adapter = new ListingAdapter(StoreListings.this, items);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        recyclerView.setAdapter(adapter);

        LocalBroadcastManager.getInstance(this).registerReceiver(messageReciever, new IntentFilter("count_value_changed"));
        LocalBroadcastManager.getInstance(this).registerReceiver(onDeleteListner, new IntentFilter("item_deleted"));
        checkOutSummary = new HashMap<>();

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
        navigationView.setCheckedItem(R.id.nav_store);

        Menu menu = navigationView.getMenu();
        //menu.findItem(R.id.nav_login).setVisible(false);
        //menu.findItem(R.id.nav_logout).setVisible(false);


        //Search View
        mySearchView = findViewById(R.id.SearchView);
        myList = findViewById(R.id.myList);

        list = new ArrayList<>();
        //initialiseSearchList();
/*
        fStore = FirebaseFirestore.getInstance();
        DocumentReference docuref = fStore.collection("Suggestions").document("Search Suggestion");
        docuref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()){
                        list = (ArrayList<String>) documentSnapshot.get("tm284");
                    }
                }
            }
        });
        Log.d ("list", list.get(0));*/

        initialiseSearchList();

        SearchAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
        myList.bringToFront();
        myList.setVisibility(View.INVISIBLE);
        myList.setAdapter(SearchAdapter);

        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String text = adapterView.getItemAtPosition(i).toString();
                mySearchView.setQuery(text, true);
            }
        });

        mySearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                RefilterList(s);
                myList.setVisibility(View.INVISIBLE);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (s.isEmpty()){
                    adapter = new ListingAdapter(StoreListings.this, items);
                    recyclerView.setAdapter(adapter);
                    myList.setVisibility(View.INVISIBLE);
                } else {
                    myList.setVisibility(View.VISIBLE);
                    myList.bringToFront();
                }
                SearchAdapter.getFilter().filter(s);
                return false;
            }
        });
        //mySearchView.setFocusedByDefault(false);
        mySearchView.setSubmitButtonEnabled(true);

        cartProceed = findViewById(R.id.store_proceed);
        cartTotal = findViewById(R.id.cartTotal);
        Locale locale = new Locale("en", "in");
        NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
        String moneyString = formatter.format(cart_total);
        cartTotal.setText("Cart Total: " + moneyString);
        cartProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkOutCart();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0){
            getCurrentLocation();
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void getCurrentLocation() {
        final String[] local = new String[1];
        Address_Progress.setVisibility(View.VISIBLE);

        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null){
                    try {
                        Geocoder geocoder = new Geocoder(StoreListings.this, Locale.getDefault());

                        List<Address> addresses = geocoder.getFromLocation(
                                location.getLatitude(), location.getLongitude(), 1
                        );
                        local[0] = addresses.get(0).getFeatureName();
                        if (local[0] != null){
                            locationFine[2].setText(local[0]);
                        }
                        local[0] = addresses.get(0).getLocality();
                        if (local[0] != null){
                            locationFine[3].setText(local[0]);
                        }
                        local[0] = addresses.get(0).getAdminArea();
                        if (local[0] != null){
                            locationFine[4].setText(local[0]);
                        }
                        local[0] = addresses.get(0).getPostalCode();
                        if (local[0] != null){
                            locationFine[5].setText(local[0]);
                        }
                        local[0] = addresses.get(0).getCountryName();
                        if (local[0] != null){
                            locationFine[6].setText(local[0]);
                        }/*
                                addresses.get(0).getAddressLine(0)+ ",1: "+ //Colony, village/city, State, PIN, country
                                        addresses.get(0).getFeatureName()+",4: "+ //Colony
                                addresses.get(0).getLocality()+",7: "+ //area/village/city
                                        addresses.get(0).getSubAdminArea()+",8: "+ //District
                                        addresses.get(0).getAdminArea()+",9: "+ //State
                                addresses.get(0).getCountryName()+",10: "+ //Country
                                addresses.get(0).getPostalCode()); //PIN*/
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void RefilterList(String query) {
        final HashMap<String, Items> filteredItems = new HashMap<>();
        for(String key : items.keySet()){
            if (items.get(key).getItemName().toLowerCase().contains(query.toLowerCase())){
                filteredItems.put(key, items.get(key));
                continue;
            }
            if (items.get(key).getItemType().toLowerCase().contains(query.toLowerCase())){
                filteredItems.put(key, items.get(key));
                continue;
            }
            if (items.get(key).getItemID().toLowerCase().contains(query.toLowerCase())){
                filteredItems.put(key, items.get(key));
                continue;
            }
        }
        adapter = new ListingAdapter(StoreListings.this, filteredItems);
        recyclerView.setAdapter(adapter);
    }

    private void checkOutCart() {
        setContentView(R.layout.check_out_summary);
        cart_recycler = findViewById(R.id.cart_recycler);
        cart_recycler.setLayoutManager(new LinearLayoutManager(this));
        CheckOutAdapter checkOutAdapter = new CheckOutAdapter(StoreListings.this, checkOutSummary);
        cart_recycler.setAdapter(checkOutAdapter);
        Button addMore = findViewById(R.id.Cart_addmore);
        addMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backToListings();
            }
        });
        Button proceedToPay = findViewById(R.id.Cart_summary_pay);
        proceedToPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConfirmationDialogue();
            }
        });
        CartSummaryTotal = findViewById(R.id.Cart_summary_total);
        Locale locale = new Locale("en", "in");
        NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
        String moneyString = formatter.format(cart_total);
        CartSummaryTotal.setText("Cart Total: " + moneyString);

        locationFine[0] = findViewById(R.id.locationNo);
        locationFine[1] = findViewById(R.id.locationLandmark);
        locationFine[2] = findViewById(R.id.locationLocality);
        locationFine[3] = findViewById(R.id.locationCity);
        locationFine[4] = findViewById(R.id.locationState);
        locationFine[5] = findViewById(R.id.locationPIN);
        locationFine[6] = findViewById(R.id.locationCountry);
        Address_Progress = findViewById(R.id. progressBar3);
        yourLocation = findViewById(R.id.get_current_location);
        yourLocation.setClickable(true);
        if (yourLocation.isSelected())
            yourLocation.setBackgroundResource(R.drawable.btn_clicked);
        else
            yourLocation.setBackgroundResource(R.drawable.btn_idle);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        yourLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(
                            StoreListings.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_LOCATION_PERMISSION
                    );
                } else {
                    getCurrentLocation();
                }
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
        navigationView.setCheckedItem(R.id.nav_store);

        Menu menu = navigationView.getMenu();
        //menu.findItem(R.id.nav_login).setVisible(false);
        //menu.findItem(R.id.nav_logout).setVisible(false);
    }

    private void ConfirmationDialogue() {
        String local, complete = "";

        local = locationFine[0].getText().toString();
        if (!local.isEmpty())
            complete = local + ", ";

        local = locationFine[1].getText().toString();
        if (!local.isEmpty())
            complete = complete + local + ", ";

        local = locationFine[2].getText().toString();
        if (!local.isEmpty())
            complete = complete + local + ", ";

        local = locationFine[3].getText().toString();
        if (!local.isEmpty())
            complete = complete + local + ", ";

        local = locationFine[4].getText().toString();
        if (!local.isEmpty())
            complete = complete + local + ", ";

        local = locationFine[6].getText().toString();
        if (!local.isEmpty())
            complete = complete + local;

        local = locationFine[5].getText().toString();
        if (!local.isEmpty())
            complete = complete + " [" + local + "]";

        final ScrollView scrollView = findViewById(R.id.scroller);

        final AlertDialog.Builder builder = new AlertDialog.Builder(StoreListings.this);
        builder.setTitle("Continue with this address:").setMessage(complete).setCancelable(false);
        final String finalComplete = complete;
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                uploadData(finalComplete);
                checkOutSummary.clear();
                for (String key: items.keySet()){
                    items.get(key).setItemCount(0);
                }
                cart_total = 0;
                backToListings();
            }
        }).setNegativeButton("Edit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void uploadData(String address) {

        fUser = fAuth.getCurrentUser();

        HashMap<String, Integer> workOrders = new HashMap<>();

        for(String key : checkOutSummary.keySet()){
            workOrders.put(key, checkOutSummary.get(key).getQuantity());
        }
        this.fStore = FirebaseFirestore.getInstance();

        final String orderID = fAuth.getCurrentUser().getUid() + String.valueOf(System.currentTimeMillis());
        DocumentReference docuRef = fStore.collection("Workorders").document(orderID);
        Map<String, Object> order = new HashMap<>();

        order.put("Address", address);
        order.put("Total", cart_total);
        order.put("Order", workOrders);
        docuRef.set(order).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("Order generated", "onSuccess: User profile is created for"+orderID);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Order cancelled", "onFailure:" + e.toString());
            }
        });

    }

    private void backToListings() {

        setContentView(R.layout.activity_store_listings);

        recyclerView = this.findViewById(R.id.recyclerList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        reference = FirebaseDatabase.getInstance().getReference().child("Store");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){
                    Items i = dataSnapshot1.getValue(Items.class);
                    if (!items.containsKey(i.getItemID())) {
                        items.put(i.getItemID(), i);
                        items.get(i.getItemID()).setItemCount(0);
                    }
                }
                adapter = new ListingAdapter(StoreListings.this, items);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        recyclerView.setAdapter(adapter);

        cartProceed = findViewById(R.id.store_proceed);
        cartTotal = findViewById(R.id.cartTotal);
        Locale locale = new Locale("en", "in");
        NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
        String moneyString = formatter.format(cart_total);
        cartTotal.setText("Cart Total: " + moneyString);
        cartProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkOutCart();
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(messageReciever, new IntentFilter("count_value_changed"));

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
        navigationView.setCheckedItem(R.id.nav_store);

        Menu menu = navigationView.getMenu();
        //menu.findItem(R.id.nav_login).setVisible(false);
        //menu.findItem(R.id.nav_logout).setVisible(false);


        //Search View
        mySearchView = findViewById(R.id.SearchView);
        myList = findViewById(R.id.myList);

        SearchAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
        myList.bringToFront();
        myList.setVisibility(View.INVISIBLE);
        myList.setAdapter(SearchAdapter);

        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String text = adapterView.getItemAtPosition(i).toString();
                mySearchView.setQuery(text, true);
            }
        });

        mySearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                RefilterList(s);
                myList.setVisibility(View.INVISIBLE);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (s.isEmpty()){
                    adapter = new ListingAdapter(StoreListings.this, items);
                    recyclerView.setAdapter(adapter);
                    myList.setVisibility(View.INVISIBLE);
                } else {
                    myList.setVisibility(View.VISIBLE);
                    myList.bringToFront();
                }
                SearchAdapter.getFilter().filter(s);
                return false;
            }
        });
        //mySearchView.setFocusedByDefault(false);
        mySearchView.setSubmitButtonEnabled(true);
    }

    @Override
    public void onBackPressed() {
        adapter = new ListingAdapter(StoreListings.this, items);
        recyclerView.setAdapter(adapter);
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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
                break;
            case R.id.nav_login:
                break;
            case R.id.nav_profile:
                startActivity(new Intent(getApplicationContext(), ProfilePage.class));
                finish();
                break;
            case R.id.nav_logout:
                fAuth.signOut();
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

    public BroadcastReceiver onDeleteListner =new BroadcastReceiver() {
        String key;
        @Override
        public void onReceive(Context context, Intent intent) {
            key = intent.getStringExtra("key");
            Log.d("key", key);
            checkOutSummary.remove(key);
            items.get(key).setItemCount(0);
            computeTotal();
            Locale locale = new Locale("en", "in");
            NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
            String moneyString = formatter.format(cart_total);
            CartSummaryTotal.setText("Cart Total: " + moneyString);

            cart_recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            CheckOutAdapter checkOutAdapter = new CheckOutAdapter(StoreListings.this, checkOutSummary);
            cart_recycler.setAdapter(checkOutAdapter);
        }
    };

    public BroadcastReceiver messageReciever = new BroadcastReceiver() {
        String name, id;
        int cost, quantity;
        CheckOutListing checkoutitem;

        @Override
        public void onReceive(Context context, Intent intent) {
            id  = intent.getStringExtra("id");
            name = intent.getStringExtra("name");
            cost = Integer.parseInt(intent.getStringExtra("cost"));
            quantity = Integer.parseInt(intent.getStringExtra("quantity"));
            checkoutitem = new CheckOutListing(name, cost, quantity, id);
            if (checkOutSummary.containsKey(id)){
                checkOutSummary.replace(id, checkoutitem);
            } else {
                if (quantity == 0)
                    checkOutSummary.remove(id);
                else
                    checkOutSummary.put(id, checkoutitem);
            }
            items.get(id).setItemCount(quantity);
            computeTotal();
        }
    };

    private void computeTotal() {
        cart_total = 0;
        for (String key: checkOutSummary.keySet()) {
            sub_count = checkOutSummary.get(key).getQuantity();
            sub_cost = checkOutSummary.get(key).getCost();
            cart_total = cart_total + sub_cost*sub_count;
        }
        Locale locale = new Locale("en", "in");
        NumberFormat formatter = NumberFormat.getCurrencyInstance(locale);
        String moneyString = formatter.format(cart_total);
        cartTotal.setText("Cart Total: " + moneyString);
    }

    private void initialiseSearchList() {
        //categories
        list.add("Computer & Assessories");
        list.add("Computer");
        list.add("Fashion & Lifestyle");
        list.add("Fashion");
        list.add("Lifestyle");
        list.add("stationery");
        list.add("electronics");
        list.add("Daily Essentials");
        list.add("Daily");
        list.add("Essentials");

        //itemIDs
        list.add("tm001");
        list.add("tm003");
        list.add("tm015");
        list.add("tm023");
        list.add("tm043");
        list.add("tm050");
        list.add("tm080");
        list.add("tm100");
        list.add("tm108");
        list.add("tm284");
        list.add("tm785");

        //items
        list.add("Rollerball");
        list.add("Parker");
        list.add("Vector");

        list.add("LM8UU");
        list.add("linear bearings");
        list.add("bearings");

        list.add("SK8UU");
        list.add("rod holder");

        list.add("2GB");
        list.add("Ram");
        list.add("R-Pi");

        list.add("HP");
        list.add("32GB");
        list.add("Pendrive");

        list.add("Logitech");
        list.add("M235");
        list.add("Wireless Mouse");
        list.add("Mouse");

        list.add("SKEMI");
        list.add("Analogue");
        list.add("Digital");
        list.add("Watch");

        list.add("Boost");
        list.add("Malt Drink");
        list.add("Drink");
        list.add("200mL");

        list.add("Footlodge");
        list.add("Men's");
        list.add("Black");
        list.add("Casual");
        list.add("Sport Shoes");
        list.add("Shoes");

        list.add("Apsara");
        list.add("Absolute Dark");
        list.add("Apsara Absolute Dark");

        list.add("Crayola");
        list.add("Jumbo");
        list.add("Crayons");
    }
}
