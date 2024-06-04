package com.e.weatherapp;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private TextView latitudeTextview;
    private TextView longitudeTextview;
    private TextView addressTextview;
    private TextView timeTextview;
    private TextView weatherTextview;
    String latitude = "7.0991829195973635";
    String longitude = "79.94113916082082";

    private final String url = "https://api.openweathermap.org/data/2.5/weather";
    private final String appid = "dba21fb16a81edc6b570242d00c2e393";

    String city,country ;

    DecimalFormat df = new DecimalFormat("#.##");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        latitudeTextview = findViewById(R.id.latitude_textview);
        longitudeTextview = findViewById(R.id.longitude_textview);
        addressTextview = findViewById(R.id.address_textview);
        timeTextview = findViewById(R.id.time_textview);
        weatherTextview = findViewById(R.id.weather_textview);

        latitudeTextview.setText(latitude);
        longitudeTextview.setText(longitude);

        final Handler handler = new Handler();
        final Runnable updateTimer = new Runnable() {
            @Override
            public void run() {
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                int second = calendar.get(Calendar.SECOND);
                String currentTimeString = String.format("%02d:%02d:%02d", hour, minute, second);

                timeTextview.setText(currentTimeString);

                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(updateTimer, 0);

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        String result = null;

        try {
            addresses = geocoder.getFromLocation(7.0991829195973635, 79.94113916082082, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (addresses != null && addresses.size() > 0) {
            Address address = addresses.get(0);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                sb.append(address.getAddressLine(i));
            }
            sb.append(address.getAddressLine(0));
            // sb.append(address.getSubAdminArea()).append("\n");
            //sb.append(address.getCountryCode()).append("\n");
            result = sb.toString();
            addressTextview.setText(result);

            city =address.getSubAdminArea();
            country =address.getCountryCode();

            String tempUrl = url + "?q=" + city + "," + country + "&appid=" + appid;

            StringRequest stringRequest = new StringRequest(Request.Method.GET, tempUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    String output = "";
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray jsonArray = jsonResponse.getJSONArray("weather");
                        JSONObject jsonObjectWeather = jsonArray.getJSONObject(0);
                        String description = jsonObjectWeather.getString("description");

                        JSONObject jsonObjectMain = jsonResponse.getJSONObject("main");
                        double temp = jsonObjectMain.getDouble("temp") - 273.15;
                        double feelsLike = jsonObjectMain.getDouble("feels_like") - 273.15;
                        float pressure = jsonObjectMain.getInt("pressure");
                        int humidity = jsonObjectMain.getInt("humidity");

                        JSONObject jsonObjectWind = jsonResponse.getJSONObject("wind");
                        String wind = jsonObjectWind.getString("speed");

                        JSONObject jsonObjectClouds = jsonResponse.getJSONObject("clouds");
                        String clouds = jsonObjectClouds.getString("all");

                        JSONObject jsonObjectSys = jsonResponse.getJSONObject("sys");
                        String cityName = jsonResponse.getString("name");

                        weatherTextview.setTextColor(Color.rgb(255,0,0));

                        output += "Current Weather of " + cityName
                                + "\n Temp: " + df.format(temp) + " °C"
                                + "\n Feels Like: " + df.format(feelsLike) + " °C"
                                + "\n Humidity: " + humidity + "%"
                                + "\n Description: " + description
                                + "\n Wind Speed: " + wind + "m/s"
                                + "\n Cloudiness: " + clouds + "%"
                                + "\n Pressure: " + pressure + " hPa";
                        weatherTextview.setText(output);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), error.toString().trim(), Toast.LENGTH_SHORT).show();
                    weatherTextview.setText("Error: " + error.toString().trim());
                }
            });
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            requestQueue.add(stringRequest);
        }

    }
}


