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

        ChatsActivity activity = (ChatsActivity) getActivity();
        binding.newGroupButton.setOnClickListener(view -> {
            if (activity == null) return;
            activity.openNewGroup();
        });

        return binding.getRoot();
    }
}
