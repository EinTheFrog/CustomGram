package com.example.customgram;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.customgram.databinding.ToolbarChatInfoFragmentBinding;
import com.example.customgram.databinding.ToolbarDefaultFragmentBinding;

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
            setTitle(chat, binding);
        }

        return binding.getRoot();
    }

    private void setTitle(TdApi.Chat chat, ToolbarChatInfoFragmentBinding binding) {
        if (chat.photo != null && !chat.photo.small.local.path.equals("")) {
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(chat.photo.small.local.path, bmOptions);
            binding.toolbarChatImg.setImageBitmap(bitmap);
            binding.toolbarAltChatImg.setText("");
        } else {
            binding.toolbarChatImg.setImageDrawable(null);
            Context photoContext = binding.toolbarChatImg.getContext();
            binding.toolbarChatImg.setBackgroundColor(
                    ContextCompat.getColor(photoContext, R.color.pink)
            );
            binding.toolbarAltChatImg.setText(ChatAltPhotoHelper.getChatInitials(chat));
        }
    }
}
