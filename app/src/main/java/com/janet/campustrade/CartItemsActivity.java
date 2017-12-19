package com.janet.campustrade;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartItemsActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    ProgressDialog progressDialog;
    private List<Item> itemList = new ArrayList<>();
    String user;
    RecyclerView recyclerView;
    CartAdapter cartAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_items);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressDialog = new ProgressDialog(this);
        final Button checkOut = (Button) findViewById(R.id.checkOutButton);
        checkOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkout();
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.cartRecyclerView);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 2);
        recyclerView.setLayoutManager(layoutManager);
        cartAdapter = new CartAdapter(getApplicationContext(), itemList);
        recyclerView.setAdapter(cartAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView,
                new RecyclerTouchListener.OnItemClickListener(){
                    @Override
                    public void onItemClick(View view, int position) {
                        Item itemNext = itemList.get(position);
                        Intent viewItem = new Intent(getApplicationContext(), ItemsViewActivity.class);
                        viewItem.putExtra("itemId", itemNext.getItemId());
                        startActivity(viewItem);
                    }
                }));
        prepareItemData();
    }

    private void prepareItemData() {
        String URL = "http://campustrade.000webhostapp.com/order.php?apicall=getCart";
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
                            String quantity = jsonObject1.optString("quantity");
                            item = new Item(id, name, description, image, cost, quantity);
                            itemList.add(item);
                        }
                        cartAdapter.notifyDataSetChanged();
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

    public void checkout(){
        String URL = "http://campustrade.000webhostapp.com/order.php?apicall=checkout";
        String TAG = "ALL";

        StringRequest checkOff = new StringRequest(Request.Method.POST, URL, new Response
                .Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Message", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.optBoolean("error") == false) {
                        Intent home = new Intent(getApplicationContext(), HomeActivity.class);
                        startActivity(home);
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

        Singleton.getInstance(getApplicationContext()).addToRequestQueue(checkOff, TAG);
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
