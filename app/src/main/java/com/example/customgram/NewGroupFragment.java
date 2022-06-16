package com.example.customgram;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
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
    private NewGroupFragmentBinding binding;
    private NavController navController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (ChatsActivity) getActivity();

        navController = Navigation.findNavController(
                activity,
                R.id.nav_host_fragment
        );

        users = chatManager.getUsers();
        selectedUsers = chatManager.getNewGroupUsers();
        chatManager.addOnNewUser(this::updateNewUser);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = NewGroupFragmentBinding.inflate(
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

        binding.fab.setOnClickListener(view -> {
            navController.navigate(R.id.action_new_group_fragment_to_new_group_options_fragment);
        });
        if (!selectedUsers.isEmpty()) {
            binding.fab.setVisibility(View.VISIBLE);
        }

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        chatManager.clearNewGroupUsers();
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
            ChatManager.getInstance().addNewGroupUser(user);
            if (selectedUsers.contains(user)) return;
            selectedUsers.add(user);
            selectedUserAdapter.notifyItemInserted(selectedUsers.size() - 1);

            if (selectedUsers.size() == 1) {
                binding.fab.setVisibility(View.VISIBLE);
            }
        });
    }

    private void unselectUser(int pos) {
        activity.runOnUiThread(() -> {
            TdApi.User user = selectedUsers.get(pos);
            ChatManager.getInstance().removeNewGroupUser(user);
            if (!selectedUsers.contains(user)) return;
            selectedUsers.remove(user);
            selectedUserAdapter.notifyItemRemoved(pos);

            if (selectedUsers.size() == 0) {
                binding.fab.setVisibility(View.GONE);
            }
        });
    }
}
