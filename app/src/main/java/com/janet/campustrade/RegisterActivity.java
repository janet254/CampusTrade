package com.janet.campustrade;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private String userType;
    ProgressDialog progressDialog;
    EditText editFirstName;
    EditText editLastName;
    EditText editPhone;
    EditText editPassword;
    EditText editEmail;
    EditText editUniversity;
    String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressDialog = new ProgressDialog(this);
        editFirstName = (EditText) findViewById(R.id.firstName);
        editLastName = (EditText) findViewById(R.id.lastName);
        editEmail = (EditText) findViewById(R.id.userEmail);
        editPassword = (EditText) findViewById(R.id.userPassword);
        editUniversity = (EditText) findViewById(R.id.userUniversity);
        editPhone = (EditText) findViewById(R.id.userPhone);

        Button register = (Button) findViewById(R.id.registrationButton);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });
    }

    public void onSelectUserType(View view){
        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()){
            case R.id.userConsumer:
                if(checked){
                    userType = "0";
                }
                break;
            case R.id.userSupplier:
                if(checked){
                    userType ="1";
                }
        }
    }

    private void register(){
        String URL = "http://campustrade.000webhostapp.com/user.php?apicall=create";

        final String firstName = editFirstName.getText().toString().trim();
        final String lastName = editLastName.getText().toString().trim();
        final String email = editEmail.getText().toString().trim();
        final String password = editPassword.getText().toString().trim();
        final String university = editUniversity.getText().toString().trim();
        final String phone = editPhone.getText().toString().trim();

        if(TextUtils.isEmpty(firstName)){
            editFirstName.setError("Required!!");
        }
        else if(TextUtils.isEmpty(lastName)){
            editLastName.setError("Required!!");
        }
        else if(TextUtils.isEmpty(email)){
            editEmail.setError("Required!!");
        }
        else if(TextUtils.isEmpty(university)){
            editUniversity.setError("Required!!");
        }
        else if(TextUtils.isEmpty(password) || password.length() < 8){
            editPassword.setError("Password length should be more than 8 characters");
        } else {
            progressDialog.setMessage("Registering...");
            progressDialog.show();

            StringRequest registerUser = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.optBoolean("error") == false) {
                            progressDialog.hide();
                            Intent login = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(login);
                        } else {
                            Toast.makeText(getApplicationContext(), "Error: " + jsonObject.optString
                                    ("message"), Toast.LENGTH_LONG).show();
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
                    VolleyLog.d("Error: " + error.getMessage());
                    volleyClearCache();
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("firstName", firstName);
                    params.put("lastName", lastName);
                    params.put("email", email);
                    params.put("password", password);
                    params.put("uni", university);
                    params.put("phone", phone);
                    params.put("userType", userType);
                    return params;
                }
            };

            registerUser.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy
                    .DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            Singleton.getInstance(getApplicationContext()).addToRequestQueue(registerUser,
                    "UserRegistration");
        }
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
