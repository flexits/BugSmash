package com.flexits.bugsmash;

import static android.content.SharedPreferences.*;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.flexits.bugsmash.databinding.FragmentFirstBinding;

public class StartFragment extends Fragment {
    private final int ROUND_DURATION = 120000;

    private FragmentFirstBinding binding;
    private SharedPreferences sPref;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        //update fields with saved settings
        sPref = getActivity().getSharedPreferences(
                getResources().getString(R.string.pref_filename),
                Context.MODE_PRIVATE);

        binding.editTxtName.setText(
                sPref.getString(
                        getResources().getString(R.string.pref_usr_name),
                        getResources().getString(R.string.pref_usr_name_default))
        );
        binding.editTxtQuantity.setText(
                sPref.getString(
                        getResources().getString(R.string.pref_mobs_quantity),
                        getResources().getString(R.string.pref_mobs_quantity_default))
        );
        binding.tvScore.setText(getResources().getString(R.string.str_score_line) +
                sPref.getString(
                        getResources().getString(R.string.pref_max_score),
                        getResources().getString(R.string.pref_max_score_default))
        );
        //set global game options
        GameGlobal gameGlobal = (GameGlobal) getActivity().getApplication();
        gameGlobal.setTimerval(ROUND_DURATION);
        gameGlobal.setScore(0);
        gameGlobal.getMobs().clear();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.buttonPlay.setOnClickListener(view1 -> btnPlayPress(this.getView()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void btnPlayPress(View view){
        //update settings with user-provided values
        Editor spEditor =  sPref.edit();
        spEditor.putString(
                getResources().getString(R.string.pref_usr_name),
                binding.editTxtName.getText().toString().trim()
        );
        spEditor.putString(
                getResources().getString(R.string.pref_mobs_quantity),
                binding.editTxtQuantity.getText().toString().trim()
        );
        spEditor.commit();

        Intent gameIntent = new Intent(binding.getRoot().getContext().getApplicationContext(), GameActivity.class);
        gameIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(gameIntent);
    }

}