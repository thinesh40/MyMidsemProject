package com.xon.mymidsemproject;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class RegisterActivity extends AppCompatActivity {
    EditText ed1,ed2,ed3,ed4;
    Button register;
    Integer REQUEST_CAMERA=1, SELECT_FILE=0;
    User user;
    ImageView imgbtn2;
    final int REQUEST_CODE_GALLERY = 999;
    public static SQLiteHelper helper;
    Boolean val;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        ed1 = findViewById(R.id.emailTxt);
        ed2 = findViewById(R.id.nametxt);
        ed3 = findViewById(R.id.phonetxt);
        ed4 = findViewById(R.id.passwordtxt);
        imgbtn2 = findViewById(R.id.updbtn);
        register = findViewById(R.id.registerbtn);

        helper = new SQLiteHelper(this, "UserDB.sqlite", null, 1);

        helper.queryData("CREATE TABLE IF NOT EXISTS USER(email VARCHAR PRIMARY KEY , name VARCHAR, phone VARCHAR, password VARCHAR, image BLOB)");


        imgbtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                chooseImage();
                val = true;

            }
        });




            register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if((ed1.getText().toString().length() >0)&&(ed2.getText().toString().length() >0)&&(ed3.getText().toString().length() >0)&&(ed4.getText().toString().length() >0)) {
                        validateInput();
                        try {
                        Boolean uniqueEmail = helper.emailCheck(ed1.getText().toString());
                        if (uniqueEmail == false) {
                            RegisConfirmDialog();
                        } else {
                            Toast.makeText(RegisterActivity.this, "The Email is Already registrated, Please try again with different Email", Toast.LENGTH_SHORT).show();
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(RegisterActivity.this, "Please Fill Up the form and Upload The image", Toast.LENGTH_SHORT).show();
                    }}

            });
        }

    private Boolean validateInput() {
        String emailPattern = "[a-zA-Z0-9._-]+@[gmail]+\\.+[a-z]+";
        String emailval = ed1.getText().toString();

        if(ed4.length() < 8){
            ed4.setError("Password too short !");
            return false;
        }else if(!(emailval.matches(emailPattern))){
            ed1.setError("Invalid Email,Required Email Eg. user@gmail.com ");
            return false;
        }else {
            return true;
        }
    }



    private void chooseImage(){



            final CharSequence[] items={"Camera","Gallery", "Cancel"};

            AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
            builder.setTitle("Add Image");

            builder.setItems(items, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (items[i].equals("Camera")) {

                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, REQUEST_CAMERA);

                    } else if (items[i].equals("Gallery")) {

                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/*");
                        //startActivityForResult(intent.createChooser(intent, "Select File"), SELECT_FILE);
                        startActivityForResult(intent, SELECT_FILE);

                    } else if (items[i].equals("Cancel")) {
                        dialogInterface.dismiss();
                    }
                }
            });
            builder.show();


    }

    public static byte[] imageViewToByte(ImageView image) {
        Bitmap bitmap = ((BitmapDrawable)image.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_CODE_GALLERY){
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_GALLERY);
            }
            else {
                Toast.makeText(getApplicationContext(), "You don't have permission to access file location!", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public  void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode,data);

        if(resultCode== Activity.RESULT_OK){

            if(requestCode==REQUEST_CAMERA){

                Bundle bundle = data.getExtras();
                final Bitmap bmp = (Bitmap) bundle.get("data");

                imgbtn2.setImageBitmap(bmp);
                imgbtn2.setMinimumWidth(1280);
                imgbtn2.setMinimumHeight(960);


            }else if(requestCode==SELECT_FILE){

                Uri uri = data.getData();
                imgbtn2.setImageURI(uri);
                InputStream inputStream = null;
                try {
                    inputStream = getContentResolver().openInputStream(uri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                imgbtn2.setImageBitmap(bitmap);
            }

        }
    }

    private void RegisConfirmDialog() {
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(this.getResources().getString(R.string.confirmRegis));

        alertDialogBuilder
                .setMessage(this.getResources().getString(R.string.confirmRegisMsge))
                .setCancelable(false)
                .setPositiveButton(this.getResources().getString(R.string.yesbutton1), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        helper.insertData(
                                ed1.getText().toString().trim(),
                                ed2.getText().toString().trim(),
                                ed3.getText().toString().trim(),
                                ed4.getText().toString().trim(),
                                imageViewToByte(imgbtn2));


                        Toast.makeText(getApplicationContext(), "Registrated successfully!", Toast.LENGTH_SHORT).show();

                        Intent back = new Intent(RegisterActivity.this,LoginActivity.class);
                        startActivity(back);
                    }

                })
                .setNegativeButton(this.getResources().getString(R.string.nobutton1), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        android.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }




}
