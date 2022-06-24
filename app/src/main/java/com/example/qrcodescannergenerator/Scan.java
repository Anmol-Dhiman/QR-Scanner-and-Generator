package com.example.qrcodescannergenerator;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.qrcodescannergenerator.databinding.FragmentScanBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class Scan extends Fragment {

    private ListenableFuture<ProcessCameraProvider> processCameraProviderListenableFuture;
    private AlertDialog alertDialog = null;
    private FragmentScanBinding binding;

    private ActivityResultLauncher<String> permissionResult = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if (result) {
//                           permission granted
                        scan();
                    } else {
                        permissionNotGrantedAlertMessage();
                    }
                }
            }
    );

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentScanBinding.inflate(inflater, container, false);


        if (checkPermission()) scan();
        else permissionResult.launch(Manifest.permission.CAMERA);


        return binding.getRoot();
    }


    private void permissionNotGrantedAlertMessage() {
        new AlertDialog.Builder(getContext())
                .setTitle("Camera Permission Needed!!")
                .setMessage("we need the camera resources to scan the qr")
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestPermission();
                    }
                }).setNegativeButton("dismiss", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        permissionNotGrantedAlertMessage();
                    }
                })
                .show();
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions((Activity) getContext(), new String[]{Manifest.permission.CAMERA}, 101);

    }


    private void scan() {

        processCameraProviderListenableFuture = ProcessCameraProvider.getInstance(getContext());
        processCameraProviderListenableFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = processCameraProviderListenableFuture.get();
                    bindImage(cameraProvider);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }, ContextCompat.getMainExecutor(getContext()));
    }


    private void bindImage(ProcessCameraProvider cameraProvider) {


        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().setTargetResolution(new Size(248, 254)).setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(getContext()), new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy image) {
                @SuppressLint("UnsafeOptInUsageError") Image mediaImage = image.getImage();
                if (mediaImage != null) {
                    InputImage image2 = InputImage.fromMediaImage(mediaImage, image.getImageInfo().getRotationDegrees());
                    BarcodeScanner scanner = BarcodeScanning.getClient();

                    Task<List<Barcode>> results = scanner.process(image2);
                    results.addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                        @Override
                        public void onSuccess(List<Barcode> barcodes) {
                            for (Barcode barcode : barcodes) {
                                final String value = barcode.getRawValue();
                                if (alertDialog == null) {
                                    alertDialog = new AlertDialog.Builder(getContext())
                                            .setTitle("Result..")
                                            .setMessage(value)
                                            .setPositiveButton("Search", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                                                    intent.putExtra(SearchManager.QUERY, value);
                                                    startActivity(intent);
                                                    alertDialog = null;
                                                }
                                            }).setNegativeButton("Continue", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    Toast.makeText(getContext(), "Continue Scanning.", Toast.LENGTH_SHORT).show();
                                                    dialogInterface.dismiss();
                                                    alertDialog = null;
                                                }
                                            }).setNeutralButton("Copy text", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(getContext().CLIPBOARD_SERVICE);
                                                    ClipData clip = ClipData.newPlainText("", "" + value);
                                                    clipboard.setPrimaryClip(clip);
                                                    Toast.makeText(getContext(), "Copied!", Toast.LENGTH_LONG).show();
                                                }
                                            }).show();
                                }

                            }
                            image.close();
                            mediaImage.close();
                        }
                    });

                }
            }
        });
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        preview.setSurfaceProvider(binding.scannerView.getSurfaceProvider());
        cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview);

    }
}