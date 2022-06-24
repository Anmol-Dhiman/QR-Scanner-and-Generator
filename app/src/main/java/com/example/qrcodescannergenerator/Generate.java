package com.example.qrcodescannergenerator;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.qrcodescannergenerator.databinding.FragmentGenerateBinding;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;


public class Generate extends Fragment {
    private FragmentGenerateBinding binding;

    private Bitmap bitmap = null;


    @SuppressLint("ResourceAsColor")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentGenerateBinding.inflate(inflater, container, false);


        binding.generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (binding.inputTextUrl.getText().toString().trim().isEmpty()) {
                    Toast.makeText(container.getContext(), "Enter the Text/Url...", Toast.LENGTH_SHORT).show();
                } else {
                    createQR();
                }
            }
        });

        binding.inputTextUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!binding.inputTextUrl.getText().toString().trim().isEmpty() && binding.text.getVisibility() == View.GONE) {
                    createQR();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.jpgDownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (bitmap != null) {
                    if (checkPermission())
                        saveImage("jpg");
                    else {
                        Toast.makeText(getContext(), "Storage Permission is not granted", Toast.LENGTH_LONG).show();
                        requestPermission();
                    }
                } else
                    Toast.makeText(getContext(), "Create the QR first.", Toast.LENGTH_SHORT).show();

            }
        });

        binding.svgDownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bitmap != null) {
                    if (checkPermission())
                        saveImage("svg");
                    else {
                        Toast.makeText(getContext(), "Storage Permission is not granted", Toast.LENGTH_LONG).show();
                        requestPermission();
                    }
                } else
                    Toast.makeText(getContext(), "Create the QR first.", Toast.LENGTH_SHORT).show();

            }
        });


        return binding.getRoot();
    }

    private void createQR() {
        String text = binding.inputTextUrl.getText().toString().trim();
        MultiFormatWriter writer = new MultiFormatWriter();
        try {

            BitMatrix matrix = writer.encode(text, BarcodeFormat.QR_CODE, 250, 250);
            BarcodeEncoder encoder = new BarcodeEncoder();
            bitmap = encoder.createBitmap(matrix);
            binding.text.setVisibility(View.GONE);
            binding.imageView.setImageBitmap(bitmap);

        } catch (WriterException e) {
            Toast.makeText(getContext(), "There is some issue please try again.", Toast.LENGTH_SHORT).show();
        }

    }


    private void saveImage(String imageType) {

        String filePath = Environment.getExternalStorageDirectory().toString();
        File dir = new File(filePath + "/qr_images");
        dir.mkdir();
        String fname = "Image-" + binding.inputTextUrl.getText().toString().trim() + "." + imageType;
        File file = new File(dir, fname);

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            Toast.makeText(getContext(), imageType + " save to storage", Toast.LENGTH_SHORT).show();
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions((Activity) getContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
    }


}