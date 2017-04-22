package edu.sdu.rnyati.hometownchat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class ChatHistoryActivity extends AppCompatActivity {

    private String currentUser;
    private FirebaseListAdapter<String> adapter;
    TextView messageUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_history);

        if ( null != FirebaseAuth.getInstance().getCurrentUser()) {
            currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            displayChats(currentUser);
        }
    }

    protected void displayChats(String uid) {
        ListView list = (ListView) findViewById(R.id.listView1);

        adapter = new FirebaseListAdapter<String>(this, String.class,
                R.layout.chat_history_row, FirebaseDatabase.getInstance().getReference().child("chat_users").child(uid)) {
            @Override
            protected void populateView(View v, String model, int position) {

                messageUser = (TextView) v.findViewById(R.id.chat_nickName);
                Button chatButton = (Button) v.findViewById(R.id.chat_button_history);
                messageUser.setText(model.toString());
                chatButton.setOnClickListener( new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Bundle data = new Bundle();
                        View row = (View)v.getParent();
                        messageUser = (TextView) row.findViewById(R.id.chat_nickName);
                        String name = messageUser.getText().toString();
                        data.putString("nickname", name);
                        Intent intent = new Intent(ChatHistoryActivity.this,ChatMessageActivity.class);
                        intent.putExtras(data);
                        startActivity(intent);
                    }
                });
            }
        };

        list.setAdapter(adapter);
    }
}
