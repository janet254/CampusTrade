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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UpdateProfileActivity extends AppCompatActivity {
    private static final String URL = "http://campustrade.000webhostapp.com/user.php?apicall=update";
    ProgressDialog progressDialog;
    SharedPreferences sharedPreferences;
    private String userID;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String uni;
    private String image;
    private int vendor;
    private String newFirstName;
    private String newLastName;
    private String newEmail;
    private String newPhone;
    private String newUni;
    ImageView imageView;
    EditText editFirstName;
    EditText editLastName;
    EditText editEmail;
    EditText editPhone;
    EditText editUni;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressDialog = new ProgressDialog(this);
        editFirstName = (EditText) findViewById(R.id.firstName);
        editLastName = (EditText) findViewById(R.id.lastName);
        editEmail = (EditText) findViewById(R.id.userEmail);
        editPhone = (EditText) findViewById(R.id.userPhone);
        editUni = (EditText) findViewById(R.id.userUniversity);
        imageView = (ImageView) findViewById(R.id.imageView);

        sharedPreferences = this.getSharedPreferences("ProfilePreferences", Context.MODE_PRIVATE);
        userID = sharedPreferences.getString("userID", "DEFAULT");
        firstName = sharedPreferences.getString("firstName", "DEFAULT");
        lastName = sharedPreferences.getString("lastName", "DEFAULT");
        email = sharedPreferences.getString("email", "DEFAULT");
        phone = sharedPreferences.getString("phone", "DEFAULT");
        uni = sharedPreferences.getString("uni", "DEFAULT");
        vendor = sharedPreferences.getInt("vendor", 0);
        image = sharedPreferences.getString("image", "DEFAULT");

        editFirstName.setText(firstName);
        editLastName.setText(lastName);
        editEmail.setText(email);
        editPhone.setText(phone);
        editUni.setText(uni);
        imageLoader(image);

        Button update = (Button) findViewById(R.id.updateProfileButton);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update();
            }
        });
    }

    private void imageLoader(String url){
        ImageLoader imageLoader = Singleton.getInstance(getApplicationContext()).getImageLoader();
        imageLoader.get(url, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                if(response.getBitmap() != null){
                    imageView.setImageBitmap(response.getBitmap());
                }
            }
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TAG", "Error: "+error.getMessage());
            }
        });
    }

    public void update(){
        newFirstName = editFirstName.getText().toString().trim();
        newLastName = editLastName.getText().toString().trim();
        newEmail = editEmail.getText().toString().trim();
        newPhone = editPhone.getText().toString().trim();
        newUni = editUni.getText().toString().trim();

        String REQUEST_TAG ="Update Profile";

        progressDialog.setMessage("Updating...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Success", "Success Updating");
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.optBoolean("error")== false) {
                        progressDialog.hide();
                        if (vendor == 0) {
                            Intent home = new Intent(getApplicationContext(), HomeActivity.class);
                            startActivity(home);
                        } else {
                            Intent home = new Intent(getApplicationContext(), SupplierHomeActivity
                                    .class);
                            startActivity(home);
                        }
                    } else {
                        Toast.makeText(UpdateProfileActivity.this, "Profile not updated!!" +
                                        jsonObject.optString("message"),
                                Toast.LENGTH_SHORT).show();
                        progressDialog.hide();
                        volleyClearCache();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
                progressDialog.hide();
                volleyClearCache();
            }
        }){@Override
        protected Map<String, String> getParams(){
            Map<String, String> params = new HashMap<>();
            params.put("userID", userID);
            params.put("firstName", newFirstName);
            params.put("lastName", newLastName);
            params.put("email", newEmail);
            params.put("phone", newPhone);
            params.put("uni", newUni);
            return params;
        }
        };

        Singleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest, REQUEST_TAG);

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
