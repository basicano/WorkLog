package com.example.android.attendance5june.QRCode;

public interface QRDataListener {

    /**
     * On detected.
     *
     * @param data
     *     the data
     */
    // Called from not main thread. Be careful
    void onDetected(final String data, final String email);
    //void onDetected(final String data, final String email, final String phn);
}

