package com.example.yummyrestaurant.activities;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.yummyrestaurant.R;
import com.example.yummyrestaurant.database.DatabaseHelper;
import com.example.yummyrestaurant.utils.RoleManager;
import java.util.ArrayList;
import java.util.List;

public class SupportActivity extends AppCompatActivity {

    EditText messageInput;
    Button sendBtn;
    ListView chatList;
    ArrayAdapter<String> adapter;
    List<String> messages = new ArrayList<>();

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        messageInput = findViewById(R.id.messageInput);
        sendBtn = findViewById(R.id.sendBtn);
        chatList = findViewById(R.id.chatList);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messages);
        chatList.setAdapter(adapter);

        // Initialize Database Helper
        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();

        // Load chat messages from SQLite
        loadChatMessages();

        sendBtn.setOnClickListener(v -> {
            String msg = messageInput.getText().toString();
            if (!msg.isEmpty()) { // 避免发送空消息
                // Insert the chat message into the SQLite database
                long result = dbHelper.insertChatMessage(db, msg);
                if (result != -1) {
                    messages.add(msg);
                    adapter.notifyDataSetChanged();
                    messageInput.setText("");
                } else {
                    Toast.makeText(this, "Sending the message failed, please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadChatMessages() {
        Cursor cursor = dbHelper.getAllChatMessages();
        int columnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_CHAT_MESSAGE);

        // Check if the column exists
        if (columnIndex == -1) {
            Toast.makeText(this, "The chat message column does not exist.", Toast.LENGTH_SHORT).show();
            cursor.close();
            return;
        }

        if (cursor.moveToFirst()) {
            do {
                String message = cursor.getString(columnIndex);
                if (message != null) {
                    messages.add(message);
                }
            } while (cursor.moveToNext());
            adapter.notifyDataSetChanged();
        }
        cursor.close();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }
}
