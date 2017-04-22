package edu.sdu.rnyati.hometownchat;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;

public class ShowUserActivity extends AppCompatActivity {

    private String TAG = "ShowUserActivity:";

    private Spinner spinnerCountry, spinnerState, spinnerYear;
    String selectedCountry, selectedState, selectedYear, url, query;
    FragmentManager fragmentManager;

    private DatabaseAdapter dbHelper;
    private SQLiteDatabase nameDb;
    Bundle bundle;
    boolean isListView;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    TextView filterTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_show_user);
        mAuth = FirebaseAuth.getInstance();

        spinnerCountry = (Spinner) findViewById(R.id.spinnerCountry);
        spinnerState = (Spinner) findViewById(R.id.spinnerState);
        spinnerYear = (Spinner) findViewById(R.id.spinnerYear);

        filterTextView = (TextView) findViewById(R.id.filterText);

        fragmentManager = getSupportFragmentManager();

        dbHelper = (new DatabaseAdapter( ShowUserActivity.this));
        nameDb = dbHelper.getWritableDatabase();
        dbHelper.onCreate(nameDb);

        getCountry();
        spinnerCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCountry = parent.getItemAtPosition(position).toString();
                if(emptyCheck(selectedCountry)){
                    getStates(selectedCountry);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedState = parent.getItemAtPosition(position).toString();

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        String yearStr[] = new String[49];
        int startYear = 1970;
        yearStr[0] = "Select Year";
        for(int i = 1; i<=48; i++){
            yearStr[i] = String.valueOf(startYear);
            startYear++;
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(ShowUserActivity.this, android.R.layout.simple_spinner_item, yearStr);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerYear.setAdapter(dataAdapter);

        spinnerYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedYear = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        isListView = true;
        showListView();
    }

    public void showMapView() {
        isListView = false;
        url = getUrl();
        query = createQuery();
        bundle = new Bundle();
        bundle.putString("query", query);
        bundle.putString("url", url);
        ShowUserMapFragment userList = new ShowUserMapFragment();
        userList.setArguments(bundle);
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, userList)
                .addToBackStack(null)
                .commit();
    }

    public void showListView() {
        url = getUrl();
        isListView = true;
        query = createQuery();
        bundle = new Bundle();
        bundle.putString("query", query);
        bundle.putString("url", url);
        ShowUserListFragment userList = new ShowUserListFragment();
        userList.setArguments(bundle);
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, userList)
                .addToBackStack(null)
                .commit();
    }

    public void getCountry() {
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                String strings[] = Utility.convertToArray(response.toString());

                String newStr[] = new String[strings.length+1];
                newStr[0] = "Select Country";
                for(int i=1; i<newStr.length; i++){
                    newStr[i] = strings[i-1];
                }
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(ShowUserActivity.this,
                        android.R.layout.simple_spinner_item, newStr);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCountry.setAdapter(dataAdapter);

            }
        };
        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        };
        String url ="http://bismarck.sdsu.edu/hometown/countries";
        JsonArrayRequest getRequest = new JsonArrayRequest( url, success, failure);
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(getRequest);
    }

    public void getStates(String countrySelected) {
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                String strings[] = Utility.convertToArray(response.toString());
                String newStr[] = new String[strings.length+1];
                newStr[0] = "Select State";
                for(int i=1; i<newStr.length; i++){
                    newStr[i] = strings[i-1];
                }
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(ShowUserActivity.this,
                        android.R.layout.simple_spinner_item, newStr);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerState.setAdapter(dataAdapter);
            }
        };
        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        };
        String url ="http://bismarck.sdsu.edu/hometown/states?country="+countrySelected;
        JsonArrayRequest getRequest = new JsonArrayRequest( url, success, failure);
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(getRequest);
    }

    public boolean emptyCheck(String string){
        if( null != string && !string.isEmpty() && !string.contains("Select") ) {
            return true;
        }
        return false;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.signOutMenu:
                mAuth.signOut();
                this.finish();
                break;
            case R.id.chatMenu:
                Intent intent = new Intent(ShowUserActivity.this,ChatHistoryActivity.class);
                startActivity(intent);
                break;
            case R.id.mapMenu:
                showMapView();
                break;
            case R.id.listMenu:
                showListView();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public String getUrl(){
        filterTextView.setText("Filter applied");
        if(emptyCheck(selectedCountry) && emptyCheck(selectedState) && emptyCheck(selectedYear)){
            url = "http://bismarck.sdsu.edu/hometown/users?reverse=true&country=" + selectedCountry + "&state=" + selectedState + "&year=" + selectedYear;
        }else if(emptyCheck(selectedCountry)&& emptyCheck(selectedState)){
            url = "http://bismarck.sdsu.edu/hometown/users?reverse=true&country=" + selectedCountry + "&state=" + selectedState;
        }else if(emptyCheck(selectedCountry)&&emptyCheck(selectedYear)){
            url = "http://bismarck.sdsu.edu/hometown/users?reverse=true&country=" + selectedCountry + "&year=" + selectedYear;
        }else if(emptyCheck(selectedState)&&emptyCheck(selectedYear)){
            url = "http://bismarck.sdsu.edu/hometown/users?reverse=true&state=" + selectedState + "&year=" + selectedYear;
        }else if(emptyCheck(selectedState)){
            url = "http://bismarck.sdsu.edu/hometown/users?reverse=true&state=" + selectedState;
        }else if(emptyCheck(selectedYear)){
            url = "http://bismarck.sdsu.edu/hometown/users?reverse=true&year=" + selectedYear;
        }else if(emptyCheck(selectedCountry)){
            url = "http://bismarck.sdsu.edu/hometown/users?reverse=true&country=" + selectedCountry;
        }else{
            url = "http://bismarck.sdsu.edu/hometown/users?reverse=true";
            filterTextView.setText("Filter not applied");
        }
        return url;
    }

    public String createQuery(){
        if (emptyCheck(selectedCountry) && emptyCheck(selectedState)
                && emptyCheck(selectedYear)) {
            query = " SELECT * FROM HOMETOWNLOCATION_DETAILS WHERE COUNTRY = '" + selectedCountry + "'AND STATE = '" + selectedState + "'AND YEAR= " + Integer.valueOf(selectedYear);
        } else if (emptyCheck(selectedCountry) &&
                emptyCheck(selectedState)) {
            query = " SELECT * FROM HOMETOWNLOCATION_DETAILS WHERE COUNTRY='" + selectedCountry + "' AND STATE = '" + selectedState+"' ";
        } else if (emptyCheck(selectedCountry) &&
                emptyCheck(selectedYear)) {
            query = " SELECT * FROM HOMETOWNLOCATION_DETAILS WHERE COUNTRY='" + selectedCountry + "'AND YEAR = " + Integer.valueOf(selectedYear);
        } else if (emptyCheck(selectedState) && emptyCheck(selectedYear)) {
            query = " SELECT * FROM HOMETOWNLOCATION_DETAILS WHERE STATE='" + selectedState + "'AND YEAR = " + Integer.valueOf(selectedYear);
        } else if (emptyCheck(selectedState)) {
            query = " SELECT * FROM HOMETOWNLOCATION_DETAILS WHERE STATE='" + selectedState +"' ";
        } else if (emptyCheck(selectedYear)) {
            query = " SELECT * FROM HOMETOWNLOCATION_DETAILS WHERE YEAR=" + Integer.valueOf(selectedYear);
        } else if (emptyCheck(selectedCountry)) {
            query = " SELECT * FROM HOMETOWNLOCATION_DETAILS WHERE COUNTRY='" + selectedCountry +"' ";
        } else {
            query = "SELECT * FROM HOMETOWNLOCATION_DETAILS ";
        }
        return query;
    }

    public void onApplyFilter(View v){
        if(isListView){
            showListView();
        }else{
            showMapView();
        }
    }

}
