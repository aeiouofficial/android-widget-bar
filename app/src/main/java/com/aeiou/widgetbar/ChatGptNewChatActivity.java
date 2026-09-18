package com.aeiou.widgetbar;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public final class ChatGptNewChatActivity extends Activity {
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
        Intent nativeNewChat = new Intent(Intent.ACTION_VIEW, NATIVE_NEW_CHAT);
        nativeNewChat.setPackage(CHATGPT_PACKAGE);
        nativeNewChat.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (nativeNewChat.resolveActivity(getPackageManager()) != null) {
            startActivity(nativeNewChat);
            return;
        }

        Intent web = new Intent(Intent.ACTION_VIEW, WEB_FALLBACK);
        web.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(web);
    }
}
