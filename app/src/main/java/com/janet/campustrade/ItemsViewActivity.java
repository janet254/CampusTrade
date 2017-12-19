package com.janet.campustrade;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ItemsViewActivity extends AppCompatActivity {
    String itemID;
    TextView textName;
    TextView textDescription;
    TextView textPrice;
    ImageView itemImage;
    ImageView favIcon;
    SharedPreferences sharedPreferences;
    String user;
    ProgressDialog progressDialog;
    String quantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        itemID = intent.getStringExtra("itemId");

        textName = (TextView) findViewById(R.id.itemName);
        textDescription = (TextView) findViewById(R.id.itemDescription);
        textPrice = (TextView) findViewById(R.id.price);
        favIcon = (ImageView) findViewById(R.id.addFav);

        itemImage = (ImageView) findViewById(R.id.itemImage);
        progressDialog = new ProgressDialog(this);

        sharedPreferences = this.getSharedPreferences("LoginPreferences", Context.MODE_PRIVATE);
        user = sharedPreferences.getString("user", "DEFAULT");

        getItem();

        Button cart  = (Button) findViewById(R.id.addToCartButton);
        if (quantity == "0"){
            cart.setText("Out Of Stock");
            cart.setEnabled(false);
        }
        cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToCart();
            }
        });
    }

    public void getItem() {
        String URL = "http://campustrade.000webhostapp.com/item.php?apicall=get";
        String TAG = "Item";

        StringRequest all = new StringRequest(Request.Method.POST, URL, new Response
                .Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Message", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.optBoolean("error") == false) {
                        JSONArray jsonArray = jsonObject.getJSONArray("item");
                        JSONObject jsonObject1 = jsonArray.getJSONObject(0);
                        String id = jsonObject1.optString("itemID");
                        String name = jsonObject1.optString("name");
                        String description = jsonObject1.optString("desc");
                        String image = jsonObject1.optString("image");
                        String cost = jsonObject1.optString("cost");
                        quantity = jsonObject1.optString("quantity");

                        Glide.with(getApplicationContext()).load(image).into(itemImage);
                        textName.setText(name);
                        textDescription.setText(description);
                        textPrice.setText("Kshs. "+cost);
                    } else {
                        Toast.makeText(getApplicationContext(), jsonObject
                                .optString("message"), Toast
                                .LENGTH_LONG).show();
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
                params.put("itemID", itemID);
                return params;
            }
        };

        Singleton.getInstance(getApplicationContext()).addToRequestQueue(all, TAG);
    }

    public void addToFav(View view){
        String URL = "http://campustrade.000webhostapp.com/item.php?apicall=fav";
        String TAG = "Favourites";

        StringRequest all = new StringRequest(Request.Method.POST, URL, new Response
                .Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Message", response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.optBoolean("error") == false) {
                        Toast.makeText(getApplicationContext(), jsonObject.optString("message"),
                                Toast.LENGTH_SHORT).show();
                        favIcon.setImageResource(R.drawable.favs_heart);
                    } else {
                        Toast.makeText(getApplicationContext(), jsonObject.optString("message"),
                                Toast.LENGTH_LONG)
                                .show();
                        volleyClearCache();
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
                params.put("itemID", itemID);
                params.put("userID", user);
                return params;
            }
        };

        Singleton.getInstance(getApplicationContext()).addToRequestQueue(all, TAG);
    }

    public void addToCart(){
        progressDialog.setMessage("Adding...");
        progressDialog.show();

        String URL = "http://campustrade.000webhostapp.com/order.php?apicall=addCart";

        StringRequest addCart = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if(jsonObject.optBoolean("error")== false){
                        progressDialog.hide();
                        Intent home = new Intent(getApplicationContext(), HomeActivity.class);
                        startActivity(home);
                    } else{
                        progressDialog.hide();
                        Toast.makeText(getApplicationContext(), jsonObject.optString("message"),
                                Toast.LENGTH_SHORT).show();
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
                params.put("itemID", itemID);
                params.put("userID", user);
                params.put("quantity", "1");
                return params;
            }
        };

        Singleton.getInstance(getApplicationContext()).addToRequestQueue(addCart, "ADDTOCART");
    }

    public void volleyInvalidateCache(String url) {
        Singleton.getInstance(getApplicationContext()).getRequestQueue().getCache()
                .invalidate
                        (url, true);
    }

    public void volleyDeleteCache(String url) {
        Singleton.getInstance(getApplicationContext()).getRequestQueue().getCache()
                .remove(url);
    }

    public void volleyClearCache() {
        Singleton.getInstance(getApplicationContext()).getRequestQueue().getCache()
                .clear();
    }
}
