package com.janet.campustrade;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AddItemActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    ImageView imageView;
    private String user;
    ProgressDialog progressDialog;
    String category;
    Spinner spinner;
    Bitmap bitmap;
    SharedPreferences sharedPreferences;
    EditText editName;
    EditText editDesc;
    EditText editPrice;
    EditText editQuantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressDialog = new ProgressDialog(this);
        sharedPreferences = this.getSharedPreferences("LoginPreferences", Context.MODE_PRIVATE);
        user = sharedPreferences.getString("user", "DEFAULT");

        editName = (EditText) findViewById(R.id.itemName);
        editDesc = (EditText) findViewById(R.id.itemDescription);
        editPrice = (EditText) findViewById(R.id.itemPrice);
        editQuantity = (EditText) findViewById(R.id.itemQuantity);

        imageView = (ImageView) findViewById(R.id.itemImageView);

        spinner = (Spinner) findViewById(R.id.catSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array
                .category, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        Button takePic = (Button) findViewById(R.id.takePhoto);
        takePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePhoto, 0);
            }
        });

        Button gallery = (Button)findViewById(R.id.takeGalleryPhoto);
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media
                        .EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, 1);
            }
        });

        Button upload = (Button) findViewById(R.id.buttonUploadItem);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage(bitmap);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturned){
        super.onActivityResult(requestCode, resultCode, imageReturned);
        switch(requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturned.getData();
                    //imageView.setImageURI(selectedImage);
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                        imageView.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 1:
                if (resultCode == RESULT_OK) {
                    Uri selectedImage = imageReturned.getData();
                    //imageView.setImageURI(selectedImage);
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                        imageView.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    public byte[] getFileFromDrawable(Bitmap bitmap){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private void uploadImage(final Bitmap bitmap){
        final String tag = "Trial";
        String url = "http://campustrade.000webhostapp.com/item.php?apicall=add";
        progressDialog.setMessage("Uploading image...");
        progressDialog.show();

        final String name = editName.getText().toString().trim();
        final String description = editDesc.getText().toString().trim();
        final String price = editPrice.getText().toString().trim();
        final String quantity = editQuantity.getText().toString().trim();

        VolleyMultiRequest volleyMultiRequest = new VolleyMultiRequest(Request.Method.POST, url, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                try {
                    JSONObject jsonObject = new JSONObject(new String(response.data));
                    if(jsonObject.optBoolean("error") == false){
                        Intent intent = new Intent(getApplicationContext(), SupplierHomeActivity.class);
                        startActivity(intent);
                        progressDialog.hide();
                    } else {
                        Toast.makeText(AddItemActivity.this, "response" +jsonObject.optString("message"), Toast.LENGTH_LONG).show();
                        progressDialog.hide();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("Error: "+error.getMessage());
                progressDialog.hide();
            }
        }){@Override
            protected Map<String, String> getParams(){
            Map<String, String> params = new HashMap<>();
            params.put("itemName", name);
            params.put("description", description);
            params.put("cost", price);
            params.put("quantity", quantity);
            params.put("category", category);
            params.put("userID", user);
            return params;
        }
            protected Map<String, DataPart> getByteData(){
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                params.put("image", new DataPart(imagename + ".png", getFileFromDrawable
                        (bitmap)));
                return params;
            }
        };
        volleyMultiRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        Volley.newRequestQueue(this).add(volleyMultiRequest);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        switch (spinner.getSelectedItem().toString()){
            case "Accessories":
                category = "1";
                break;
            case "Art":
                category = "2";
                break;
            case "Books":
                category = "3";
                break;
            case "Clothing":
                category = "4";
                break;
            case "Electronics":
                category = "5";
                break;
            case "Food Stuff":
                category = "6";
                break;
            case "Furniture":
                category = "7";
                break;
            case "Groceries":
                category = "8";
                break;
            case "Shoes":
                category = "9";
                break;
            case "Other":
                category = "10";
                break;
            default:
                category = "0";
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
