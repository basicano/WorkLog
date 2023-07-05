package com.example.android.attendance5june.QRCode;

import android.content.Context;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

class BarcodeDetectorHolder {
    private static BarcodeDetector detector;

    /**
     * Gets barcode detector.
     *
     * @param context
     *     the context
     * @return the barcode detector
     */

    // to ensure that only one instance of BarcodeDetector is created and reused throughout the application. 
    // By using the static getBarcodeDetector method, other parts of the code can easily obtain the BarcodeDetector instance without having to manage its creation and initialization.
    static BarcodeDetector getBarcodeDetector(Context context) {
        // Context object is typically used to access resources and information about the application's environment.
        if (detector == null) {
            // new instance of BarcodeDetector is created using the BarcodeDetector.Builder class. 
            // The builder is configured with the application context obtained from the parameter, and the barcode format is set to Barcode.QR_CODE.
            detector = new BarcodeDetector.Builder(context.getApplicationContext()).setBarcodeFormats(
                    Barcode.QR_CODE).build();
        }
        return detector;
    }
}


