package com.aeiou.widgetbar;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public final class ChatGptNewChatActivity extends Activity {
    static final String EXTRA_PROMPT = "prompt";

    private static final String CHATGPT_PACKAGE = "com.openai.chatgpt";
    private static final Uri NATIVE_NEW_CHAT = Uri.parse("chatgpt://");
    private static final Uri WEB_FALLBACK = Uri.parse("https://chatgpt.com/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openNewChat();
        finish();
    }

    private void openNewChat() {
        String prompt = getIntent().getStringExtra(EXTRA_PROMPT);
        String trimmed = prompt == null ? "" : prompt.trim();

        if (!trimmed.isEmpty()) {
            Intent sharePrompt = new Intent(Intent.ACTION_SEND)
                    .setType("text/plain")
                    .putExtra(Intent.EXTRA_TEXT, trimmed)
                    .setPackage(CHATGPT_PACKAGE)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (sharePrompt.resolveActivity(getPackageManager()) != null) {
                startActivity(sharePrompt);
                return;
            }
        }

        Intent nativeNewChat = new Intent(Intent.ACTION_VIEW, NATIVE_NEW_CHAT)
                .setPackage(CHATGPT_PACKAGE)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (nativeNewChat.resolveActivity(getPackageManager()) != null) {
            startActivity(nativeNewChat);
            return;
        }

        Uri fallbackUri = trimmed.isEmpty()
                ? WEB_FALLBACK
                : WEB_FALLBACK.buildUpon().appendQueryParameter("q", trimmed).build();
        Intent web = new Intent(Intent.ACTION_VIEW, fallbackUri)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(web);
    }
}
