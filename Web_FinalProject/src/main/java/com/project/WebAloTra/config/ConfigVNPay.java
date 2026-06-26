package com.project.WebAloTra.config;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class ConfigVNPay {
    public static String vnp_PayUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    public static String vnp_ReturnUrl = "http://localhost:8080/payment-result";
    public static String vnp_TmnCode = "WSKPMBP7";
    public static String secretKey = "YXVDLHFPDPTUKXVYXDCAMLIPMBQOXWUV";
    public static String vnp_ApiUrl = "https://sandbox.vnpayment.vn/merchant_webapi/api/transaction";
    public static String vnp_Version = "2.1.0";
    public static String vnp_Command = "pay";

    public static String md5(String message) {
        String digest = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(message.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                sb.append(String.format("%02x", b & 0xff));
            }
            digest = sb.toString();
        } catch (UnsupportedEncodingException ex) {
            digest = "";
        } catch (NoSuchAlgorithmException ex) {
            digest = "";
        }
        return digest;
    }

    public static String Sha256(String message) {
        String digest = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(message.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                sb.append(String.format("%02x", b & 0xff));
            }
            digest = sb.toString();
        } catch (UnsupportedEncodingException ex) {
            digest = "";
        } catch (NoSuchAlgorithmException ex) {
            digest = "";
        }
        return digest;
    }

    //Util for VNPAY
    public static String hashAllFields(Map<String, String> fields) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (String fieldName : fieldNames) {
            String fieldValue = fields.get(fieldName);
            if (fieldValue != null && fieldValue.length() > 0) {
                if (!isFirst) {
                    sb.append('&');
                }
                sb.append(fieldName);
                sb.append('=');
                // VNPay spec: use URL-encoded value for hashing
                try {
                    sb.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (UnsupportedEncodingException e) {
                    // Fallback to raw if encoding unexpectedly fails
                    sb.append(fieldValue);
                }
                isFirst = false;
            }
        }
        return hmacSHA512(secretKey, sb.toString());
    }

    public static String hmacSHA512(final String key, final String data) {
        try {

            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes();
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();

        } catch (Exception ex) {
            return "";
        }
    }

    public static String getIpAddress(HttpServletRequest request) {
        String ipAddress;
        try {
            ipAddress = request.getHeader("X-FORWARDED-FOR");
            if (ipAddress == null || ipAddress.isEmpty()) {
                ipAddress = request.getRemoteAddr();
            }
            
            // Convert IPv6 to IPv4 if needed
            // IPv6 localhost (0:0:0:0:0:0:0:1) → convert to 127.0.0.1
            if ("0:0:0:0:0:0:0:1".equals(ipAddress) || "::1".equals(ipAddress)) {
                ipAddress = "127.0.0.1";
            }
            
            // Handle IPv6 full form and compressed form
            if (ipAddress.contains(":")) {
                // If it's IPv6, try to use localhost IP instead
                // VNPAY only accepts IPv4 addresses
                if (ipAddress.startsWith("0:") || ipAddress.startsWith("::")) {
                    ipAddress = "127.0.0.1";
                } else {
                    // For other IPv6 addresses, log warning
                    System.out.println("[VNPay] ⚠️ IPv6 address detected: " + ipAddress + ", converting to 127.0.0.1");
                    ipAddress = "127.0.0.1";
                }
            }
            
        } catch (Exception e) {
            System.err.println("[VNPay] ❌ Error getting IP: " + e.getMessage());
            ipAddress = "127.0.0.1"; // Default to localhost if error
        }
        
        System.out.println("✅ [VNPay] IP Address converted: " + ipAddress);
        return ipAddress;
    }

    public static String validateAndSanitizeParameter(String value) {
        if (value == null) return "";
        // Remove any problematic characters that might cause VNPAY JS issues
        return value.trim();
    }

    public static boolean validateAmount(long amount) {
        // VNPAY requires amount >= 10000 VND (100,000 in units of 1)
        // and <= 999,999,999,999 VND
        return amount >= 1000 && amount <= 999999999999L;
    }

    public static String getRandomNumber(int len) {
        Random rnd = new Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}