package edu.sdu.rnyati.hometownchat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AddUserActivity extends AppCompatActivity {

    private static final int INTENT_SET_LOCATION = 213;

    private FirebaseAuth firebaseAuth;

    private TextView txtLongitude, txtLatitude;
    private EditText emailEditText, nicknameEditText, pwdEditText, cityEditText, yearEditText;
    private Button btnSetLocation, btnRegisterUser;
    private Spinner spinnerCountry, spinnerState;
    private String country, state, nickname;
    double longitude, latitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        firebaseAuth = FirebaseAuth.getInstance();

        emailEditText = (EditText) findViewById(R.id.emailAdress);
        nicknameEditText = (EditText)findViewById(R.id.nickName);
        pwdEditText =(EditText) findViewById(R.id.password);
        cityEditText = (EditText)findViewById(R.id.city);
        yearEditText = (EditText)findViewById(R.id.year);
        txtLongitude = (TextView) findViewById(R.id.longitude);
        txtLatitude = (TextView)findViewById(R.id.latitude);
        btnRegisterUser = (Button) findViewById(R.id.registerUser);
        btnSetLocation = (Button)findViewById(R.id.setLocation);
        spinnerCountry =(Spinner) findViewById(R.id.country);
        spinnerState = (Spinner)findViewById(R.id.state);

        if (getIntent()!= null && getIntent().getExtras() != null) {
            latitude = getIntent().getExtras().getDouble("latitude");
            longitude = getIntent().getExtras().getDouble("longitude");
            txtLatitude.setText(String.valueOf(latitude));
            txtLongitude.setText(String.valueOf(longitude));
        }

        getCountries();
        spinnerCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                country = parent.getItemAtPosition(position).toString();
                getStates(country);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinnerState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                state = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnRegisterUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser(v);
            }
        });

        btnSetLocation.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(AddUserActivity.this,MapsActivity.class);
                startActivityForResult(intent, INTENT_SET_LOCATION);
            }
        });

    }

    public void registerUser(View v){

        if(checkUserDetails()) {
            postData();
        }else{
            Utility.createDialog("Oops!","Please enter all the details.",true, AddUserActivity.this);
        }

        Utility.showProgressDialog(AddUserActivity.this);
        (firebaseAuth.createUserWithEmailAndPassword(emailEditText.getText().toString(),
                pwdEditText.getText().toString()))
                .addOnCompleteListener(
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Utility.hideProgressDialog();
                                if (task.isSuccessful()){
                                    firebaseAuth.signInWithEmailAndPassword(emailEditText.getText().toString(),
                                            pwdEditText.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()){
                                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                                nickname = nicknameEditText.getText().toString();
                                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users");
                                                ref.child(user.getUid()).push().setValue(nickname);
                                                FirebaseAuth.getInstance().signOut();
                                            }
                                        }
                                    });

                                    Utility.createDialog(".Congratulations!", "User registered successfully.", false,AddUserActivity.this);
                                    Intent intent = new Intent(AddUserActivity.this, MainActivity.class);
                                    startActivity(intent);
                                } else {
                                    Utility.createDialog("Try again!", "User not registered.", true, AddUserActivity.this);
                                }
                            }
                        }

                );
    }


    public void getCountries() {

        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                String strings[] = Utility.convertToArray(response.toString());
                for(int i=0; i<response.length(); i++){
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(AddUserActivity.this, android.R.layout.simple_spinner_item, strings);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCountry.setAdapter(dataAdapter);
                }
            }
        };

        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Log.d("error", error.toString());
            }
        };

        String url = "http://bismarck.sdsu.edu/hometown/countries";
        JsonArrayRequest getRequest = new JsonArrayRequest(url, success, failure);
        VolleyQueue.instance(AddUserActivity.this).add(getRequest);
    }

    public void getStates(String selectedCountry) {
        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                Log.d("response", response.toString());
                String strings[] = Utility.convertToArray(response.toString());
                for(int i=0; i<response.length(); i++){
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(AddUserActivity.this, android.R.layout.simple_spinner_item, strings);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerState.setAdapter(dataAdapter);
                }
            }
        };

        Response.ErrorListener failure = new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Log.d("error", error.toString());
            }
        };

        String url = Constants.GET_STATES + selectedCountry;
        JsonArrayRequest getRequest = new JsonArrayRequest(url, success, failure);
        VolleyQueue.instance(AddUserActivity.this).add(getRequest);
    }

    private void postData() {
        JSONObject data = new JSONObject();
        try {
            data.put("nickname", nicknameEditText.getText().toString());
            data.put("password", pwdEditText.getText().toString());
            data.put("country", country);
            data.put("state", state);
            data.put("city", cityEditText.getText().toString());
            data.put("year", Integer.valueOf(yearEditText.getText().toString()));
            if(!txtLongitude.getText().equals("") && !txtLatitude.getText().equals("")){
                data.put("latitude", latitude);
                data.put("longitude", longitude);
            }
        } catch (JSONException error) {
             error.printStackTrace();
            return;
        }

        Response.Listener<JSONObject> success = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Utility.createDialog("Congratulations", "User registered successfully!!",false, AddUserActivity.this);

            }
        };

        Response.ErrorListener failure = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Utility.createDialog("Oops", new String(error.networkResponse.data),false,AddUserActivity.this);
            }
        };

        JsonObjectRequest postRequest = new JsonObjectRequest(Constants.ADD_USER, data, success, failure);
        VolleyQueue.instance(AddUserActivity.this).add(postRequest);

    }


    private boolean checkUserDetails(){
        if (!Utility.checkIsNullOrIsEmpty(nicknameEditText.getText().toString())
                || !Utility.checkIsNullOrIsEmpty(pwdEditText.getText().toString())
                ||  !Utility.checkIsNullOrIsEmpty(country)
                ||  !Utility.checkIsNullOrIsEmpty(state)
                ||  !Utility.checkIsNullOrIsEmpty(cityEditText.getText().toString())
                ||  !Utility.checkIsNullOrIsEmpty(yearEditText.getText().toString())) {
            return false;
        } else {
            return  true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INTENT_SET_LOCATION) {
            switch (resultCode) {
                case RESULT_OK:
                    txtLatitude.setText(String.valueOf(data.getExtras().get("latitude")));
                    txtLongitude.setText(String.valueOf(data.getExtras().get("longitude")));
                    break;
                case RESULT_CANCELED:
                    break;
            }
        }
    }

}
