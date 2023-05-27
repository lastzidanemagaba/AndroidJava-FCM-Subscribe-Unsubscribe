package com.test.pushnotificationproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import java.util.TimeZone;
import android.view.View;
import android.widget.Button;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "PushNotification";
    private static final String CHANNEL_ID = "101";
    private int rawOffset;
    private int gmtOffset;
    private String ZidaneTime;
    @Override


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();
        getToken();
        getTimeZone();

        Button myButton = findViewById(R.id.myButton);
        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Button click event handler
                callAPI();
            }
        });

        Button myButtonUnsubscribe = findViewById(R.id.myButtonUnsubscribe);
        myButtonUnsubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Button click event handler
                unsubscribeFromTopic();
            }
        });

        Button myButtonSubscribe = findViewById(R.id.myButtonSubscribe);
        myButtonSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Button click event handler
                subscribeToTopic();
            }
        });


    }

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                //If task is failed then
                if (!task.isSuccessful()) {
                    Log.d(TAG, "onComplete: Failed to get the Token");
                }

                //Token
                String token = task.getResult();
                Log.d(TAG, "onComplete: " + token);
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "firebaseNotifChannel";
            String description = "Receve Firebase notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }



    private void subscribeToTopic(){
        FirebaseMessaging.getInstance().subscribeToTopic("news").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //If task is failed then
                if (!task.isSuccessful()) {
                    Log.d(TAG, "onComplete: Failed to subscribe to the topic");
                }
                Log.d(TAG, "onComplete: Subscribed to the topic");
            }
        });
    }

    // unsubscribe from a topic
    private void unsubscribeFromTopic(){
        FirebaseMessaging.getInstance().unsubscribeFromTopic("news").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //If task is failed then
                if (!task.isSuccessful()) {
                    Log.d(TAG, "onComplete: Failed to unsubscribe from the topic");
                }
                Log.d(TAG, "onComplete: Unsubscribed from the topic");
            }
        });
    }

    // create function get timezone device
    private void getTimeZone() {
        TimeZone timeZone = TimeZone.getDefault();
        String timeZoneID = timeZone.getID();

        rawOffset = timeZone.getRawOffset();
        gmtOffset = rawOffset / (1000 * 60 * 60);

        // Get the current time in GMT
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, gmtOffset);

        // Format the time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        String currentTimeGMT = sdf.format(calendar.getTime());
        ZidaneTime = currentTimeGMT;

        // Print the timezone ID and current time with GMT offset
        Log.d(TAG, "Timezone ID: " + timeZoneID);
        Log.d(TAG, "GMT Offset: " + gmtOffset);
        Log.d(TAG, "Current Time (GMT): " + currentTimeGMT);
    }

    private void callAPI() {
        String apiUrl = "http://172.26.192.1:8687/testdata"; // Replace with your Go API URL


        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(apiUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json; utf-8");
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setDoOutput(true);

                    String jsonInputString = "{\"created_at\": \"" + ZidaneTime + "\", \"updated_at\": \"" + ZidaneTime + "\"}";

                    // print the jsonInputString
                    Log.d(TAG, "run: " + jsonInputString);

// Send the JSON data to the server
                    try (OutputStream outputStream = connection.getOutputStream()) {
                        byte[] input = jsonInputString.getBytes("utf-8");
                        outputStream.write(input, 0, input.length);
                    }

// Read the response
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        String jsonResponse = response.toString();
                        // Handle the JSON response
                        try {
                            JSONObject jsonObject = new JSONObject(jsonResponse);
                            // Access the JSON data and perform operations
                            // For example:
                            String message = jsonObject.getString("message");
                            Log.d(TAG, "onPostExecute: " + message);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.e("Response Code", "HTTP error code: " + responseCode);
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }
}