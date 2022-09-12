package com.flexits.bugsmash;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.flexits.bugsmash.databinding.FragmentFirstBinding;

import java.util.ArrayList;

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

        //populate the list of entities
        MobSpecies ms1 = new MobSpecies(BitmapFactory.decodeResource(getResources(), R.drawable.spider_40px));
        Mob mb1 = new Mob(1, 10, 0, true, ms1);
        GameGlobal gameGlobal = (GameGlobal) this.binding.getRoot().getContext().getApplicationContext();
        ArrayList<Mob> mobs = gameGlobal.getMobs();
        if (mobs.size() <= 0) gameGlobal.getMobs().add(mb1);

        Intent gameIntent = new Intent(view.getContext(), GameActivity.class);
        //StartFragment.this.startActivity(gameIntent);
        //gameIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //gameIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(gameIntent);
        //startActivityIfNeeded(gameIntent, 0);
        //startActivityForResult(gameIntent, 0);
    }

}