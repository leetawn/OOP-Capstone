package com.exception.ccpp.FileManagement;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CrypticWriter {

    // --- Header Definition ---
    // A 12-byte "magic number" to identify the file format.
    // This is the first thing written to the file.
    private static final byte[] FILE_HEADER = {0, 'C', 'C', 'P', 'P', 3, 'A', 'B', 'B', 'D', 'L', 12, (byte)0x07, (byte)0xE9};
    private static final int IV_SIZE = 16;
    private static final int MIN_FILE_SIZE = FILE_HEADER.length + IV_SIZE + 1; // Header + IV + minimum 1 byte of data

    // --- Key Derivation Logic ---
    private static final String APP_UNIQUE_SECRET = "pleasetaluninnatinangfeutechpleaselang-G0dzPro1sG@Y";
    private static final String MAC_ALGORITHM = "HmacSHA256"; // 🚨 New constant for MAC algorithm
    private static final int MAC_TAG_SIZE = 32;

    public static SecretKey getDerivedSecretKey() throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] key = sha.digest(APP_UNIQUE_SECRET.getBytes("UTF-8"));
        key = Arrays.copyOf(key, 16); // Truncate to AES-128 key size
        return new SecretKeySpec(key, "AES");
    }

    public static IvParameterSpec generateIV() {
        byte[] iv = new byte[IV_SIZE];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    /******************* ENCRYPT AND WRITE *******************/
    public static void writeEncryptedData(List<Object> data, Path path) throws Exception {

        SecretKey key = getDerivedSecretKey();
        IvParameterSpec iv = generateIV();

        // Setup Cipher & Serialize Data
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);

        byte[] serializedData;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(data);
            oos.flush();
            serializedData = bos.toByteArray();
        }

        byte[] encryptedData = cipher.doFinal(serializedData);

        Mac mac = Mac.getInstance(MAC_ALGORITHM);
        mac.init(new SecretKeySpec(key.getEncoded(), MAC_ALGORITHM));

        byte[] macTag = mac.doFinal(encryptedData);

        try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
            fos.write(FILE_HEADER);
            fos.write(iv.getIV());
            fos.write(encryptedData);
            fos.write(macTag);
            System.out.println("Data written, encrypted, header, and HMAC Tag included.");
        }
    }


    /******************* DECRYPT AND READ *******************/
    @SuppressWarnings("unchecked")
    public static List<Object> readEncryptedData(Path path) throws Exception {

        final int MIN_READABLE_FILE_SIZE = FILE_HEADER.length + IV_SIZE + MAC_TAG_SIZE + 1;
        if (!Files.exists(path) || Files.size(path) < MIN_READABLE_FILE_SIZE) {
            System.out.println("File not found or too small.");
            return new ArrayList<>();
        }

        SecretKey key = getDerivedSecretKey();

        byte[] fileBytes;
        try (FileInputStream fis = new FileInputStream(path.toFile())) {
            fileBytes = fis.readAllBytes();
        }

        byte[] retrievedHeader = new byte[FILE_HEADER.length];
        System.arraycopy(fileBytes, 0, retrievedHeader, 0, FILE_HEADER.length);

        if (!Arrays.equals(FILE_HEADER, retrievedHeader)) {
            System.out.println("File header mismatch");
            return new ArrayList<>();
        }

        int ivStartIndex = FILE_HEADER.length;
        int encryptedDataStartIndex = ivStartIndex + IV_SIZE;

        int macTagStartIndex = fileBytes.length - MAC_TAG_SIZE;

        // Extract IV
        byte[] ivBytes = new byte[IV_SIZE];
        System.arraycopy(fileBytes, ivStartIndex, ivBytes, 0, IV_SIZE);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);

        int encryptedDataLength = macTagStartIndex - encryptedDataStartIndex;
        byte[] encryptedData = new byte[encryptedDataLength];
        System.arraycopy(fileBytes, encryptedDataStartIndex, encryptedData, 0, encryptedDataLength);

        byte[] storedMacTag = new byte[MAC_TAG_SIZE];
        System.arraycopy(fileBytes, macTagStartIndex, storedMacTag, 0, MAC_TAG_SIZE);


        // INTEGRITY CHECK
        Mac mac = Mac.getInstance(MAC_ALGORITHM);
        mac.init(new SecretKeySpec(key.getEncoded(), MAC_ALGORITHM));

        byte[] calculatedMacTag = mac.doFinal(encryptedData);

        if (!Arrays.equals(storedMacTag, calculatedMacTag)) {
            System.out.println("File has been corrupted or tampered with!");
            return new ArrayList<>();
        }
        System.out.println("DATA IS THE GOAT");

        // Setup Cipher for Decryption
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key, iv);

        // Decrypt and Deserialize
        byte[] decryptedData = cipher.doFinal(encryptedData);
        try (ByteArrayInputStream bis = new ByteArrayInputStream(decryptedData);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return (List<Object>) ois.readObject();
        }
    }

    // DEMO
    public static void main(String[] args) {
        String FILE_PATH = "app_data_with_header.ccpp";
        Path path = Path.of(FILE_PATH);

        // Prepare the data!
        List<Object> mixedDataToStore = new ArrayList<>();
        String[] configArray = {"Key:Theme"};
        mixedDataToStore.add(configArray);

        try {
            // write
            CrypticWriter.writeEncryptedData(mixedDataToStore, path);

            // read
            List<Object> retrievedData = CrypticWriter.readEncryptedData(path);
            System.out.println("\nSuccessful Read Size: " + retrievedData.size());
            System.out.println("Data: " + Arrays.toString((String[]) retrievedData.get(0)));

            System.out.println("\nTest for Invalid File");
            Path invalidPath = Path.of("fake_file.ccpp");
            try (FileOutputStream fos = new FileOutputStream(invalidPath.toFile())) {
                fos.write("JUNK DATA".getBytes());
            }

            // Read the invalid file - Should fail the header check and return empty list
            List<Object> invalidData = CrypticWriter.readEncryptedData(invalidPath);
            System.out.println("Invalid File Read Size: " + invalidData.size()); // Should be 0

        } catch (Exception e) {
            System.err.println("An error occurred during file operation:");
            e.printStackTrace();
        }
    }
}