package com.janet.campustrade;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
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

public class EditItemActivity extends AppCompatActivity {
    String itemID;
    TextView textName;
    TextView textDescription;
    TextView textPrice;
    TextView textQuantity;
    ImageView itemImage;
    SharedPreferences sharedPreferences;
    String user;
    ProgressDialog progressDialog;
    Spinner spinner;
    String category;
    ArrayAdapter<CharSequence> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        itemID = intent.getStringExtra("itemId");

        progressDialog = new ProgressDialog(this);
        textName = (TextView) findViewById(R.id.itemName);
        textDescription = (TextView) findViewById(R.id.itemDescription);
        textPrice = (TextView) findViewById(R.id.itemCost);
        textQuantity = (TextView) findViewById(R.id.itemQuantity);
        itemImage = (ImageView) findViewById(R.id.itemImage);

        getItem();

//        spinner = (Spinner) findViewById(R.id.catSpinner);
//        adapter = ArrayAdapter.createFromResource(this, R.array
//                .category, android.R.layout.simple_spinner_dropdown_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinner.setAdapter(adapter);
//        spinner.setOnItemSelectedListener(this);

        Button save = (Button) findViewById(R.id.saveChangesButton);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveItemChanges();
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
                        String quantity = jsonObject1.optString("quantity");
                        String compareValue = jsonObject1.optString("category");

                        Glide.with(getApplicationContext()).load(image).into(itemImage);
                        textName.setText(name);
                        textDescription.setText(description);
                        textPrice.setText(cost);
                        textQuantity.setText(quantity);
                        if (!compareValue.equals(null)) {
                            int spinnerPosition = adapter.getPosition(compareValue);
                            spinner.setSelection(spinnerPosition);
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), jsonObject.optString("message"),
                                Toast.LENGTH_LONG).show();
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

    public void saveItemChanges(){
        final String name = textName.getText().toString();
        final String desc = textDescription.getText().toString();
        final String quantity = textQuantity.getText().toString();
        final String price = textPrice.getText().toString();
        progressDialog.setMessage("Saving...");
        progressDialog.show();

        String URL = "http://campustrade.000webhostapp.com/item.php?apicall=update";
        StringRequest saveItem = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    progressDialog.hide();
                    Toast.makeText(getApplicationContext(), jsonObject.optString("message"), Toast.LENGTH_LONG)
                            .show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("Error" +error.getMessage());
                progressDialog.hide();
                volleyClearCache();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("itemID", itemID);
                params.put("category", category);
                params.put("name", name);
                params.put("description", desc);
                params.put("cost", price);
                params.put("quantity", quantity);
                return params;
            }
        };

        saveItem.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Singleton.getInstance(getApplicationContext()).addToRequestQueue(saveItem, "EditItem");
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

//    @Override
//    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//        switch (spinner.getSelectedItem().toString()){
//            case "Accessories":
//                category = "1";
//                break;
//            case "Art":
//                category = "2";
//                break;
//            case "Books":
//                category = "3";
//                break;
//            case "Clothing":
//                category = "4";
//                break;
//            case "Electronics":
//                category = "5";
//                break;
//            case "Food Stuff":
//                category = "6";
//                break;
//            case "Furniture":
//                category = "7";
//                break;
//            case "Groceries":
//                category = "8";
//                break;
//            case "Shoes":
//                category = "9";
//                break;
//            case "Other":
//                category = "10";
//                break;
//            default:
//                category = "0";
//                break;
//        }
//    }
//
//    @Override
//    public void onNothingSelected(AdapterView<?> adapterView) {
//
//    }

}
