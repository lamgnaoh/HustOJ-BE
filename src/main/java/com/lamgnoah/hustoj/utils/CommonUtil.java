package com.lamgnoah.hustoj.utils;

import com.lamgnoah.hustoj.domain.enums.AuthorityName;
import com.lamgnoah.hustoj.domain.enums.ErrorCode;
import com.lamgnoah.hustoj.entity.Authority;
import com.lamgnoah.hustoj.entity.Contest;
import com.lamgnoah.hustoj.entity.ContestProblem;
import com.lamgnoah.hustoj.entity.Problem;
import com.lamgnoah.hustoj.entity.User;
import com.lamgnoah.hustoj.exception.AppException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class CommonUtil {
  public static boolean isNull(String str) {
    return str == null || str.isEmpty();
  }

  public static List<File> unzip(String path , List<String> testCaseList) throws AppException {
    String destDirectoryPath = path.substring(0, path.lastIndexOf(File.separator));
    File destDirectory = new File(destDirectoryPath);
    List<File> fileList = new ArrayList<>();
    byte[] buffer = new byte[1024];

    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(path))) {
      ZipEntry zipEntry = zis.getNextEntry();
      if (zipEntry == null) {
        throw new AppException(ErrorCode.EMPTY_ZIP_FILE);
      }
      while (zipEntry != null) {
        if (!testCaseList.contains(zipEntry.getName())) {
          zipEntry = zis.getNextEntry();
          continue;
        }
        fileList.add(new File(destDirectoryPath + File.separator + zipEntry.getName()));
        File unzippedFile = newFile(destDirectory, zipEntry);
        try (FileOutputStream fos = new FileOutputStream(unzippedFile)) {
          int len;
          while ((len = zis.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
          }
        }
        zipEntry = zis.getNextEntry();
      }
    } catch (IOException e) {
      e.printStackTrace();
      throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
    File file = new File(path);
    file.delete();
    return fileList;
  }

  private static File newFile(File destDirectory, ZipEntry zipEntry) throws IOException {
    File destFile = new File(destDirectory, zipEntry.getName());

    String destDirectoryPath = destDirectory.getCanonicalPath();
    String destFilePath = destFile.getCanonicalPath();

    if (!destFilePath.startsWith(destDirectoryPath + File.separator)) {
      throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
    }
    return destFile;
  }

  public static String md5(byte[] byteArray) throws NoSuchAlgorithmException {
    MessageDigest md5 = MessageDigest.getInstance("MD5");
    byte[] encryption = md5.digest(byteArray);
    StringBuffer strBuf = new StringBuffer();
    for (int i = 0; i < encryption.length; i++) {
      if (Integer.toHexString(0xff & encryption[i]).length() == 1) {
        strBuf.append("0").append(Integer.toHexString(0xff & encryption[i]));
      } else {
        strBuf.append(Integer.toHexString(0xff & encryption[i]));
      }
    }
    return strBuf.toString();
  }

  public static boolean isAdmin(User user) {
    for (Authority authority : user.getAuthorities()) {
      if (AuthorityName.ROLE_SUPER_ADMIN.equals(authority.getName()) || AuthorityName.ROLE_ADMIN.equals(authority.getName())) {
        return true;
      }
    }
    return false;
  }
  public static boolean isSuperAdmin(User user) {
    return user.getAuthorities().stream()
        .anyMatch(authority -> authority.getName().equals(AuthorityName.ROLE_SUPER_ADMIN));
  }

  public static <T> boolean ensureCreatedBy(T object , User user) {
    if (object instanceof Problem) {
      return ((Problem) object).getAuthor().equals(user) ||
          user.canManageAllProblem() ||
          isSuperAdmin(user);
    }
    if (object instanceof Contest) {
      return ((Contest) object).getAuthor().equals(user) || isSuperAdmin(user);
    }
    if (object instanceof ContestProblem) {
      return ((ContestProblem) object).getContest().getAuthor().equals(user)|| isSuperAdmin(user);
    }
    return false;
  }

}
