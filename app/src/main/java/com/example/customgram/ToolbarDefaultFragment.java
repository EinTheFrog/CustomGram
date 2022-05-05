package com.example.customgram;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.customgram.databinding.ToolbarDefaultFragmentBinding;

public class ToolbarDefaultFragment extends Fragment {

    public ToolbarDefaultFragment() {
        super(R.layout.toolbar_default_fragment);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        ToolbarDefaultFragmentBinding binding = ToolbarDefaultFragmentBinding.inflate(
                inflater,
                container,
                false
        );
        return binding.getRoot();
    }
}
