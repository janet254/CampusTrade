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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProfilePicActivity extends AppCompatActivity {
    ImageView imageView;
    private String user;
    ProgressDialog progressDialog;
    SharedPreferences sharedPreferences;
    Bitmap bitmap;
    String image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_pic);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        image = intent.getStringExtra("image");

        progressDialog = new ProgressDialog(this);
        sharedPreferences = this.getSharedPreferences("LoginPreferences", Context.MODE_PRIVATE);
        user = sharedPreferences.getString("user", "DEFAULT");
        imageView = (ImageView) findViewById(R.id.dp);
        Glide.with(this).load(image).into(imageView);

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

        Button upload = (Button) findViewById(R.id.saveImage);
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
                    imageView.setImageURI(selectedImage);
//                    try {
//                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
//                        imageView.setImageBitmap(bitmap);
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
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
        String url = "http://campustrade.000webhostapp.com/user.php?apicall=updateDP";
        progressDialog.setMessage("Uploading image...");
        progressDialog.show();

        VolleyMultiRequest volleyMultiRequest = new VolleyMultiRequest(Request.Method.POST, url, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                try {
                    JSONObject jsonObject = new JSONObject(new String(response.data));
                    if(jsonObject.optBoolean("error") == false){
                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        startActivity(intent);
                        progressDialog.hide();
                    } else {
                        Toast.makeText(getApplicationContext(), "response" +jsonObject.optString("message"), Toast
                                .LENGTH_LONG).show();
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
            params.put("userID", user);
            return params;
        }
            protected Map<String, DataPart> getByteData(){
                Map<String, DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                params.put("profilePic", new DataPart(imagename + ".png", getFileFromDrawable
                        (bitmap)));
                return params;
            }
        };

        Volley.newRequestQueue(this).add(volleyMultiRequest);
    }

}
