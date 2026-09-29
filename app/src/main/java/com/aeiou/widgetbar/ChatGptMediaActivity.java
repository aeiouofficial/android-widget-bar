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
import android.util.Log;

import java.util.ArrayList;

public final class ChatGptMediaActivity extends Activity {
    static final String EXTRA_ACTION = "action";
    static final String ACTION_VOICE = "voice";
    static final String ACTION_CAMERA = "camera";
    static final String ACTION_PHOTO = "photo";
    static final String ACTION_DICTATION = "dictation";

    private static final String TAG = "WidgetBarMedia";
    private static final String STATE_PENDING_CAMERA_URI = "pending_camera_uri";
    private static final int REQUEST_CAMERA = 1001;
    private static final int REQUEST_PHOTO = 1002;
    private static final int REQUEST_DICTATION = 1003;
    private static final String CHATGPT_PACKAGE = "com.openai.chatgpt";

    private Uri pendingCameraUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);

        if (savedInstanceState != null) {
            String pendingUri = savedInstanceState.getString(STATE_PENDING_CAMERA_URI);
            if (pendingUri != null && !pendingUri.isEmpty()) {
                pendingCameraUri = Uri.parse(pendingUri);
            }
            return;
        }

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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (pendingCameraUri != null) {
            outState.putString(
                    STATE_PENDING_CAMERA_URI,
                    pendingCameraUri.toString());
        }
        super.onSaveInstanceState(outState);
    }

    private void openVoice() {
        Intent voice = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://chatgpt.com/voice"))
                .setPackage(CHATGPT_PACKAGE)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (voice.resolveActivity(getPackageManager()) == null
                || !SearchLauncher.startSafely(
                        this,
                        voice,
                        "chatgpt-voice")) {
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

        try {
            pendingCameraUri = getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values);
        } catch (RuntimeException e) {
            Log.w(TAG, "camera_insert_failed cause="
                    + e.getClass().getSimpleName());
            pendingCameraUri = null;
        }

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
        } catch (ActivityNotFoundException | SecurityException e) {
            Log.w(TAG, "camera_launch_failed cause="
                    + e.getClass().getSimpleName());
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
        } catch (ActivityNotFoundException | SecurityException e) {
            Log.w(TAG, "photo_picker_launch_failed cause="
                    + e.getClass().getSimpleName());
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
        } catch (ActivityNotFoundException | SecurityException e) {
            Log.w(TAG, "dictation_launch_failed cause="
                    + e.getClass().getSimpleName());
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
            if (resultCode == RESULT_OK
                    && pendingCameraUri != null
                    && publishCameraUri()) {
                Uri image = pendingCameraUri;
                pendingCameraUri = null;
                shareImage(image);
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
                SearchLauncher.startSafely(
                        this,
                        new Intent(this, ChatGptNewChatActivity.class)
                                .putExtra(
                                        ChatGptNewChatActivity.EXTRA_PROMPT,
                                        results.get(0)),
                        "chatgpt-dictation-result");
            }
            finish();
        }
    }

    private boolean publishCameraUri() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.IS_PENDING, 0);
        try {
            return getContentResolver().update(
                    pendingCameraUri,
                    values,
                    null,
                    null) > 0;
        } catch (RuntimeException e) {
            Log.w(TAG, "camera_publish_failed cause="
                    + e.getClass().getSimpleName());
            return false;
        }
    }

    private void shareImage(Uri image) {
        Intent share = new Intent(Intent.ACTION_SEND)
                .setType("image/*")
                .putExtra(Intent.EXTRA_STREAM, image)
                .setPackage(CHATGPT_PACKAGE)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        share.setClipData(ClipData.newRawUri("image", image));

        if (share.resolveActivity(getPackageManager()) == null
                || !SearchLauncher.startSafely(
                        this,
                        share,
                        "chatgpt-image-share")) {
            SearchLauncher.openAppHome(this, ProviderTarget.CHATGPT);
        }
        finish();
    }

    private void cleanupCameraUri() {
        if (pendingCameraUri == null) {
            return;
        }

        try {
            getContentResolver().delete(
                    pendingCameraUri,
                    null,
                    null);
        } catch (RuntimeException e) {
            Log.w(TAG, "camera_cleanup_failed cause="
                    + e.getClass().getSimpleName());
        } finally {
            pendingCameraUri = null;
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}
