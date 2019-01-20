package com.ankan.android.guessthecelebrity;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    Document htmlFile = null;
    Elements div;
    Pattern nameP;
    Pattern imgP;
    Matcher nameM;
    Matcher imgM;
    ArrayList<String> names;
    ArrayList<String> images;
    ImageView imageView;
    TextView scoreTextView;
    Button button1;
    Button button2;
    Button button3;
    Button button4;
    Bitmap myImage;
    int answer;
    int currentQuestion;
    int score;


    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            URL url;
            try {
                htmlFile = Jsoup.connect(urls[0]).get();
                div = htmlFile.select("div.image");
                return div.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream in = connection.getInputStream();

                Bitmap myBitmap = BitmapFactory.decodeStream(in);

                return  myBitmap;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public void setNewImage(String url) {
        ImageDownloader task = new ImageDownloader();
        answer = (int)(Math.random() * 4);


        try {
            myImage = task.execute(url).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        imageView.setImageBitmap(myImage);

        setOptions(button1);
        setOptions(button2);
        setOptions(button3);
        setOptions(button4);
    }

    public void buttonClicked(View view) {
        if(Integer.parseInt(view.getTag().toString()) == answer) {
            Toast.makeText(this, "CORRECT ANSWER! ", Toast.LENGTH_SHORT).show();
            score++;
        }
        else {
            Toast.makeText(this, "WRONG ANSWER! It was " + names.get(currentQuestion), Toast.LENGTH_SHORT).show();
        }

        if(currentQuestion < names.size() - 1) {
            currentQuestion++;
            setNewImage(images.get(currentQuestion));
        }
        else {
            button1.setEnabled(false);
            button2.setEnabled(false);
            button3.setEnabled(false);
            button4.setEnabled(false);
            Toast.makeText(this, "GAME OVER! ", Toast.LENGTH_LONG).show();
        }

        scoreTextView.setText(score + " / " + currentQuestion);

    }

    public void setOptions(Button button) {
        int tag =  Integer.parseInt(button.getTag().toString());
        if(tag != answer) {
            int num = (int)(Math.random() * names.size() - 1);
            while(num == currentQuestion) {
                num = (int)(Math.random() * names.size() - 1);
            }
            button.setText(names.get(num));
            Log.i(Integer.toString(tag), names.get(num));
        }
        else {
            button.setText(names.get(currentQuestion));
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        scoreTextView = findViewById(R.id.scoreTextView);
        imageView = findViewById(R.id.imageView);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);
        DownloadTask task = new DownloadTask();
        names = new ArrayList<>();
        images = new ArrayList<>();
        String result;
        currentQuestion = 0;
        score = 0;

        try {
            result = task.execute("http://www.posh24.se/kandisar").get();
            nameP = Pattern.compile("alt=\"(.*?)\"");
            imgP = Pattern.compile("img src=\"(.*?)\"");
            nameM = nameP.matcher(result);
            imgM = imgP.matcher(result);
            while(nameM.find() && imgM.find()) {
                names.add(nameM.group(1));
                images.add(imgM.group(1));
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        scoreTextView.setText(score + " / " + currentQuestion);

        setNewImage(images.get(currentQuestion));

    }
}
