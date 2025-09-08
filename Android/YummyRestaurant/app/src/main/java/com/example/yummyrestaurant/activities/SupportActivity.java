package com.example.yummyrestaurant.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.yummyrestaurant.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.List;

public class SupportActivity extends AppCompatActivity {
    EditText messageInput;
    Button sendBtn;
    ListView chatList;
    ArrayAdapter<String> adapter;
    List<String> messages = new ArrayList<>();
    DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("supportChat");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        messageInput = findViewById(R.id.messageInput);
        sendBtn = findViewById(R.id.sendBtn);
        chatList = findViewById(R.id.chatList);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messages);
        chatList.setAdapter(adapter);

        sendBtn.setOnClickListener(v -> {
            String msg = messageInput.getText().toString();
            if (!msg.isEmpty()) { // 避免发送空消息
                chatRef.push().setValue(msg);
                messageInput.setText("");
            }
        });

        chatRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String prevChildKey) {
                String message = snapshot.getValue(String.class);
                if (message != null) {
                    messages.add(message);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String prevChildKey) {
                // 处理子节点更改事件
            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {
                // 处理子节点删除事件
            }

            @Override
            public void onChildMoved(DataSnapshot snapshot, String prevChildKey) {
                // 处理子节点移动事件
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // 处理取消事件
            }
        });
    }
}
