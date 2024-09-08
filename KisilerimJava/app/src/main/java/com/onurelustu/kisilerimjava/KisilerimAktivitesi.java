package com.onurelustu.kisilerimjava;

import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;
import com.onurelustu.kisilerimjava.databinding.ActivityKisilerimAktivitesiBinding;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class KisilerimAktivitesi extends AppCompatActivity {

    private ActivityKisilerimAktivitesiBinding binding;
    ActivityResultLauncher<Intent>activityResultLauncher;
    ActivityResultLauncher<String>permissionLauncher;
    Bitmap selectedImage;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityKisilerimAktivitesiBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        registerLauncher();

        database = this.openOrCreateDatabase("Rehber",MODE_PRIVATE,null);

        Intent intent =getIntent();
        String info = intent.getStringExtra("info");
        if (info.equals("new")){
            // new rehber
            binding.nameTxt.setText("");
            binding.datetxt.setText("");
            binding.phonetxt.setText("");
            binding.imgView.setImageResource(R.drawable.slcimg);
        }else {
            int rehberId = intent.getIntExtra("rehberId",1);
            binding.button.setVisibility(View.INVISIBLE);

            try {

                Cursor cursor = database.rawQuery(  "SELECT * FROM rehber WHERE id = ?", new  String[]{String.valueOf(rehberId)});
                int rehberNameIx = cursor.getColumnIndex("rehbername");
                int phoneNumberNameIx= cursor.getColumnIndex("phoneNumberName");
                int dateIx = cursor.getColumnIndex("date");
                int imageIx = cursor.getColumnIndex("image");

                while (cursor.moveToNext()){
                    binding.nameTxt.setText(cursor.getString(rehberNameIx));
                    binding.phonetxt.setText(cursor.getString(phoneNumberNameIx));
                    binding.datetxt.setText(cursor.getString(dateIx));
                    byte[] bytes = cursor.getBlob(imageIx);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    binding.imgView.setImageBitmap(bitmap);


                }


            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }


    public void save(View view ) {

        String name = binding.nameTxt.getText().toString();
        String number =binding.phonetxt.getText().toString();
        String date = binding.datetxt.getText().toString();

        Bitmap smallImage = makeSmallerImage(selectedImage,300);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);
        byte[] byteArray = outputStream.toByteArray();

        try {


            database.execSQL("CREATE TABLE IF NOT EXISTS rehber (id INT PRIMARY KEY, adsoyad VARCHAR, phonenumber INT, date INT, image BLOB)");

            String sqlString =  "INSERT INTO rehber(adsoyad, phonenumber, date, image)VALUES(?,?,?,?)";
            SQLiteStatement sqLiteStatement =database.compileStatement(sqlString);
            sqLiteStatement.bindString(1,name);
            sqLiteStatement.bindString(2,number);
            sqLiteStatement.bindString(3,date);
            sqLiteStatement.bindBlob(4,byteArray);
            sqLiteStatement.execute();


        }catch (Exception e){
            e.printStackTrace();
        }

        Intent intent = new Intent(KisilerimAktivitesi.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);



    }

    public  Bitmap makeSmallerImage(Bitmap image,int maximumSize){
        int width =image.getWidth();
        int height =image.getHeight();

        float bitmapRatio= (float) width / (float) height;
        if (bitmapRatio>1){
            //landscape image
            width= maximumSize;
            height = (int) ((int) width / bitmapRatio);
        }else {
            //portrait image
            height=maximumSize;
            width= (int) (height*bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image,width,height,true );

    }

    public void selectImage(View view){

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
            //Android 33+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)!= PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_MEDIA_IMAGES)){

                    Snackbar.make(view,"Galeriye gitmek için izin lazım",Snackbar.LENGTH_INDEFINITE).setAction("İzin Ver", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //İZİN İSTEMEK
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                        }
                    }).show();


                }else{
                    //İZİN İSTEMEK
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);

                }


            }else {
                //GALERİ
                Intent intentToGaleri = new Intent(Intent.ACTION_PICK, EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGaleri);
            }
        }else{
            //Android 32-
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){

                    Snackbar.make(view,"Galeriye gitmek için izin lazım",Snackbar.LENGTH_INDEFINITE).setAction("İzin Ver", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //İZİN İSTEMEK
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                        }
                    }).show();


                }else{
                    //İZİN İSTEMEK
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);

                }


            }else {
                //GALERİ
                Intent intentToGaleri = new Intent(Intent.ACTION_PICK, EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGaleri);
            }
        }



    }

    private void registerLauncher(){
        activityResultLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode()==RESULT_OK){
                    Intent intentFromResult= result.getData();
                    if (intentFromResult!=null){
                        Uri imageData=intentFromResult.getData();
                        //binding.imgView.setImageURI(imageData);

                        try {
                            if(Build.VERSION.SDK_INT>=28){
                                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(),imageData);
                                selectedImage = ImageDecoder.decodeBitmap(source);
                                binding.imgView.setImageBitmap(selectedImage);

                            }else{
                                selectedImage=MediaStore.Images.Media.getBitmap(KisilerimAktivitesi.this.getContentResolver(),imageData);
                                binding.imgView.setImageBitmap(selectedImage);
                            }


                        }catch (Exception e){
                            e.printStackTrace();


                        }

                    }

                }

            }
        });

        permissionLauncher=registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if(result){
                    //İzin Verildi
                    Intent intentToGaleri = new Intent(Intent.ACTION_PICK, EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGaleri);
                }else{
                    //İzin Verilmedi
                    Toast.makeText(KisilerimAktivitesi.this,"İzin Verilmedi!",Toast.LENGTH_LONG).show();
                }

            }
        });

    }

}



