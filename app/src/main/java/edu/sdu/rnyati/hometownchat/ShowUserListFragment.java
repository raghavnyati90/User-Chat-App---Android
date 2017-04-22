package edu.sdu.rnyati.hometownchat;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by raghavnyati on 4/5/17.
 */

public class ShowUserListFragment extends Fragment{

    private ListView listView;
    private ListBaseAdapter listAdapter;
    private boolean isLoading = false;
    private static boolean isDataFromServer = true;
    private DatabaseAdapter dbHelper;
    private SQLiteDatabase nameDb;
    private MyHandler myHandler;
    private ArrayList<UserDetails> hometownList;
    View userDetails, footerView;
    private String query, url, id, nickname, country, state, city, year, longitude, latitude;
    private int leastId = 0, minId = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        dbHelper = (new DatabaseAdapter(getContext()));
        nameDb = dbHelper.getWritableDatabase();
        dbHelper.onCreate(nameDb);
        myHandler = new MyHandler();
        userDetails = inflater.inflate(R.layout.activity_show_home_town_list, container, false);
        footerView = inflater.inflate(R.layout.footer_view, container, false);
        listView = (ListView) userDetails.findViewById(R.id.listView);
        hometownList = new ArrayList<>();

        if (null != getArguments()) {
            url = getArguments().getString("url");
            query = getArguments().getString("query");
        }

        getLatestData(getSQLLastId());

        return userDetails;
    }

    public class GetDataThread {
        int pageNumber, initialCount;

        GetDataThread() {
            this.pageNumber = 1;
            this.initialCount = 0;
        }

        public void run(int leastId) {
            myHandler.sendEmptyMessage(0);
            ArrayList<UserDetails> listResult;
            if (isDataFromServer) {
                if (minId == 0) {
                    listResult = fetchMoreDataFromServer(pageNumber, leastId);
                } else {
                    listResult = fetchMoreDataFromServer(pageNumber, minId);
                }
                pageNumber++;
            } else {
                listResult = getDataFromSQL(leastId).entrySet().iterator().next().getKey();
                if (listResult.size() == 0) {
                    listResult = fetchMoreDataFromServer(pageNumber, leastId);
                } else if (listResult.size() < 25) {
                    isDataFromServer = true;
                    pageNumber = 0;
                    minId = (getDataFromSQL(leastId).entrySet().iterator().next().getValue()) + 1;
                }
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Message msg = myHandler.obtainMessage(1, listResult);
            myHandler.sendMessage(msg);
        }
    }

    public class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    listView.addFooterView(footerView);
                    break;
                case 1:
                    listAdapter.addListItemToAdapter((ArrayList<UserDetails>) msg.obj);
                    listView.removeFooterView(footerView);
                    isLoading = false;
                    break;
                default:
                    break;
            }
        }
    }

    private ArrayList<UserDetails> fetchMoreDataFromServer(int page, int leastId) {
        final ArrayList<UserDetails> lst = new ArrayList<>();
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                try {
                    if (response != null) {
                        for (int i = 0; i < response.length(); i++) {
                            UserDetails userDetails = new UserDetails();
                            ContentValues contentValues = new ContentValues();
                            JSONObject jsonObj = response.getJSONObject(i);
                            id = (jsonObj.get("id").toString());
                            nickname = (jsonObj.get("nickname").toString());
                            country = (jsonObj.get("country").toString());
                            state = (jsonObj.get("state").toString());
                            city = (jsonObj.get("city").toString());
                            year = (jsonObj.get("year").toString());
                            latitude = (jsonObj.get("latitude").toString());
                            longitude = (jsonObj.get("longitude").toString());
                            userDetails.setId(Integer.valueOf(id));
                            userDetails.setNickName(nickname);
                            userDetails.setCountry(country);
                            userDetails.setState(state);
                            userDetails.setCity(city);
                            userDetails.setYear(year);
                            userDetails.setLatitude(latitude);
                            userDetails.setLongitude(longitude);
                            contentValues.put("ID", id);
                            contentValues.put("NICKNAME", nickname);
                            contentValues.put("COUNTRY", country);
                            contentValues.put("STATE", state);
                            contentValues.put("CITY", city);
                            contentValues.put("YEAR", year);
                            contentValues.put("LONGITUDE", longitude);
                            contentValues.put("LATITUDE", latitude);

                            lst.add(userDetails);
                            nameDb.insert("HOMETOWNLOCATION_DETAILS", null, contentValues);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        };

        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        };

        String getUrl = url + "&beforeid=" + leastId + "&page=" + page;
        JsonArrayRequest getRequest = new JsonArrayRequest(getUrl, success, failure);
        VolleyQueue.instance(getActivity()).add(getRequest);
        return lst;
    }

    private void getLatestData(int lastSQLId) {
        final int maxId = lastSQLId;
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                try {
                    final int beforeId;
                    if (response != null) {
                        Log.i("response", response.toString());
                        for (int i = 0; i < response.length(); i++) {
                            ContentValues contentValues = new ContentValues();
                            UserDetails userDetails = new UserDetails();
                            JSONObject jsonObj = response.getJSONObject(i);
                            id = (jsonObj.get("id").toString());
                            nickname = (jsonObj.get("nickname").toString());
                            country = (jsonObj.get("country").toString());
                            state = (jsonObj.get("state").toString());
                            city = (jsonObj.get("city").toString());
                            year = (jsonObj.get("year").toString());
                            latitude = (jsonObj.get("latitude").toString());
                            longitude = (jsonObj.get("longitude").toString());
                            userDetails.setId(Integer.valueOf(id));
                            userDetails.setNickName(nickname);
                            userDetails.setCountry(country);
                            userDetails.setState(state);
                            userDetails.setCity(city);
                            userDetails.setYear(year);
                            userDetails.setLatitude(latitude);
                            userDetails.setLongitude(longitude);
                            contentValues.put("ID", id);
                            contentValues.put("NICKNAME", nickname);
                            contentValues.put("COUNTRY", country);
                            contentValues.put("STATE", state);
                            contentValues.put("CITY", city);
                            contentValues.put("YEAR", year);
                            contentValues.put("LONGITUDE", longitude);
                            contentValues.put("LATITUDE", latitude);

                            hometownList.add(userDetails);

                            try {
                                nameDb.insert("HOMETOWNLOCATION_DETAILS", null, contentValues);
                            }catch(SQLiteConstraintException e){}
                        }
                        if (!hometownList.isEmpty() && hometownList.size() !=0) {
                            beforeId = hometownList.get(hometownList.size() - 1).getId();
                        } else{
                            beforeId = 0;
                        }
                        if (null != hometownList && hometownList.size() != 0 && beforeId <= maxId) {
                            isDataFromServer = false;
                        }
                        leastId = beforeId;

                        listAdapter = new ListBaseAdapter(getActivity(), hometownList);
                        listView.setAdapter(listAdapter);
                        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                            GetDataThread threadGetMoreData = new GetDataThread();

                            @Override
                            public void onScrollStateChanged(AbsListView view, int scrollState) {

                            }

                            @Override
                            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                                if (view.getLastVisiblePosition() == totalItemCount - 1 && listView.getCount() >= 25 && isLoading == false) {
                                    isLoading = true;
                                    threadGetMoreData.run(leastId);
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        };

        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        };
        String getUrl = url + "&page=0";
        JsonArrayRequest getRequest = new JsonArrayRequest(getUrl, success, failure);
        VolleyQueue.instance(getActivity()).add(getRequest);
    }


    private int getSQLLastId() {
        int lastId = 0;
        Cursor result = nameDb.rawQuery("SELECT MAX(ID) FROM HOMETOWNLOCATION_DETAILS", null);
        if (null != result && result.moveToFirst()) {
            lastId = (int) result.getLong(0);
        }
        return lastId;
    }

    private HashMap<ArrayList<UserDetails>, Integer> getDataFromSQL(int lastId) {
        ArrayList<UserDetails> lst = new ArrayList<>();
        Cursor result;
         if(query.equals("SELECT * FROM HOMETOWNLOCATION_DETAILS "))
             result = nameDb.rawQuery(query + " WHERE ID < " + lastId + " ORDER BY ID DESC LIMIT 50", null);
        else {
             result = nameDb.rawQuery(query + " AND ID < " + lastId + " ORDER BY ID DESC LIMIT 50", null);
         }
        if (result.moveToFirst()) {
            do {
                UserDetails details = new UserDetails();
                details.setNickName(result.getString(1));
                details.setId(Integer.parseInt(result.getString(0)));
                details.setCountry(result.getString(2));
                details.setCity(result.getString(4));
                details.setState(result.getString(3));
                lst.add(details);
                leastId = details.getId();
            } while (result.moveToNext());
        }
        HashMap<ArrayList<UserDetails>, Integer> resultMap = new HashMap<>();
        resultMap.put(lst, leastId);
        return resultMap;
    }

}
