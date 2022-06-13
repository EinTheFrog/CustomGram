package com.example.customgram;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.customgram.databinding.ToolbarChatInfoFragmentBinding;

import org.drinkless.td.libcore.telegram.TdApi;

public class ToolbarChatInfoFragment extends Fragment {
    public ToolbarChatInfoFragment() {
        super(R.layout.toolbar_default_fragment);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        ToolbarChatInfoFragmentBinding binding = ToolbarChatInfoFragmentBinding.inflate(
                inflater,
                container,
                false
        );
        ChatManager chatManager = ChatManager.getInstance();
        TdApi.Chat chat = chatManager.getCurrentChat();
        if (chat != null) {
            binding.toolbarChatTitle.setText(chat.title);
            setPhoto(chat, binding);
        }

        return binding.getRoot();
    }

    private void setPhoto(TdApi.Chat chat, ToolbarChatInfoFragmentBinding binding) {
        String photoPath = chat.photo == null ? "" : chat.photo.small.local.path;
        ProfilePhotoHelper.setPhoto(
                photoPath,
                chat.title,
                binding.toolbarChatPhoto,
                binding.toolbarAltChatPhoto
        );
    }
}
