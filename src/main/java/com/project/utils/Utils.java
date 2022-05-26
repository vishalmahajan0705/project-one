package com.project.utils;


import java.io.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Utils {


    public static String hash(String str)  {

        MessageDigest digest =null;
        try {
            digest = MessageDigest.getInstance("SHA3-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        final byte[] hash = digest.digest(
                str.getBytes(StandardCharsets.UTF_8));


        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();

    }

    public synchronized void download(String globalBlockChainPath, String localBlockChainPath) throws Exception{
        Path path = FileSystems.getDefault().getPath(globalBlockChainPath);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(FileSystems.getDefault().getPath(localBlockChainPath))));
        try (InputStream in = Files.newInputStream(path);
             BufferedReader reader =
                     new BufferedReader(new InputStreamReader(in))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                bw.write(line);
                bw.newLine();
            }
            bw.flush();
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public static String mineBlock(String prefix, String value) {
        int nonce = 0;

        String hash = hash(value+nonce);
        System.out.println(nonce+" " +hash);
        while (!hash.substring(0, prefix.length()).equals(prefix)) {
            nonce++;
            hash = hash(value+nonce);
            //System.out.println(nonce+" " +hash);
        }
        return hash;
    }








}
