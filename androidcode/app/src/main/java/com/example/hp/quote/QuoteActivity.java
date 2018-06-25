package com.example.hp.quote;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.icu.util.Calendar;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.hp.quote.QuoteActivity.img;
import static com.example.hp.quote.QuoteActivity.msg;

public class QuoteActivity extends AppCompatActivity {


    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListner;
    public static ImageView img;
    public static String msg=null,url=null;

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListner);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Toast.makeText(this,"you clicked "+item.getTitle(),Toast.LENGTH_SHORT).show();

        switch (item.getItemId()){
            case R.id.menu_logout:
                FirebaseMessaging.getInstance().unsubscribeFromTopic("quotes");
                mAuth.signOut();
                break;
            case R.id.menu_about:
                Intent i = new Intent(this,About.class);
                startActivity(i);
                break;
            case R.id.menu_download:
                Toast.makeText(this,"download...",Toast.LENGTH_SHORT).show();
                break;
            case R.id.menu_profile:
                Intent i1 = new Intent(this,Profile.class);
                startActivity(i1);
                break;
            case R.id.menu_settings:
                Toast.makeText(this,"menu settings...",Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quote);

        //send();

        if(isNetworkAvailable()){
            Toast.makeText(this,"sec activity with internet",Toast.LENGTH_SHORT).show();
        }
        else {
            AlertDialog.Builder alert = new AlertDialog.Builder(QuoteActivity.this);
            alert.setMessage("Oops, your internet connection seems to be off");
            alert.setPositiveButton("OK",new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface alert, int which) {
                    // TODO Auto-generated method stub
                    //Do something
                    alert.dismiss();
                    moveTaskToBack(true);
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(1);
                }
            });
            alert.show();
        }

        mAuth = FirebaseAuth.getInstance();
        img = (ImageView)findViewById(R.id.img);

        FirebaseMessaging.getInstance().subscribeToTopic("quotes");
        /*if(getIntent().getExtras() != null) {
            if(getIntent().getExtras().get("message") != null){
                SharedPreferences.Editor editor= pref.edit();
                editor.putString("message",getIntent().getExtras().get("message").toString());
                editor.putString("url",getIntent().getExtras().get("url").toString());
                editor.commit();
            }
        }*/

        if(MyFirebaseMessagingService.pref != null){
            msg = MyFirebaseMessagingService.pref.getString("message",null);
            url = MyFirebaseMessagingService.pref.getString("url",null);
        }
        else {
            msg=url=null;
        }
        if(msg!=null&&url!=null)
        {
            //Toast.makeText(this,"key received first:"+msg,Toast.LENGTH_SHORT).show();
            //Toast.makeText(this,"key received second:"+url,Toast.LENGTH_SHORT).show();
            new DownloadImageTask(getApplicationContext()).execute(url);
        }
        else {
            //Toast.makeText(this,"Nullll////",Toast.LENGTH_SHORT).show();
            img.setImageResource(R.drawable.images);
        }

        mAuthListner = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null){
                    startActivity(new Intent(QuoteActivity.this,MainActivity.class));
                }
            }
        };

    }

    public void send(){
        Intent r = new Intent(this,QuoteActivity.class);
        r.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent p = PendingIntent.getActivity(this,(int) System.currentTimeMillis()
        ,r,0);

        Notification n = new Notification.InboxStyle(new Notification.Builder(getApplicationContext())
        .setTicker("hiii")
        .setSmallIcon(R.drawable.doller)
        .setWhen(System.currentTimeMillis())
        .setContentTitle("msggggg")
        .setContentText("vvhbgjhbjkhk\n1234fhjjjm")
        .setNumber(4)
        .setContentIntent(p))
                .addLine("Line 1")
                .addLine("Line 2")
                .addLine("Line 3")
                .setBigContentTitle("expanded")
                .setSummaryText("sum")
                .build();

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        nm.notify(0, n);


    }

    public static Bitmap drawTextToBitmap(Context mContext, Bitmap bitmap, String mText) {
        try {
            Resources resources = mContext.getResources();
            float scale = resources.getDisplayMetrics().density;
            int scaledSize = mContext.getResources().getDimensionPixelSize(R.dimen.myFontSize);
           // Bitmap bitmap = BitmapFactory.decodeResource(resources, resourceId);

            android.graphics.Bitmap.Config bitmapConfig =   bitmap.getConfig();
            // set default bitmap config if none
            if(bitmapConfig == null) {
                bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
            }
            // resource bitmaps are imutable,
            // so we need to convert it to mutable one
            bitmap = bitmap.copy(bitmapConfig, true);
            //bitmap = processingBitmap_Brightness(bitmap);

            Canvas canvas = new Canvas(bitmap);
            // new antialised Paint
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            //paint.setAlpha(100);
            // text color - #3D3D3D
            paint.setColor(Color.WHITE);
            // text size in pixels
            paint.setTextSize(scaledSize);
            // text shadow
            paint.setShadowLayer(1f, 0f, 1f, Color.DKGRAY);

            // draw text to the Canvas center
            Rect bounds = new Rect();
            paint.getTextBounds(mText, 0, mText.length(), bounds);
            int x = (bitmap.getWidth() - bounds.width())/6;
            int y = (bitmap.getHeight() + bounds.height())/5;

            List<String> arr = splitString(mText,30);

            int x1 = 100, y1 = 230;
            for (String s :arr) {
                //Toast.makeText(mContext,""+s,Toast.LENGTH_SHORT).show();
                canvas.drawText(s, x1, y1 , paint);
                y1 = y1 + 80;
            }



            return bitmap;
        } catch (Exception e) {
            // TODO: handle exception

            return null;
        }
    }

    public static  List<String> splitString(String msg, int lineSize){
        List<String> res = new ArrayList<>();

        Pattern p = Pattern.compile("\\b.{1," + (lineSize-1) + "}\\b\\W?");
        Matcher m = p.matcher(msg);

        while (m.find()){
            res.add(m.group());
        }

        return res;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private static Bitmap processingBitmap_Brightness(Bitmap src){
        int brightnessValue = 0;
        Bitmap dest = Bitmap.createBitmap(
                src.getWidth(), src.getHeight(), src.getConfig());

        for(int x = 0; x < src.getWidth(); x++){
            for(int y = 0; y < src.getHeight(); y++){
                int pixelColor = src.getPixel(x, y);
                int pixelAlpha = Color.alpha(pixelColor);

                int pixelRed = Color.red(pixelColor) + brightnessValue;
                int pixelGreen = Color.green(pixelColor) + brightnessValue;
                int pixelBlue = Color.blue(pixelColor) + brightnessValue;

                if(pixelRed > 255){
                    pixelRed = 255;
                }else if(pixelRed < 0){
                    pixelRed = 0;
                }

                if(pixelGreen > 255){
                    pixelGreen = 255;
                }else if(pixelGreen < 0){
                    pixelGreen = 0;
                }

                if(pixelBlue > 255){
                    pixelBlue = 255;
                }else if(pixelBlue < 0){
                    pixelBlue = 0;
                }

                int newPixel = Color.argb(
                        pixelAlpha, pixelRed, pixelGreen, pixelBlue);

                dest.setPixel(x, y, newPixel);

            }
        }
        return dest;
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }
}


class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

    Context c;

    public DownloadImageTask(Context c) {
        this.c = c;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        Bitmap bmp =QuoteActivity.drawTextToBitmap(c,result,QuoteActivity.msg);

        QuoteActivity.img.setImageBitmap(bmp);
    }
}