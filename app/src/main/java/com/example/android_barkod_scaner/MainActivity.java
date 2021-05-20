package com.example.android_barkod_scaner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.gson.internal.$Gson$Preconditions;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListenerAdapter;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.util.List;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {

    CameraView  cameraView;
    Button btnDetect;
    AlertDialog waitingDialog;
   @Override
   protected void onResume(){
       super.onResume();
       cameraView.start();;
   }
   @Override
   protected void onPause(){
       super.onPause();
       cameraView.stop();
   }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        cameraView =(CameraView)findViewById(R.id.cameraview);
        btnDetect=(Button)findViewById(R.id.btn_detect);
        waitingDialog= new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("please wait")
                .setCancelable(false)
                .build();


        btnDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.start();
                cameraView.captureImage();
            }
        });

        cameraView.addCameraKitListener(new CameraKitEventListenerAdapter() {
            @Override
            public void onEvent(CameraKitEvent event) {
                super.onEvent(event);
            }

            @Override
            public void onError(CameraKitError error) {
                super.onError(error);
            }

            @Override
            public void onImage(CameraKitImage image) {
                waitingDialog.show();
                Bitmap bitmap= image.getBitmap();
                bitmap = Bitmap.createScaledBitmap(bitmap,cameraView.getWidth(),cameraView.getHeight(),false);
                cameraView.stop();
                runDetector(bitmap);


            }

            @Override
            public void onVideo(CameraKitVideo video) {
                super.onVideo(video);
            }
        });


    }
    private void runDetector(Bitmap bitmap){
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionBarcodeDetectorOptions options= new FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(
                        FirebaseVisionBarcode.FORMAT_QR_CODE,
                        FirebaseVisionBarcode.FORMAT_PDF417

                )
                .build();
        FirebaseVisionBarcodeDetector detector= FirebaseVision.getInstance().getVisionBarcodeDetector(options);
        detector.detectInImage(image)
            .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
            @Override
                    public void onSuccess(List<FirebaseVisionBarcode> firebaseVisionBarcodes){
                        processResult(firebaseVisionBarcodes);
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });

    }
    private void processResult(List<FirebaseVisionBarcode>firebaseVisionBarcodes){
       for(FirebaseVisionBarcode barcode :firebaseVisionBarcodes) {
           int valueType = barcode.getValueType();
           // See API reference for complete list of supported types
           switch (valueType) {
               case FirebaseVisionBarcode.TYPE_WIFI:
                   String ssid = barcode.getWifi().getSsid();
                   String password = barcode.getWifi().getPassword();
                   int type = barcode.getWifi().getEncryptionType();
                   break;
               case FirebaseVisionBarcode.TYPE_URL:
                   //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getRawValue()));
                  // startActivity(intent);
                   String title = barcode.getUrl().getTitle();
                   String url = barcode.getUrl().getUrl();
                   break;
               default:
                   throw new IllegalStateException("Unexpected value: " + valueType);
           }

       }
       waitingDialog.dismiss();
    }
}