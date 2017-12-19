package com.janet.campustrade;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class LoginActivity extends AppCompatActivity {
    EditText eEmail;
    EditText ePassword;
    ProgressDialog progressDialog;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressDialog = new ProgressDialog(this);
        eEmail = (EditText) findViewById(R.id.loginEmail);
        ePassword = (EditText) findViewById(R.id.loginPassword);

        //setting the login activity button
        Button login = (Button) findViewById(R.id.loginButton);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        //setting the register activity button
        Button register = (Button) findViewById(R.id.registerButton);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //new page
                Intent registration = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(registration);
            }
        });
    }


    private void login(){
        String URL = "http://campustrade.000webhostapp.com/user.php?apicall=auth";
        String TAG = "Login";

        sharedPreferences = getSharedPreferences("LoginPreferences", Context.MODE_PRIVATE);
        final String email = eEmail.getText().toString().trim();
        final String password = ePassword.getText().toString().trim();
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        //input validation
        if (TextUtils.isEmpty(email)){
            eEmail.setError("Email is required");
        }
        else if (TextUtils.isEmpty(password)){
            ePassword.setError("Password is required");
        } else {
            progressDialog.setMessage("Authenticating...");
            progressDialog.show();

            //sending data to database
            StringRequest loginUser = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.optBoolean("error") == false) {
                            editor.putString("user", jsonObject.optString("user"));
                            editor.putInt("vendor", jsonObject.optInt("vendor"));
                            editor.apply();
                            progressDialog.hide();
                            if (jsonObject.optInt("vendor") == 0) {
                                Intent home = new Intent(getApplicationContext(), HomeActivity.class);
                                startActivity(home);
                            } else {
                                Intent home = new Intent(getApplicationContext(), SupplierHomeActivity
                                        .class);
                                startActivity(home);
                            }
                        } else {
                            progressDialog.hide();
                            Toast.makeText(LoginActivity.this, "Error:" + jsonObject.optString
                                    ("message"), Toast.LENGTH_LONG).show();
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
                    progressDialog.hide();
                    volleyClearCache();
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("email", email);
                    params.put("password", password);
                    return params;
                }
            };
            loginUser.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            Singleton.getInstance(getApplicationContext()).addToRequestQueue(loginUser, TAG);
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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
}
