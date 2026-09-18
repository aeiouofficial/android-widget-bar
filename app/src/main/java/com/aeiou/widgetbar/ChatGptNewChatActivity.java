package com.aeiou.widgetbar;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public final class ChatGptNewChatActivity extends Activity {
    private static final String CHATGPT_PACKAGE = "com.openai.chatgpt";
    private static final Uri NEW_CHAT_ROOT = Uri.parse("https://chatgpt.com/");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openNewChat();
        finish();
    }

    private void openNewChat() {
        Intent appLink = new Intent(Intent.ACTION_VIEW, NEW_CHAT_ROOT);
        appLink.setPackage(CHATGPT_PACKAGE);
        appLink.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (appLink.resolveActivity(getPackageManager()) != null) {
            startActivity(appLink);
            return;
        }

        Intent web = new Intent(Intent.ACTION_VIEW, NEW_CHAT_ROOT);
        web.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(web);
    }
}
