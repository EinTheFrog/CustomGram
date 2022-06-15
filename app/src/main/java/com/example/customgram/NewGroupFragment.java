package com.example.customgram;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customgram.databinding.NewGroupFragmentBinding;

import org.drinkless.td.libcore.telegram.TdApi;

import java.util.ArrayList;
import java.util.List;

public class NewGroupFragment extends Fragment {
    private final ChatManager chatManager = ChatManager.getInstance();
    private ChatsActivity activity;
    private List<TdApi.User> users;
    private List<TdApi.User> selectedUsers;
    private UserRecyclerViewAdapter userAdapter;
    private SelectedUserRecyclerViewAdapter selectedUserAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (ChatsActivity) getActivity();

        users = chatManager.getUsers();
        selectedUsers = new ArrayList<>();
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

        userAdapter = new UserRecyclerViewAdapter(users);
        userAdapter.setOnUserClicked(this::onUserClicked);
        binding.recyclerUsers.setAdapter(userAdapter);
        binding.recyclerUsers.setLayoutManager(new LinearLayoutManager(activity));

        selectedUserAdapter = new SelectedUserRecyclerViewAdapter(selectedUsers);
        selectedUserAdapter.setOnUserClicked(this::unselectUser);
        binding.recyclerSelectedUsers.setAdapter(selectedUserAdapter);
        LinearLayoutManager lm = new LinearLayoutManager(activity);
        lm.setOrientation(RecyclerView.HORIZONTAL);
        binding.recyclerSelectedUsers.setLayoutManager(lm);

        return binding.getRoot();
    }

    private void updateNewUser(TdApi.User user) {
        users.add(user);
        activity.runOnUiThread(() -> {
            int pos = users.indexOf(user);
            userAdapter.notifyItemInserted(pos);
        });
    }

    private void onUserClicked(int pos) {
        TdApi.User user = users.get(pos);
        if (selectedUsers.contains(user)) {
            pos = selectedUsers.indexOf(user);
            unselectUser(pos);
        } else {
            selectUser(pos);
        }
    }

    private void selectUser(int pos) {
        activity.runOnUiThread(() -> {
            TdApi.User user = users.get(pos);
            if (selectedUsers.contains(user)) return;
            selectedUsers.add(user);
            selectedUserAdapter.notifyItemInserted(selectedUsers.size() - 1);
        });
    }

    private void unselectUser(int pos) {
        activity.runOnUiThread(() -> {
            TdApi.User user = selectedUsers.get(pos);
            if (!selectedUsers.contains(user)) return;
            selectedUsers.remove(user);
            selectedUserAdapter.notifyItemRemoved(pos);
        });
    }
}
