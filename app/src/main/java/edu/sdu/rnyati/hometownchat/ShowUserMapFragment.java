package edu.sdu.rnyati.hometownchat;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by raghavnyati on 4/6/17.
 */


public class ShowUserMapFragment extends SupportMapFragment implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    GoogleMap mMap;
    String nickname, country, url;
    double latitude, longitude;
    String selectQueryString;
    private ArrayList<UserHomeInfo> hometownList;
    Geocoder locator;
    private DatabaseAdapter dbHelper;
    private SQLiteDatabase nameDb;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(null != getArguments()){
            selectQueryString = getArguments().getString("query");
            url = getArguments().getString("url");
            Log.i("url",url);
        }
        hometownList = new ArrayList<>();
        dbHelper = (new DatabaseAdapter( getContext()));
        nameDb = dbHelper.getWritableDatabase();
        dbHelper.onCreate(nameDb);

        this.getFragmentManager().findFragmentById(R.id.map);
        this.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        locator= new Geocoder(getContext());

        getAllUsersMapMarker(getActivity(), mMap, 0);
        mMap.setOnInfoWindowClickListener(this);
    }

    @Override
    public void onInfoWindowClick(Marker marker){
        final Bundle data = new Bundle();
        data.putString("nickname", nickname);
        Intent intent = new Intent(getActivity(),ChatMessageActivity.class);
        intent.putExtras(data);
        startActivity(intent);
    }

    JSONArray mapPoints;
    int id;

    public void getAllUsersMapMarker(final Activity a, final GoogleMap mMap, int startId) {
        String url;
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                mapPoints = response;
                Log.d("rag", response.toString());

                for (int i = 0 ; i < 50 ; i++) {

                    JSONObject rec;
                    try {
                        rec = mapPoints.getJSONObject(i);
                        id = rec.getInt("id");
                        nickname = rec.getString("nickname");
                        latitude = rec.getDouble("latitude");
                        longitude = rec.getDouble("longitude");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    LatLng marker = new LatLng(latitude, longitude);
                    if(nickname!=null){
                        mMap.addMarker(new MarkerOptions().position(marker).title(nickname));
                    }
                }

                if(response.length()>0){
                    Log.i("Response: ", response.length() + "");
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getAllUsersMapMarker(a,mMap,id);
                        }
                    }, 3000);

                }
            }
        };

        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Log.d("rag", "in error");
                Log.d("rag", error.toString());
            }
        };

        if(id!=0)
            //Log.i("rag", url);
            url = this.url+ "&beforeid="+startId;
        else
            url = this.url;

        JsonArrayRequest getRequest = new JsonArrayRequest(url, success, failure);
        RequestQueue queue = Volley.newRequestQueue(a);
        queue.add(getRequest);

    }

}

