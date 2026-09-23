package com.aeiou.widgetbar;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;

import java.util.ArrayList;

public final class ChatGptMediaActivity extends Activity {
    static final String EXTRA_ACTION = "action";
    static final String ACTION_VOICE = "voice";
    static final String ACTION_CAMERA = "camera";
    static final String ACTION_PHOTO = "photo";
    static final String ACTION_DICTATION = "dictation";

    private static final int REQUEST_CAMERA = 1001;
    private static final int REQUEST_PHOTO = 1002;
    private static final int REQUEST_DICTATION = 1003;
    private static final String CHATGPT_PACKAGE = "com.openai.chatgpt";

    private Uri pendingCameraUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        String action = getIntent().getStringExtra(EXTRA_ACTION);
        if (ACTION_VOICE.equals(action)) {
            openVoice();
        } else if (ACTION_CAMERA.equals(action)) {
            openCamera();
        } else if (ACTION_PHOTO.equals(action)) {
            openPhotoPicker();
        } else if (ACTION_DICTATION.equals(action)) {
            openDictation();
        } else {
            finish();
        }
    }

    private void openVoice() {
        Intent voice = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://chatgpt.com/voice"))
                .setPackage(CHATGPT_PACKAGE)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (voice.resolveActivity(getPackageManager()) != null) {
            startActivity(voice);
        } else {
            SearchLauncher.openAppHome(this, ProviderTarget.CHATGPT);
        }
        finish();
    }

    private void openCamera() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            SearchLauncher.openAppHome(this, ProviderTarget.CHATGPT);
            finish();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(
                MediaStore.Images.Media.DISPLAY_NAME,
                "widgetbar-" + System.currentTimeMillis() + ".jpg");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "Pictures/WidgetBar");
        values.put(MediaStore.Images.Media.IS_PENDING, 1);

        pendingCameraUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values);

        if (pendingCameraUri == null) {
            SearchLauncher.openAppHome(this, ProviderTarget.CHATGPT);
            finish();
            return;
        }

        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                .putExtra(MediaStore.EXTRA_OUTPUT, pendingCameraUri);
        camera.setClipData(ClipData.newRawUri("camera-output", pendingCameraUri));
        camera.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        try {
            startActivityForResult(camera, REQUEST_CAMERA);
        } catch (ActivityNotFoundException e) {
            cleanupCameraUri();
            SearchLauncher.openAppHome(this, ProviderTarget.CHATGPT);
            finish();
        }
    }

    private void openPhotoPicker() {
        Intent pick;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pick = new Intent(MediaStore.ACTION_PICK_IMAGES);
        } else {
            pick = new Intent(Intent.ACTION_OPEN_DOCUMENT)
                    .addCategory(Intent.CATEGORY_OPENABLE)
                    .setType("image/*");
        }

        try {
            startActivityForResult(pick, REQUEST_PHOTO);
        } catch (ActivityNotFoundException e) {
            SearchLauncher.openAppHome(this, ProviderTarget.CHATGPT);
            finish();
        }
    }

    private void openDictation() {
        Intent dictate = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                .putExtra(
                        RecognizerIntent.EXTRA_PROMPT,
                        getString(R.string.chatgpt_dictation));

        try {
            startActivityForResult(dictate, REQUEST_DICTATION);
        } catch (ActivityNotFoundException e) {
            openVoice();
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CAMERA) {
            if (resultCode == RESULT_OK && pendingCameraUri != null) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.IS_PENDING, 0);
                getContentResolver().update(
                        pendingCameraUri,
                        values,
                        null,
                        null);
                shareImage(pendingCameraUri);
            } else {
                cleanupCameraUri();
                finish();
            }
            return;
        }

        if (requestCode == REQUEST_PHOTO) {
            Uri image = resultCode == RESULT_OK && data != null
                    ? data.getData()
                    : null;
            if (image != null) {
                shareImage(image);
            } else {
                finish();
            }
            return;
        }

        if (requestCode == REQUEST_DICTATION) {
            ArrayList<String> results = resultCode == RESULT_OK && data != null
                    ? data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    : null;
            if (results != null && !results.isEmpty()) {
                startActivity(new Intent(this, ChatGptNewChatActivity.class)
                        .putExtra(
                                ChatGptNewChatActivity.EXTRA_PROMPT,
                                results.get(0)));
            }
            finish();
        }
    }

    private void shareImage(Uri image) {
        Intent share = new Intent(Intent.ACTION_SEND)
                .setType("image/*")
                .putExtra(Intent.EXTRA_STREAM, image)
                .setPackage(CHATGPT_PACKAGE)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        share.setClipData(ClipData.newRawUri("image", image));

        if (share.resolveActivity(getPackageManager()) != null) {
            startActivity(share);
        } else {
            SearchLauncher.openAppHome(this, ProviderTarget.CHATGPT);
        }
        finish();
    }

    private void cleanupCameraUri() {
        if (pendingCameraUri != null) {
            getContentResolver().delete(
                    pendingCameraUri,
                    null,
                    null);
            pendingCameraUri = null;
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}
