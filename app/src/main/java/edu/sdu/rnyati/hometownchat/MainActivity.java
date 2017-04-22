package edu.sdu.rnyati.hometownchat;

import android.content.ContentValues;
import android.content.Intent;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main Activity";

    private EditText emailEditText, pwdEditText;
    private FirebaseAuth firebaseAuth;
    private Button signIn;
    private TextView newUser;
    private DatabaseAdapter dbHelper;
    private SQLiteDatabase nameDb;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        emailEditText = (EditText)findViewById(R.id.email);
        pwdEditText = (EditText)findViewById(R.id.password);
        signIn = (Button) findViewById(R.id.signUP);
        firebaseAuth = FirebaseAuth.getInstance();
        newUser = (TextView) findViewById(R.id.newUser);
        dbHelper = (new DatabaseAdapter( MainActivity.this));
        nameDb = dbHelper.getWritableDatabase();

        dbHelper.onCreate(nameDb);

        saveHomeTownDetailsToDb();
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        newUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                createUser( v );
            }
        });
    }


    public void signIn(){
        if (Utility.checkIsNullOrIsEmpty(emailEditText.getText().toString()) && Utility.checkIsNullOrIsEmpty(pwdEditText.getText().toString())) {
            Utility.showProgressDialog(MainActivity.this);
            (firebaseAuth.signInWithEmailAndPassword(emailEditText.getText().toString(),
                    pwdEditText.getText().toString()))
                    .addOnCompleteListener(
                            new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    Utility.hideProgressDialog();

                                    if (task.isSuccessful()){
                                        Intent intent = new Intent(MainActivity.this, ShowUserActivity.class);
                                        startActivity(intent);
                                    } else {
                                        Utility.createDialog("Oops..", "Login failed. Invalid User.", true,MainActivity.this);
                                    }
                                }
                            }
                    );
        } else {
            Utility.createDialog("Oops..", "Please enter email id and password.", true, MainActivity.this);
        }
    }


/*
    public void signIn(){

        Utility.showProgressDialog(MainActivity.this);
        (firebaseAuth.signInWithEmailAndPassword(emailEditText.getText().toString(),
                pwdEditText.getText().toString()))
                .addOnCompleteListener(
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Utility.hideProgressDialog();

                                if (task.isSuccessful()){
                                    Intent intent = new Intent(MainActivity.this, ShowUserActivity.class);
                                    startActivity(intent);
                                } else {
                                    Utility.createDialog("Oops..", "Login failed. Invalid User.", true,MainActivity.this);
                                }
                            }
                        }
                );
    }
*/

    public void createUser(View v){
        Intent intent = new Intent(v.getContext(), AddUserActivity.class);
        v.getContext().startActivity(intent);
    }

    private void saveHomeTownDetailsToDb() {

        Response.Listener<JSONArray> success = new Response.Listener<JSONArray>() {
            public void onResponse(JSONArray response) {
                try {
                    if (response != null) {
                        for (int i = 0; i < response.length(); i++) {
                            ContentValues contentValues = new ContentValues();
                            JSONObject jsonObj = response.getJSONObject(i);
                            contentValues.put("NICKNAME", jsonObj.get("nickname").toString());
                            contentValues.put("COUNTRY", jsonObj.get("country").toString());
                            contentValues.put("STATE", jsonObj.get("state").toString());
                            contentValues.put("CITY", jsonObj.get("city").toString());
                            contentValues.put("YEAR", jsonObj.get("year").toString());
                            contentValues.put("LONGITUDE", jsonObj.get("longitude").toString());
                            contentValues.put("LATITUDE", jsonObj.get("latitude").toString());
                            if (Utility.checkIsNullOrIsEmpty(jsonObj.get("id").toString())) {
                                contentValues.put("ID", jsonObj.get("id").toString());
                            }else {
                                contentValues.put("ID", 0);
                            }
                            try {
                                nameDb.insert("HOMETOWNLOCATION_DETAILS", null, contentValues);
                            }catch(SQLiteConstraintException e){}
                        }
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
        JsonArrayRequest getRequest = new JsonArrayRequest("http://bismarck.sdsu.edu/hometown/users?reverse=true&page=0", success, failure);
        VolleyQueue.instance(MainActivity.this).add(getRequest);
    }
}
