package com.example.myrestaurantai;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private TextView tvLog;
    private EditText etInput;
    private Button btnSend;
    private ScrollView scrollView;

    // --- 你的 ngrok 固定網址 ---
    private final String NGROK_URL = "https://cispadane-landlike-marvel.ngrok-free.dev/api/chat";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLog = findViewById(R.id.tvLog);
        etInput = findViewById(R.id.etInput);
        btnSend = findViewById(R.id.btnSend);
        scrollView = findViewById(R.id.scrollView);

        appendMessage("System", "歡迎來到 Yummy Restaurant！我是 AI 助手，請問有什麼可以幫您？");

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = etInput.getText().toString().trim();
                if (!input.isEmpty()) {
                    appendMessage("User", input);
                    askAI(input);
                    etInput.setText("");
                }
            }
        });
    }

    private void askAI(String userInput) {
        // 建立 OkHttp 客戶端並設定逾時
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        // 餐廳知識庫
        String systemPrompt = "你現在是 Yummy Restaurant 的專業服務員。 " +
                "菜單：1.醃製黃瓜花($28), 2.麻辣木耳($26), 3.口水雞($32), " +
                "4.酸菜魚湯($48), 5.重慶風味安格斯牛肉($95)。 " +
                "套餐：雙人套餐($180)。 " +
                "請用禮貌回答客人，簡潔一點，按照客戶問題需求回答即可";

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("model", "qwen2.5:7b");
            jsonBody.put("stream", false);

            JSONArray messages = new JSONArray();
            messages.put(new JSONObject().put("role", "system").put("content", systemPrompt));
            messages.put(new JSONObject().put("role", "user").put("content", userInput));
            jsonBody.put("messages", messages);

            RequestBody body = RequestBody.create(
                    jsonBody.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            // --- 這裡加入了關鍵的 Header，防止 ngrok 攔截 ---
            Request request = new Request.Builder()
                    .url(NGROK_URL)
                    .addHeader("ngrok-skip-browser-warning", "true")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> appendMessage("System", "連線失敗：" + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        try {
                            JSONObject jsonResponse = new JSONObject(responseData);
                            String aiAnswer = jsonResponse.getJSONObject("message").getString("content");
                            runOnUiThread(() -> appendMessage("System", aiAnswer));
                        } catch (Exception e) {
                            runOnUiThread(() -> appendMessage("System", "解析回答錯誤"));
                        }
                    } else {
                        // 如果出現 403，這裡會顯示錯誤代碼
                        runOnUiThread(() -> appendMessage("System", "伺服器錯誤代碼：" + response.code()));
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void appendMessage(String sender, String message) {
        runOnUiThread(() -> {
            String currentText = tvLog.getText().toString();
            String prefix = sender.equals("System") ? "" : "User: ";
            tvLog.setText(currentText + "\n" + prefix + message + "\n");
            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
        });
    }
}