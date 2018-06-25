package com.example.hp.quote;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class Profile extends AppCompatActivity {

    TextView t1,t2;
    ImageView img1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        t1 = (TextView)findViewById(R.id.textView1);
        t2 = (TextView)findViewById(R.id.textView2);
        Toast.makeText(this,"profile...",Toast.LENGTH_SHORT).show();

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if (acct != null) {
            String personName = acct.getDisplayName();
            String personGivenName = acct.getGivenName();
            String personFamilyName = acct.getFamilyName();
            t1.setText(personName+" "+personFamilyName);
            String personEmail = acct.getEmail();
            t2.setText(personEmail);
            String personId = acct.getId();
            Uri personPhoto = acct.getPhotoUrl();
            try {
                InputStream input = getContentResolver().openInputStream(personPhoto);
                Bitmap mIcon11 = BitmapFactory.decodeStream(input);
                img1.setImageBitmap(mIcon11);
            }catch (Exception e){
                Log.e("TAAGGG",e.getMessage());
            }
        }
    }

}
