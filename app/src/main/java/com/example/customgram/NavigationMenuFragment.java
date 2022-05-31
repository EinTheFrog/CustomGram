package com.example.customgram;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.customgram.databinding.NavigationMenuFragmentBinding;

public class NavigationMenuFragment extends Fragment {
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        NavigationMenuFragmentBinding binding
                = NavigationMenuFragmentBinding.inflate(getLayoutInflater());

        ChatsActivity chatsActivity = (ChatsActivity) getActivity();
        NavController navController = Navigation.findNavController(chatsActivity, R.id.nav_host_fragment);

        binding.buttonUserInfo.setOnClickListener(
            view -> navController.navigate(R.id.action_chats_fragment_to_user_info_fragment)
        );

        return binding.getRoot();
    }
}
