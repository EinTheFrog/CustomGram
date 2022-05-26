package com.example.customgram;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.customgram.databinding.MessageListFragmentBinding;

import org.drinkless.td.libcore.telegram.TdApi;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageListFragment extends Fragment {
    private static final String TAG = "MESSAGE_LIST_FRAGMENT";
    private static final int REQUEST_READ_EXTERNAL_STORAGE_PERMISSION = 0;

    private final ChatManager chatManager = ChatManager.getInstance();
    private List<TdApi.Message> messages;
    private Map<Long, TdApi.User> users;
    private final Map<Long, List<TdApi.Message>> messagesWithoutTitle = new HashMap<>();
    private MessageRecyclerViewAdapter mMessageRecyclerAdapter;
    private AppCompatActivity activity;
    private MessageListFragmentBinding binding;
    private String imagePath = null;

    private ActivityResultLauncher<String> permissionRequestLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            permissionGranted -> {
                if (permissionGranted) {
                    loadImageFromGallery();
                } else {
                    Log.e(TAG, "Permission isn't granted");
                }
            }
    );

    private ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Intent intent = result.getData();
                if (intent == null) return;
                Uri imgUri = intent.getData();
                copyPhotoFromGallery(imgUri, Example.getPhotosDir());
            }
    );

    public MessageListFragment() {}

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        activity = (AppCompatActivity) getActivity();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        messages = chatManager.getMessages();
        users = chatManager.getUsers();
        String chatName = chatManager.getCurrentChat().title;
        mMessageRecyclerAdapter = new MessageRecyclerViewAdapter(
                messages,
                chatName,
                chatManager.getCurrentChat().type
        );
        chatManager.setOnNewMessage(this::updateNewMessage);
        chatManager.setOnNewUser(this::updateNewUser);
        chatManager.setOnNewMessages(this::updateNewMessages);
        chatManager.setOnMessageUpdate(this::updateOldMessage);
        mMessageRecyclerAdapter.setMessageNameCallback(this::getMessageSenderName);

        binding = MessageListFragmentBinding.inflate(
                inflater,
                container,
                false
        );

        Context context = binding.getRoot().getContext();
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setReverseLayout(true);
        binding.recyclerMessages.setLayoutManager(llm);
        binding.recyclerMessages.setAdapter(mMessageRecyclerAdapter);

        binding.sendMessageButton.setOnClickListener(view -> {
            String text = binding.newMessageText.getText().toString();
            binding.newMessageText.setText("");
            if (imagePath == null) {
                Example.executeSendMessage(text);
            } else {
                Example.executeSendMessage(text, imagePath);
            }
            imagePath = null;
            binding.pinnedImage.setVisibility(View.GONE);
        });

        binding.pinImageButton.setOnClickListener(view -> {
            tryToLoadImage();
            binding.pinnedImage.setVisibility(View.VISIBLE);
        });

        binding.pinnedImage.setVisibility(View.GONE);

        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        chatManager.clearMessages();
        Example.clearMessages();
    }

    private void tryToLoadImage() {
        int permissionState = ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_EXTERNAL_STORAGE
        );
        if (permissionState == PackageManager.PERMISSION_GRANTED) {
            loadImageFromGallery();
        } else {
            permissionRequestLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    private void updateNewMessage(TdApi.Message message) {
        long senderUserId = ((TdApi.MessageSenderUser) message.senderId).userId;
        if (!users.containsKey(senderUserId)) {
            if (messagesWithoutTitle.containsKey(senderUserId)) {
                messagesWithoutTitle.get(senderUserId).add(message);
            } else {
                List<TdApi.Message> newList = new ArrayList<>();
                newList.add(message);
                messagesWithoutTitle.put(senderUserId, newList);
            }
        }
        activity.runOnUiThread(() -> {
            if (messages.contains(message)) return;
            messages.add(0, message);
            mMessageRecyclerAdapter.notifyItemInserted(0);
            binding.recyclerMessages.scrollToPosition(0);
        });
    }

    private void updateNewMessages(TdApi.Message[] newMessages) {
        for (int i = newMessages.length - 1; i >= 0; i--) {
            updateNewMessage(newMessages[i]);
        }
    }

    private void updateOldMessage(TdApi.Message message) {
        activity.runOnUiThread(() -> {
            int pos = messages.indexOf(message);
            mMessageRecyclerAdapter.notifyItemChanged(pos);
        });
    }

    private void updateNewUser(TdApi.User user) {
        users.put(user.id, user);
        activity.runOnUiThread(() -> {
            if (!messagesWithoutTitle.containsKey(user.id)) return;
            for (TdApi.Message message: messagesWithoutTitle.get(user.id)) {
                int pos = messages.indexOf(message);
                mMessageRecyclerAdapter.notifyItemChanged(pos);
            }
            messagesWithoutTitle.remove(user.id);
        });
    }

    private String getMessageSenderName(long userId) {
        if (!users.containsKey(userId)) return "";
        TdApi.User user = users.get(userId);
        return user.firstName + " " + user.lastName;
    }

    private void loadImageFromGallery() {
        Intent intent = new Intent(
                Intent.ACTION_OPEN_DOCUMENT,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        );
        galleryLauncher.launch(intent);
    }

    private void copyPhotoFromGallery(Uri uri, String dir) {
        imagePath = dir + getFileName(uri);
        try (FileOutputStream out = new FileOutputStream(imagePath)) {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), uri);
            binding.pinnedImage.setImageBitmap(bitmap);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }

    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index < 0) return "";
                    result = cursor.getString(index);
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void pinImage(String path) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);
        binding.pinnedImage.setImageBitmap(bitmap);
        binding.pinnedImage.setVisibility(View.VISIBLE);
    }
}
