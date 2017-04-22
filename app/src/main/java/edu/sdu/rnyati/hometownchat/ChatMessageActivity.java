package edu.sdu.rnyati.hometownchat;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ChatMessageActivity extends AppCompatActivity {

    private String nickname, currentUser, chatKey, key;
    private FirebaseListAdapter<ChatMessage> adapter;
    private boolean doChatExists;
    DatabaseReference myDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doChatExists = false;
        setContentView(R.layout.activity_chat_messenger);
        Bundle data = this.getIntent().getExtras();
        nickname = data.getString("nickname");
        currentUser = FirebaseAuth.getInstance().getCurrentUser().getUid();
        myDatabase = FirebaseDatabase.getInstance().getReference().child("chat_users");
        Query query = myDatabase.child(currentUser).orderByValue().equalTo(nickname);
        ValueEventListener ve = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot child : children) {
                    if (child.getValue().equals(nickname)) {
                        chatKey = child.getKey();
                        displayChatMessages(chatKey);
                        doChatExists = true;
                    }
                }

                if (!doChatExists) {
                    key = myDatabase.child(currentUser).getKey();
                    chatKey = myDatabase.child(currentUser).push().getKey();
                    myDatabase.child(currentUser).child(chatKey).setValue(nickname);
                    myDatabase.child(nickname).child(chatKey).setValue(currentUser);
                    displayChatMessages(chatKey);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };

        query.addValueEventListener(ve);
        FloatingActionButton fab =
                (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input = (EditText) findViewById(R.id.input);

                FirebaseDatabase.getInstance()
                        .getReference().child("chats").child(chatKey).push()
                        .setValue(new ChatMessage(input.getText().toString(),
                                FirebaseAuth.getInstance()
                                        .getCurrentUser().getEmail())
                        );
                input.setText("");
            }
        });
    }

    protected void displayChatMessages(String chat_id) {
        ListView list = (ListView) findViewById(R.id.list_of_messages);
        adapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class,
                R.layout.message, FirebaseDatabase.getInstance().getReference().child("chats").child(chat_id)) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                TextView messageText = (TextView) v.findViewById(R.id.message_text);
                TextView messageUser = (TextView) v.findViewById(R.id.message_user);
                TextView messageTime = (TextView) v.findViewById(R.id.message_time);
                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());
                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                        model.getMessageTime()));
            }
        };
        list.setAdapter(adapter);
    }
}

