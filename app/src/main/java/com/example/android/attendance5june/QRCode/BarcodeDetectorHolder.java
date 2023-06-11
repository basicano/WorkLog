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
    static BarcodeDetector getBarcodeDetector(Context context) {
        if (detector == null) {
            detector = new BarcodeDetector.Builder(context.getApplicationContext()).setBarcodeFormats(
                    Barcode.QR_CODE).build();
        }
        return detector;
    }
}


