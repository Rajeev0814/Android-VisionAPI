package com.example.visionapi.android;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class QRScanActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST_CODE = 102;
    SurfaceView cameraView;
    BarcodeDetector barcode;
    CameraSource cameraSource;
    SurfaceHolder holder;
    View view_scanner;
    Handler handler;
    private Animation mAnimation;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        initialiseView();
        handler = new Handler();
        context = this;


        cameraView.setZOrderMediaOverlay(true);
        holder = cameraView.getHolder();
        barcode = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();
        if (!barcode.isOperational()) {
            Toast.makeText(getApplicationContext(), "Sorry, Couldn't setup the detector", Toast.LENGTH_LONG).show();
            this.finish();
        }
        cameraSource = new CameraSource.Builder(this, barcode)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(24)
                .setRequestedPreviewSize(320, 240)
                .build();
        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                startQrCodeScanner();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (cameraSource != null) {
                    cameraSource.stop();
                }
            }
        });
        barcode.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }


            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {

                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() > 0) {
                    Barcode obj_barcode = barcodes.valueAt(0);
                    String barcode = obj_barcode.displayValue;
                    if (!TextUtils.isEmpty(barcode) && barcode.contains("meeting_room_id:")) {
                        final String finalBarcode = barcode;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (cameraSource != null) {
                                    cameraSource.stop();
                                }
                                Toast.makeText(context, finalBarcode, Toast.LENGTH_SHORT).show();
                                String barcode = finalBarcode;
                                barcode = barcode.replace("meeting_room_id:", "");
                                // callprocessTogetMeetingRoomAvailability(barcode);
                            }
                        });


                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "Please scan a valid QR Code", Toast.LENGTH_SHORT).show();
                                if (cameraSource != null) {
                                    cameraSource.stop();
                                }
                            }
                        });

                    }

                }
            }
        });
    }


    private void startQrCodeScanner() {

        try {
            cameraSource.start(cameraView.getHolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
        float density = getResources().getDisplayMetrics().density;
        mAnimation = new TranslateAnimation(0, 0, 5 * density, -(250f - 5f) * density);

        mAnimation.setDuration(2000);
        mAnimation.setFillAfter(false);
        mAnimation.setRepeatCount(-1);
        mAnimation.setRepeatMode(Animation.REVERSE);
        view_scanner.setAnimation(mAnimation);
        mAnimation.start();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (cameraSource != null) {
                    cameraSource.stop();
                }
            }
        }, 30000);
    }


    private void initialiseView() {
        cameraView = (SurfaceView) findViewById(R.id.cameraView);
        view_scanner = (View) findViewById(R.id.view_scanner);
        TextView txt_header = (TextView) findViewById(R.id.txt_scan_back);
        // FontsOverride.setBoldOxygenFont(txt_header);

        LinearLayout layout_back = (LinearLayout) findViewById(R.id.lay_scan_qr_code_back);
        layout_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
            cameraSource = null;


        }
    }
}
