package dev.rjsmith.matches;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;

public class CaptureFirstActivity extends AppCompatActivity {

    private CameraX.LensFacing lensFacing = CameraX.LensFacing.BACK;
    private String TAG = "MainActivity";
    private TextureView texture;
    private Button captureBtn;
    private Button start;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_first);

        texture = findViewById(R.id.texture);
        captureBtn = findViewById(R.id.btn_take_picture);
        start = findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        texture.post(new Runnable() {
            @Override
            public void run() {
                startCamera();
            }
        });

        verifyStoragePermissions(this);

        texture.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                updateTransform();
            }
        });
    }

    private void startCamera() {

        DisplayMetrics metrics = new DisplayMetrics();
        texture.getDisplay().getRealMetrics(metrics);
        Size screenSize = new Size(metrics.widthPixels, metrics.heightPixels);
        Rational screenAspectRatio = new Rational(metrics.widthPixels, metrics.heightPixels);

        PreviewConfig previewConfig = new PreviewConfig.Builder()
                .setLensFacing(lensFacing)
                .setTargetResolution(screenSize)
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                .setTargetRotation(texture.getDisplay().getRotation())
                .build();

        Preview preview = new Preview(previewConfig);
        preview.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {
            @Override
            public void onUpdated(Preview.PreviewOutput output) {

                ViewGroup parent = (ViewGroup) texture.getParent();
                parent.removeView(texture);
                parent.addView(texture, 0);

                texture.setSurfaceTexture(output.getSurfaceTexture());
                updateTransform();
            }
        });

        ImageCaptureConfig imageCaptureConfig = new ImageCaptureConfig.Builder()
                .setLensFacing(lensFacing)
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(texture.getDisplay().getRotation())
                .setCaptureMode(ImageCapture.CaptureMode.MAX_QUALITY)
                .build();

        final ImageCapture imageCapture = new ImageCapture(imageCaptureConfig);
        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File(
                        getExternalMediaDirs()[0], "pano_" + System.currentTimeMillis() + ".jpg");
                imageCapture.takePicture(file, new ImageCapture.OnImageSavedListener() {
                    @Override
                    public void onImageSaved(@NonNull File file) {
                        String msg = "Photo capture successfully: " + file.getAbsolutePath();
                        Toast.makeText(CaptureFirstActivity.this, msg, Toast.LENGTH_SHORT).show();

                        // Go to next screen
                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                        i.putExtra(MainActivity.FIRST_PHOTO, file.getAbsolutePath());
                        startActivity(i);
                    }

                    @Override
                    public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {
                        String msg = "Photo capture failed: $message";
                        Toast.makeText(CaptureFirstActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        CameraX.bindToLifecycle(this, preview, imageCapture);
    }

    private void updateTransform() {
        Matrix matrix = new Matrix();
        float denom = 2;
        float centerX = texture.getWidth() / denom;
        float centerY = texture.getHeight() / denom;

        int rotationDegrees = 0;
        switch (texture.getDisplay().getRotation()) {
            case Surface.ROTATION_0:
                rotationDegrees = 0;
                break;
            case Surface.ROTATION_90:
                rotationDegrees = -90;
                break;
            case Surface.ROTATION_180:
                rotationDegrees = -180;
                break;
            case Surface.ROTATION_270:
                rotationDegrees = -270;
                break;
        }
        matrix.postRotate((float) (rotationDegrees), centerX, centerY);
        texture.setTransform(matrix);
    }

    public static void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}
