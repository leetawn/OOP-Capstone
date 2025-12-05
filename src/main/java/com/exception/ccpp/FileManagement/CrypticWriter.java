package com.exception.ccpp.FileManagement;

import javax.crypto.Cipher;
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
    private static final byte[] FILE_HEADER = {0, 'C', 'C', 'P', 'P', 0, 'A', 'B', 'B', 'D', 'L', 12, (byte)0x07, (byte)0xE9};
    private static final int IV_SIZE = 16;
    private static final int MIN_FILE_SIZE = FILE_HEADER.length + IV_SIZE + 1; // Header + IV + minimum 1 byte of data

    // --- Key Derivation Logic ---
    private static final String APP_UNIQUE_SECRET = "MyApp-Data-Storage-Identifier-v1.0::StableSalt123XYZ";

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

        // 1. Setup Cipher & Serialize Data
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, iv);

        byte[] serializedData;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(data);
            oos.flush();
            serializedData = bos.toByteArray();
        }

        // 2. Encrypt
        byte[] encryptedData = cipher.doFinal(serializedData);

        // 3. Write Header, IV, and Encrypted Data to File
        try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
            fos.write(FILE_HEADER); // Write the file identifier header first
            fos.write(iv.getIV());
            fos.write(encryptedData);
            System.out.println("Data written, encrypted, and file header included.");
        }
    }


    /******************* DECRYPT AND READ *******************/
    @SuppressWarnings("unchecked")
    public static List<Object> readEncryptedData(Path path) throws Exception {

        // Check 1: File existence and minimum size
        if (!Files.exists(path) || Files.size(path) < MIN_FILE_SIZE) {
            System.out.println("File not found or too small.");
            return new ArrayList<>();
        }

        SecretKey key = getDerivedSecretKey();

        // Read the full file
        byte[] fileBytes;
        try (FileInputStream fis = new FileInputStream(path.toFile())) {
            fileBytes = fis.readAllBytes();
        }

        // Check 2: Header Validation
        byte[] retrievedHeader = new byte[FILE_HEADER.length];
        System.arraycopy(fileBytes, 0, retrievedHeader, 0, FILE_HEADER.length);

        if (!Arrays.equals(FILE_HEADER, retrievedHeader)) {
            System.out.println("File header mismatch");
            return new ArrayList<>();
        }

        // --- If Header is Valid, Proceed with Decryption ---

        // Calculate start index for IV after the header
        int ivStartIndex = FILE_HEADER.length;
        int encryptedDataStartIndex = ivStartIndex + IV_SIZE;

        // Extract IV (next 16 bytes)
        byte[] ivBytes = new byte[IV_SIZE];
        System.arraycopy(fileBytes, ivStartIndex, ivBytes, 0, IV_SIZE);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);

        // Extract Encrypted Data
        int encryptedDataLength = fileBytes.length - encryptedDataStartIndex;
        byte[] encryptedData = new byte[encryptedDataLength];
        System.arraycopy(fileBytes, encryptedDataStartIndex, encryptedData, 0, encryptedDataLength);

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
        String FILE_PATH = "app_data_with_header.dat";
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
            Path invalidPath = Path.of("fake_file.dat");
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