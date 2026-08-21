package com.benchmark;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class DataLoader {
    private static final String DATASET_URL = "https://snap.stanford.edu/data/wiki-Vote.txt.gz";
    private static final String LOCAL_FILE_GZ = "wiki-Vote.txt.gz";
    private static final String LOCAL_FILE_TXT = "wiki-Vote.txt";

    public static List<String[]> loadDataset() throws IOException {
        ensureDatasetDownloaded();
        return parseDataset();
    }

    private static void ensureDatasetDownloaded() throws IOException {
        File txtFile = new File(LOCAL_FILE_TXT);
        if (txtFile.exists()) {
            System.out.println("Dataset already found locally at: " + txtFile.getAbsolutePath());
            return;
        }

        System.out.println("Downloading dataset from: " + DATASET_URL);
        try (InputStream in = new URL(DATASET_URL).openStream()) {
            Files.copy(in, Paths.get(LOCAL_FILE_GZ), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        System.out.println("Download complete. Decompressing Gzip file...");

        try (GZIPInputStream gis = new GZIPInputStream(new FileInputStream(LOCAL_FILE_GZ));
             FileOutputStream fos = new FileOutputStream(LOCAL_FILE_TXT)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
        }

        new File(LOCAL_FILE_GZ).delete();
        System.out.println("Decompression complete. Saved to: " + txtFile.getAbsolutePath());
    }

    private static List<String[]> parseDataset() throws IOException {
        System.out.println("Parsing dataset...");
        List<String[]> edges = new ArrayList<>();
        int lineCount = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(LOCAL_FILE_TXT))) {
            String line;
            while ((line = br.readLine()) != null) {
                lineCount++;
                if (line.startsWith("#")) {
                    continue;
                }
                String[] parts = line.trim().split("\\s+");
                if (parts.length >= 2) {
                    edges.add(new String[]{parts[0], parts[1]});
                }
            }
        }

        System.out.println("Parsing finished. Total file lines: " + lineCount + ", total edges loaded: " + edges.size());
        return edges;
    }
}
