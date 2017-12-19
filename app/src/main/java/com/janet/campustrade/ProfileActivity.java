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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    ProgressDialog progressDialog;
    private static final String URL = "http://campustrade.000webhostapp.com/user.php?apicall=get";
    private String userID;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String uni;
    private String image;
    private int vendor;
    ImageView dp;
    SharedPreferences sharedPreferences;
    SharedPreferences sharedPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPreference = this.getSharedPreferences("LoginPreferences", Context.MODE_PRIVATE);
        userID = sharedPreference.getString("user", "DEFAULT");
        vendor = sharedPreference.getInt("vendor", 0);

        dp = (ImageView) findViewById(R.id.imageView);
        progressDialog = new ProgressDialog(this);

        updateProfile();

        Button update = (Button) findViewById(R.id.viewProfileButton);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent update = new Intent(getApplicationContext(), UpdateProfileActivity.class);
                startActivity(update);
            }
        });

    }

    public void setText(){
        TextView fName = (TextView) findViewById(R.id.firstName);
        TextView lName = (TextView) findViewById(R.id.lastName);
        TextView userEmail = (TextView) findViewById(R.id.userEmail);
        TextView userPhone = (TextView) findViewById(R.id.userPhone);
        TextView userUni = (TextView) findViewById(R.id.userUniversity);

        fName.setText("First Name: "+firstName);
        lName.setText("Last Name: "+lastName);
        userEmail.setText("Email: "+email);
        userPhone.setText("Phone: "+phone);
        userUni.setText("University: "+uni);

        sharedPreferences = getSharedPreferences("ProfilePreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("firstName", firstName);
        editor.putString("lastName", lastName);
        editor.putString("phone", phone);
        editor.putString("email", email);
        editor.putString("uni", uni);
        editor.putString("userID", userID);
        editor.putInt("vendor", vendor);
        editor.putString("image", image);
        editor.apply();

        //userPassword.setText("Password: "+password);
    }

    public void updateProfile(){
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        String TAG = "Profile";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Success", "Records received");
                try {
                    JSONObject result = new JSONObject(response);
                    if (result.optBoolean("error") == false) {
                        JSONArray jsonArray = result.getJSONArray("user");
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        firstName = jsonObject.optString("firstName");
                        lastName = jsonObject.optString("lastName");
                        email = jsonObject.optString("email");
                        phone = jsonObject.optString("phone");
                        uni = jsonObject.optString("university");
                        image = jsonObject.optString("prof");
                        imageLoader(image);
                        setText();
                        progressDialog.hide();
                    } else {
                        Toast.makeText(ProfileActivity.this, "User data cannot be retrieved!!"
                                +result.optString("message"), Toast.LENGTH_SHORT).show();
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
            return params;
        }
        };

        Singleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest, TAG);
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

    public void changeDP(View view){
        Intent intent = new Intent(getApplicationContext(), ProfilePicActivity.class);
        intent.putExtra("image", image);
        startActivity(intent);
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
