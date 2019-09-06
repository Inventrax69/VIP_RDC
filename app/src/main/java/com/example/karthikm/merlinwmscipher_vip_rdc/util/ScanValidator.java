package com.example.karthikm.merlinwmscipher_vip_rdc.util;

/**
 * Created by karthik.m on 05/11/2018.
 */

public class ScanValidator {

    public static boolean IsItemScanned(String scannedData) {
        if (scannedData.split("[-]").length == 2) {
            return true;
        } else {
            return false;
        }
    }

    public static Boolean IsContainerScanned(String scanneddata) {
        if (scanneddata.length() == 14) {
            return true;
        } else {
            return false;
        }
    }

    public static Boolean IsPalletScanned(String scanneddata) {
        if (scanneddata.length() == 13 || (scanneddata.substring(3) == "P")) {
            return true;
        } else {
            return false;
        }
    }

    public static Boolean IsDockScanned(String scanneddata) {
        if (scanneddata.split("[-]").length > 2 && scanneddata.startsWith("00-00-")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean IsLocationScanned(String scannedData) {
        if (scannedData.split("[-]").length > 2) {
            return true;
        } else {
            return false;
        }
    }

}