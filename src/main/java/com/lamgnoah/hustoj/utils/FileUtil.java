package com.lamgnoah.hustoj.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtil {

  public static List<String> listAllFilesFromGivenPath(String path) {
    List<String> files = new ArrayList<>();

    File folder = new File(path);
    File[] listOfFiles = folder.listFiles();

    for (File file : listOfFiles) {
      if (file.isFile()) {
        files.add(file.getName());
      }
    }

    return files;
  }

  public static void addGivenFileToZip(ZipOutputStream zipOut, String file , String absolutePath) throws IOException {
    /* Get file input stream */
    try (final InputStream inputStream = new FileInputStream(absolutePath + file)) {
      /* Create new ZIP entry */
      final ZipEntry zipEntry = new ZipEntry(file);
      zipOut.putNextEntry(zipEntry);

      /* Stream data to ZIP output stream */
      byte[] bytes = new byte[1024];
      int length;
      while ((length = inputStream.read(bytes)) >= 0) {
        zipOut.write(bytes, 0, length);
      }
    }
  }
}

