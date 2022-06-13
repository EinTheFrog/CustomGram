package com.example.customgram;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.customgram.databinding.NewGroupFragmentBinding;

public class NewGroupFragment extends Fragment {
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
        return binding.getRoot();
    }
}
