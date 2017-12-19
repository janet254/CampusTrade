package com.janet.campustrade;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SupplierHomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private List<Item> itemList = new ArrayList<>();
    SharedPreferences sharedPreferences;
    TextView email;
    TextView name;
    ImageView dp;
    RecyclerView recyclerView;
    String user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supplier_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View view = navigationView.getHeaderView(0);
        email = (TextView) view.findViewById(R.id.drawerEmail);
        name = (TextView) view.findViewById(R.id.drawerUsername);
        dp = (ImageView) view.findViewById(R.id.drawerProfile);

        recyclerView = (RecyclerView) findViewById(R.id.homeRecyclerView);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new
                RecyclerTouchListener
                .OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Item itemNext = itemList.get(position);
                Intent viewItem = new Intent(getApplicationContext(), EditItemActivity.class);
                viewItem.putExtra("itemId", itemNext.getItemId());
                startActivity(viewItem);
            }
        }));

        prepareItemData();

        sharedPreferences = this.getSharedPreferences("LoginPreferences", Context.MODE_PRIVATE);
        user = sharedPreferences.getString("user", "DEFAULT");
        String URL = "http://campustrade.000webhostapp.com/user.php?apicall=get";
        String TAG = "Profile";

        StringRequest prof = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if(jsonObject.optBoolean("error") == false){
                        JSONArray jsonArray = jsonObject.optJSONArray("user");
                        JSONObject profile = jsonArray.getJSONObject(0);
                        email.setText(profile.optString("email"));
                        name.setText(profile.optString("firstName") +" "+profile.optString
                                ("lastName"));
                        imageLoader(profile.optString("prof"));
                    }else{
                        Toast.makeText(SupplierHomeActivity.this, "Error: "+jsonObject.optString
                                ("message"), Toast.LENGTH_LONG);
                        volleyClearCache();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("Error" +error.getMessage());
                volleyClearCache();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("userID", user);
                return params;
            }
        };
        Singleton.getInstance(getApplicationContext()).addToRequestQueue(prof, TAG);
    }


    private void imageLoader(String url){
        ImageLoader imageLoader = Singleton.getInstance(getApplicationContext()).getImageLoader();
        imageLoader.get(url, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                if(response.getBitmap() != null){
                    dp.setImageBitmap(response.getBitmap());
                }
            }
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TAG", "Error: "+error.getMessage());
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            // profile
            Intent profile = new Intent(getApplicationContext(), ProfileActivity.class);
            startActivity(profile);
        } else if (id == R.id.nav_logout) {
            sharedPreferences.edit().clear().apply();
            Intent logout = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(logout);
        } else if (id == R.id.nav_orders) {
            Intent order = new Intent(getApplicationContext(), OrdersActivity.class);
            startActivity(order);
        } else if (id == R.id.nav_payments) {
            Intent payment = new Intent(getApplicationContext(), PaymentsActivity.class);
            startActivity(payment);
        } else if (id == R.id.nav_addItem){
            Intent addItem = new Intent(this, AddItemActivity.class);
            startActivity(addItem);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void prepareItemData(){
        String URL = "http://campustrade.000webhostapp.com/item.php?apicall=vendor";
        String TAG = "ALL";

        StringRequest all = new StringRequest(Request.Method.POST, URL, new Response
                .Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Message", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.optBoolean("error") == false) {
                        JSONArray jsonArray = jsonObject.getJSONArray("item");
                        Item item;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                            String id = jsonObject1.optString("itemID");
                            String name = jsonObject1.optString("name");
                            String description = jsonObject1.optString("desc");
                            String image = jsonObject1.optString("image");
                            String cost = jsonObject1.optString("cost");
                            item = new Item(id, name, description, image, cost);
                            itemList.add(item);
                        }
                        HomeAdapter homeAdapter = new HomeAdapter(getApplicationContext(), itemList);
                        recyclerView.setAdapter(homeAdapter);
                    } else {
                        Toast.makeText(getApplicationContext(), "Items not found!!", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("Error" + error.getMessage());
                volleyClearCache();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("userID", user);
                return params;
            }
        };

        Singleton.getInstance(getApplicationContext()).addToRequestQueue(all, TAG);
    }

    public void volleyInvalidateCache(String url){
        Singleton.getInstance(getApplicationContext()).getRequestQueue().getCache().invalidate(url, true);
    }

    public void volleyDeleteCache(String url){
        Singleton.getInstance(getApplicationContext()).getRequestQueue().getCache().remove(url);
    }

    public void volleyClearCache(){
        Singleton.getInstance(getApplicationContext()).getRequestQueue().getCache().clear();
    }
}
