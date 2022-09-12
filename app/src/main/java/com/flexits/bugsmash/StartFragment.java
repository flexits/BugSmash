package com.flexits.bugsmash;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.flexits.bugsmash.databinding.FragmentFirstBinding;

public class StartFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonFirst.setOnClickListener(view1 -> NavHostFragment.findNavController(StartFragment.this)
                .navigate(R.id.action_FirstFragment_to_SecondFragment));

        binding.buttonPlay.setOnClickListener(view1 -> btnPlayPress(this.getView()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void btnPlayPress(View view){
        Intent gameIntent = new Intent(binding.getRoot().getContext().getApplicationContext(), GameActivity.class);
        //gameIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        gameIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(gameIntent);
        //startActivityForResult(gameIntent, 0);
        //TODO pass gameView and gameLoopThread into the activity
    }

}