package com.example.customgram;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.customgram.databinding.NewGroupFragmentBinding;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.List;
import java.util.Map;

public class NewGroupFragment extends Fragment {
    private final ChatManager chatManager = ChatManager.getInstance();
    private ChatsActivity activity;
    private List<TdApi.User> users;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (ChatsActivity) getActivity();

        users = chatManager.getUsers();
        chatManager.addOnNewUser(this::updateNewUser);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        NewGroupFragmentBinding binding = NewGroupFragmentBinding.inflate(
                inflater,
                container,
                false
        );

        UserRecyclerViewAdapter userAdapter = new UserRecyclerViewAdapter(users);
        binding.recyclerUsers.setAdapter(userAdapter);
        binding.recyclerUsers.setLayoutManager(new LinearLayoutManager(activity));

        return binding.getRoot();
    }

    private void updateNewUser(TdApi.User user) {
        users.add(user);
        activity.runOnUiThread(() -> {

        });
    }
}
