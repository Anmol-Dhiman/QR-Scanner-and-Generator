package com.example.qrcodescannergenerator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.transition.Scene;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.view.View;

import com.example.qrcodescannergenerator.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {


    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        replaceFragment(new Scan());


        Scene scan = Scene.getSceneForLayout(binding.frameLayout, R.layout.fragment_scan, this);
        Scene generate = Scene.getSceneForLayout(binding.frameLayout, R.layout.fragment_generate, this);
        scan.enter();


        binding.scan.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                binding.scan.setTextColor(MainActivity.this.getResources().getColor(R.color.buttonColor));
                binding.scan.setBackgroundResource(R.drawable.bottom_nav_buttons);
                binding.generate.setTextColor(MainActivity.this.getResources().getColor(R.color.white));
                binding.generate.setBackgroundResource(0);
                TransitionManager.go(scan, TransitionInflater.from(MainActivity.this).inflateTransition(R.transition.fade));
                replaceFragment(new Scan());
            }
        });
        binding.generate.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                TransitionManager.go(generate, TransitionInflater.from(MainActivity.this).inflateTransition(R.transition.fade));
                binding.generate.setTextColor(MainActivity.this.getResources().getColor(R.color.buttonColor));
                binding.generate.setBackgroundResource(R.drawable.bottom_nav_buttons);

                binding.scan.setTextColor(MainActivity.this.getResources().getColor(R.color.white));
                binding.scan.setBackgroundResource(0);
                replaceFragment(new Generate());
            }
        });


    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, fragment).commit();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Do you want to exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                }).setNeutralButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
    }
}