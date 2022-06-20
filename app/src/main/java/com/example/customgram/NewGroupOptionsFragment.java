package com.example.customgram;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
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
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.customgram.databinding.NewGroupOptionsFragmentBinding;

import org.drinkless.td.libcore.telegram.TdApi;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class NewGroupOptionsFragment extends Fragment {
    private static final String TAG = "NEW_GROUP_OPTIONS_FRAGMENT";

    private ChatsActivity activity;
    private NewGroupOptionsFragmentBinding binding;
    private String chatPhotoPath = null;

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
                Uri imageUri = intent.getData();
                copyPhotoFromGallery(imageUri, Example.getPhotosDir());
            }
    );

    @Override
    public void onCreate(Bundle savedStateInstance) {
        super.onCreate(savedStateInstance);
        activity = (ChatsActivity) getActivity();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = NewGroupOptionsFragmentBinding.inflate(
                inflater,
                container,
                false
        );

        List<TdApi.User> users = ChatManager.getInstance().getNewGroupUsers();
        UserRecyclerViewAdapter userAdapter = new UserRecyclerViewAdapter(users);
        binding.recyclerUsers.setAdapter(userAdapter);
        binding.recyclerUsers.setLayoutManager(new LinearLayoutManager(activity));

        binding.groupPhoto.setOnClickListener(view -> tryToLoadImage());
        binding.fab.setOnClickListener(view -> {
            String title = binding.groupTitle.getText().toString();
            if (title.equals("")) {
                return;
            }
            long[] userIds = new long[users.size()];
            for (int i = 0; i < users.size(); i++) {
                userIds[i] = users.get(i).id;
            }
            activity.createGroup(userIds, title, chatPhotoPath);
        });

        NavController navController = Navigation.findNavController(
                activity,
                R.id.nav_host_fragment
        );
        AppBarConfiguration.Builder appBarConfBuilder =
                new AppBarConfiguration.Builder(navController.getGraph());
        AppBarConfiguration appBarConfiguration = appBarConfBuilder.build();
        NavigationUI.setupWithNavController(
                binding.customToolbar,
                navController,
                appBarConfiguration
        );

        return binding.getRoot();
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

    private void loadImageFromGallery() {
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        );
        galleryLauncher.launch(intent);
    }

    private void copyPhotoFromGallery(Uri uri, String dir) {
        chatPhotoPath = dir + getFileName(uri);
        try (FileOutputStream out = new FileOutputStream(chatPhotoPath)) {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), uri);
            binding.groupPhoto.setImageBitmap(bitmap);
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
}
