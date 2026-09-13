package com.tpaihub.app;

import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String API_URL =
            "http://127.0.0.1:3001/api/ai/chat";

    private TextView chatOutput;
    private EditText messageInput;
    private Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 32, 24, 24);
        root.setBackgroundColor(Color.rgb(16, 16, 20));

        TextView title = new TextView(this);
        title.setText("🤖 TP-AI-HUB");
        title.setTextColor(Color.WHITE);
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 10, 0, 20);

        chatOutput = new TextView(this);
        chatOutput.setText("AI: Hello! 👋\n\n");
        chatOutput.setTextColor(Color.WHITE);
        chatOutput.setTextSize(17);
        chatOutput.setPadding(12, 12, 12, 12);

        messageInput = new EditText(this);
        messageInput.setHint("Type your message...");
        messageInput.setHintTextColor(Color.GRAY);
        messageInput.setTextColor(Color.WHITE);

        sendButton = new Button(this);
        sendButton.setText("SEND");

        sendButton.setOnClickListener(v -> sendMessage());

        root.addView(title);

        root.addView(
                chatOutput,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1
                )
        );

        root.addView(
                messageInput,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        root.addView(
                sendButton,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        setContentView(root);
    }

    private void sendMessage() {

        String message = messageInput.getText().toString().trim();

        if (message.isEmpty()) {
            return;
        }

        chatOutput.append("You: " + message + "\n");
        messageInput.setText("");
        sendButton.setEnabled(false);

        new Thread(() -> {

            HttpURLConnection connection = null;

            try {
                URL url = new URL(API_URL);

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );
                connection.setDoOutput(true);
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(30000);

                JSONObject request = new JSONObject();
                request.put("message", message);

                OutputStream output = connection.getOutputStream();
                output.write(request.toString().getBytes("UTF-8"));
                output.close();

                int responseCode = connection.getResponseCode();

                BufferedReader reader;

                if (responseCode >= 200 && responseCode < 300) {
                    reader = new BufferedReader(
                            new InputStreamReader(
                                    connection.getInputStream()
                            )
                    );
                } else {
                    reader = new BufferedReader(
                            new InputStreamReader(
                                    connection.getErrorStream()
                            )
                    );
                }

                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();

                JSONObject json =
                        new JSONObject(response.toString());

                final String reply;

                if (json.has("reply")) {
                    reply = json.getString("reply");
                } else if (json.has("message")) {
                    reply = "Error: " + json.getString("message");
                } else {
                    reply = response.toString();
                }

                runOnUiThread(() -> {
                    chatOutput.append(
                            "AI: " + reply + "\n\n"
                    );
                    sendButton.setEnabled(true);
                });

            } catch (Exception e) {

                runOnUiThread(() -> {
                    chatOutput.append(
                            "AI: Connection error: "
                                    + e.getMessage()
                                    + "\n\n"
                    );
                    sendButton.setEnabled(true);

                    Toast.makeText(
                            MainActivity.this,
                            "AI request failed",
                            Toast.LENGTH_SHORT
                    ).show();
                });

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }

        }).start();
    }
}
