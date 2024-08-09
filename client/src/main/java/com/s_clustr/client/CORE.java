package com.s_clustr.client;

import com.google.gson.Gson;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.math.BigInteger;

public class CORE {

    private static final int IV_LENGTH = 16;
    private static final Gson gson = new Gson();

    // AES Key Generation
    private static SecretKey generateAESKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128, new SecureRandom());
        return keyGenerator.generateKey();
    }

    // AES CBC Encryption
    private static String encrypt(String plaintext, SecretKey secretKey) {
        try {
            byte[] iv = generateIV();
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("AES encryption error: " + e.getMessage(), e);
        }
    }

    // AES CBC Decryption
    private static String decrypt(String ciphertext, SecretKey secretKey) {
        try {
            byte[] combined = Base64.getDecoder().decode(ciphertext);
            byte[] iv = Arrays.copyOfRange(combined, 0, IV_LENGTH);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            byte[] encryptedBytes = Arrays.copyOfRange(combined, IV_LENGTH, combined.length);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AES decryption error: " + e.getMessage(), e);
        }
    }

    // Generate IV for AES CBC
    private static byte[] generateIV() {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    // Convert byte array to hex string
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    // Get MD5 hash of input string
    private static String getMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            BigInteger no = new BigInteger(1, messageDigest);
            return String.format("%032X", no);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 hashing error: " + e.getMessage(), e);
        }
    }


    // Get 8-byte hexadecimal representation of current timestamp
    private static String get8ByteTimestampHexString() {
        long currentTimeMillis = System.currentTimeMillis();
        byte[] timestampBytes = new byte[8];
        for (int i = 7; i >= 0; i--) {
            timestampBytes[i] = (byte) (currentTimeMillis & 0xFF);
            currentTimeMillis >>= 8;
        }
        return bytesToHex(timestampBytes);
    }

    public static long hexToTimestamp(String hexString) {
        if (hexString == null || hexString.isEmpty()) {
            return -1; // Or any other default value
        }
        try {
            return Long.parseLong(hexString, 16);
        } catch (NumberFormatException e) {
            return -1; // Or any other default value
        }
    }


    // Convert IPv4 address to hexadecimal representation
    private static String ipToHexString(String ipAddress) {
        StringBuilder hexString = new StringBuilder();
        String[] octets = ipAddress.split("\\.");

        for (String octet : octets) {
            int octetValue = Integer.parseInt(octet);
            hexString.append(String.format("%02X", octetValue));
        }

        while (hexString.length() < 8) {
            hexString.append("00"); // Pad with "00" if less than 4 octets
        }

        return hexString.toString();
    }

    // Get 4-byte hexadecimal representation of input string
    private static String get4ByteHex(String input) {
        byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);
        byte[] fourBytes = Arrays.copyOf(inputBytes, 4);

        StringBuilder hexString = new StringBuilder();
        for (byte b : fourBytes) {
            hexString.append(String.format("%02X", b));
        }

        return hexString.toString();
    }

    // Convert character to hexadecimal string
    private static String convertToHexString(String character) {
        byte byteValue = (byte) character.charAt(0);
        return String.format("%02X", byteValue & 0xFF);
    }

    // Generate control payload including timestamp, IP address, ID, type, and stat
    private static String generateControlPayload(String nodeIp, String id, String type, String stat) {
        return get8ByteTimestampHexString() + ipToHexString(nodeIp) + get4ByteHex(id) + get4ByteHex(type) + convertToHexString(stat);
    }

    // Parse data from hexadecimal string
    private static String parseData(String data) {
        try {
            if (data.length() != 42) {
                throw new IllegalArgumentException("Input data length is insufficient for parsing.");
            }

            String timestampHex = data.substring(0, 16);
            String ipHex = data.substring(16, 24);
            String idHex = data.substring(24, 32);
            String typeHex = data.substring(32, 40);
            String statHex = data.substring(40);

            String timestamp = hexStringToLong(timestampHex);
            String ip = hexToIpAddress(ipHex);
            String id = hexToString(idHex);
            String type = hexToString(typeHex);
            String stat = hexToString(statHex);

            Map<String, String> jsonMap = new HashMap<>();
            jsonMap.put("timestamp", timestamp);
            jsonMap.put("ip", ip);
            jsonMap.put("id", id);
            jsonMap.put("type", type);
            jsonMap.put("stat", stat);

            return gson.toJson(jsonMap);

        } catch (IllegalArgumentException e) {
            return ""; // Handle error appropriately
        }
    }

    // Convert hexadecimal string to long
    private static String hexStringToLong(String hexTimestamp) {
        return Long.toString(Long.parseUnsignedLong(hexTimestamp, 16));
    }

    // Convert hexadecimal string to IPv4 address
    private static String hexToIpAddress(String hex) {
        StringBuilder ipAddress = new StringBuilder();
        for (int i = 0; i < hex.length(); i += 2) {
            String octet = hex.substring(i, i + 2);
            int decimal = Integer.parseInt(octet, 16);
            ipAddress.append(decimal);
            if (i < hex.length() - 2) {
                ipAddress.append(".");
            }
        }
        return ipAddress.toString();
    }

    // Convert hexadecimal string to regular string
    private static String hexToString(String hex) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < hex.length(); i += 2) {
            String str = hex.substring(i, i + 2);
            int value = Integer.parseInt(str, 16);
            if (value != 0) {
                result.append((char) value);
            } else {
                break; // Stop adding characters if encountering "00"
            }
        }
        return result.toString().trim(); // Trim leading and trailing whitespace
    }

    // Generate 1-byte hexadecimal representation of input string
    private static String get1ByteHex(String input) {
        byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);
        byte[] oneByte = Arrays.copyOf(inputBytes, 1);
        return String.format("%02X", oneByte[0]);
    }

    // Generate payload block 1 including timestamp, IP address, ID, type, and stat
    public static String payloadBlock1(String key, String nodeIp, String id, String type, String stat) {
        try {
            String keyMd5 = getMD5(key);
            SecretKey secretKey = getKeyFromString(keyMd5);
            String payload = generateControlPayload(nodeIp, id, type, stat);
            return encrypt(payload, secretKey);
        } catch (Exception e) {
            return "False"; // Handle error appropriately
        }
    }

    // Generate payload block 2 including encrypted payload1, mac, and flag
    public static String payloadBlock2(String key, String payload1, String mac, String flag) {
        try {
            String keyMd5 = getMD5(key);
            SecretKey secretKey = getKeyFromString(keyMd5);
            String u8Timestamp = get8ByteTimestampHexString();
            return encrypt(payload1 +u8Timestamp+mac + get1ByteHex(flag), secretKey);
        } catch (Exception e)
        {
            return "False"; // Handle error appropriately
        }
    }

    // Decode payload block 1, decrypt and parse data
    public static String decode_payloadBlock1(String key, String data) {
        try {
            String keyMd5 = getMD5(key);
            SecretKey secretKey = getKeyFromString(keyMd5);
            String decData = decrypt(data, secretKey);
            return parseData(decData);
        } catch (Exception e) {
            return "False"; // Handle error appropriately
        }
    }

    // Decode payload block 2, decrypt and extract payload, mac, and flag
    public static String[] decode_payloadBlock2(String key, String data) {
        try {
            String decodedData = anonymousDecode(key, data);
            int startIndex = decodedData.length();
            String flag = decodedData.substring(startIndex - 2);
            String mac = decodedData.substring(startIndex - 14, startIndex - 2);
            String timetamp =  decodedData.substring(startIndex - 30,startIndex - 14);
            String payload = decodedData.substring(0, startIndex - 30);

            return new String[]{payload,timetamp, mac, flag};

        } catch (Exception e) {
            return new String[]{"False"}; // Handle error appropriately
        }
    }

    // Encode data anonymously (encrypt with AES)
    public static String anonymousEncode(String key, String data) {
        try {
            String keyMd5 = getMD5(key);
            SecretKey secretKey = getKeyFromString(keyMd5);
            return encrypt(data, secretKey);
        } catch (Exception e) {
            return "False"; // Handle error appropriately
        }
    }

    // Decode anonymously encoded data (decrypt with AES)
    public static String anonymousDecode(String key, String data) {
        try {
            String keyMd5 = getMD5(key);
            SecretKey secretKey = getKeyFromString(keyMd5);
            return decrypt(data, secretKey);
        } catch (Exception e) {
            return "False"; // Handle error appropriately
        }
    }

    // Retrieve the first real MAC address from network interfaces
    public static String getFirstRealMacAddress() throws SocketException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();

            String name = networkInterface.getName().toLowerCase();
            String displayName = networkInterface.getDisplayName().toLowerCase();

            if (!networkInterface.isLoopback() &&
                    !networkInterface.isVirtual() &&
                    !networkInterface.isPointToPoint() &&
                    networkInterface.getHardwareAddress() != null &&
                    !name.startsWith("veth") &&
                    !name.startsWith("docker") &&
                    !name.startsWith("vmnet") &&
                    !name.startsWith("virbr") &&
                    !name.startsWith("br-") &&
                    !displayName.contains("virtual") &&
                    !displayName.contains("vmware") &&
                    !displayName.contains("hyper-v") &&
                    !displayName.contains("docker") &&
                    !displayName.contains("wsl")) {

                byte[] mac = networkInterface.getHardwareAddress();
                if (mac != null && mac.length == 6) {
                    return bytesToHex(mac);
                }
            }
        }

        return null; // Return null if no suitable MAC address found
    }


    // Convert hexadecimal string to SecretKey object (AES)
    private static SecretKey getKeyFromString(String key) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, "AES");
    }
}
