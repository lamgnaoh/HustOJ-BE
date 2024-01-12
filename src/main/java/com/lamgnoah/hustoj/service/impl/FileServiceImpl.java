package com.lamgnoah.hustoj.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lamgnoah.hustoj.domain.UserContext;
import com.lamgnoah.hustoj.domain.enums.ErrorCode;
import com.lamgnoah.hustoj.dto.FileUploadingDto;
import com.lamgnoah.hustoj.entity.Problem;
import com.lamgnoah.hustoj.entity.User;
import com.lamgnoah.hustoj.exception.AppException;
import com.lamgnoah.hustoj.repository.ProblemRepository;
import com.lamgnoah.hustoj.service.FileService;
import com.lamgnoah.hustoj.utils.CommonUtil;
import com.lamgnoah.hustoj.utils.FileUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

  private final ProblemRepository problemRepository;
  private final ObjectMapper objectMapper;
  @Value("${upload.path}")
  private String directory;

  private static List<String> getZipEntryNames(File zipFile) throws IOException {
    List<String> nameList = new ArrayList<>();
    try (ZipFile zip = new ZipFile(zipFile)) {
      zip.stream().map(ZipEntry::getName).forEach(nameList::add);
    }
    return nameList;
  }

  @Override
  public FileUploadingDto uploadTestCase(MultipartFile file, HttpServletRequest request)
      throws AppException, IOException {
    String fileId = UUID.randomUUID().toString();
    String originalFilename = file.getOriginalFilename();
    String extension = FilenameUtils.getExtension(originalFilename);
    if (file.isEmpty()) {
      throw new AppException(ErrorCode.EMPTY_FILE);
    }
    if (!Objects.equals(extension, "zip")) {
      throw new AppException(ErrorCode.NOT_A_ZIP_FILE);
    }

    String tempDirectory =
        request.getSession().getServletContext().getRealPath(File.separator + "upload")
            + File.separator
            + fileId
            + File.separator;
    String tempPath = tempDirectory + originalFilename;
    File tempFile = new File(tempDirectory);
    if (!tempFile.exists()) {
      tempFile.mkdirs();
    }
    try (OutputStream os = new FileOutputStream(tempPath)) {
      os.write(file.getBytes());
    } catch (IOException e) {
      log.error("Cannot upload test case : ", e);
    }
    List<String> nameList = getZipEntryNames(new File(tempPath));
    List<String> testCaseList = filterNameList(nameList, "");
    if (testCaseList.isEmpty()) {
      throw new AppException(ErrorCode.EMPTY_TEST_CASE);
    }

//    process zip
    String testCaseId = UUID.randomUUID().toString();
    String prefix =
        File.separator + "problems" + File.separator + testCaseId + File.separator;
    String relativeTestCasePath = saveFile(tempPath, prefix);
    List<Map<String, Object>> info = processTestCase(relativeTestCasePath, false, testCaseList);
    return new FileUploadingDto(testCaseId, info, false);
  }

  private List<Map<String, Object>> processTestCase(String path, Boolean specialJudge,
      List<String> testCaseList) throws JsonProcessingException {
    String testcasePath = directory + path;
    String destDirectoryPath = testcasePath.substring(0, testcasePath.lastIndexOf(File.separator));

    Map<String, Object> sizeCache = new HashMap<>();
    Map<String, String> md5Cache = new HashMap<>();
    Map<String, Object> testCaseInfo = new HashMap<>();

    List<File> fileList = CommonUtil.unzip(testcasePath, testCaseList);

    File[] files = new File[fileList.size()];
    files = fileList.toArray(files); // file in-out
    for (File file : files) {
      String content = "";
      try (BufferedInputStream inputFile = new BufferedInputStream(new FileInputStream(file))) {
        int len = inputFile.available(); // return the sum of bytes remained to be read from this input stream
        byte[] middleArray = new byte[len];
        inputFile.read(middleArray, 0, len);
        byte[] inputFiletoBytes = new byte[len];
        int index = 0;
        for (int i = 0; i < len; i++) {
          if (middleArray[i] == 13) {
            if (i + 1 < len
                && middleArray[i + 1] == 10) { // move the cursor point to the begining of next line
              inputFiletoBytes[index++] = 10;
              i++;
            } else {
              inputFiletoBytes[index++] = middleArray[i];
            }
          } else {
            inputFiletoBytes[index++] = middleArray[i];
          }
        }
        for (int i = index - 1; i >= 0; i--) {
          if (inputFiletoBytes[i] == 10) {
            index--;
          } else {
            break;
          }
        }
        byte[] finalArray = new byte[index];
        System.arraycopy(inputFiletoBytes, 0, finalArray, 0, index);
        sizeCache.put(file.getName(), finalArray.length);
        content = new String(finalArray);
        if (file.getName().endsWith(".out")) {
          md5Cache.put(file.getName(), CommonUtil.md5(finalArray));
        }
      } catch (IOException | NoSuchAlgorithmException e) {
        log.error("Error while process test case upload :", e);
        throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
      }

      try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
        writer.write(content);
      } catch (IOException e) {
        log.error("Error while process test case upload :", e);
        throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
      }
    }

    testCaseInfo.put("spj", specialJudge);
    Map<String, Object> testCases = new HashMap<>();
    List<Map<String, Object>> info = new ArrayList<>();
    int j = 0;
    for (int i = 0; i < files.length; i += 2) {
      Map<String, Object> data = new HashMap<>();
      data.put("stripped_output_md5", md5Cache.get(files[i + 1].getName()));
      data.put("input_size", sizeCache.get(files[i].getName()));
      data.put("output_size", sizeCache.get(files[i + 1].getName()));
      data.put("input_name", files[i].getName());
      data.put("output_name", files[i + 1].getName());
      info.add(data);
      testCases.put("" + (++j), data);
    }
    testCaseInfo.put("test_cases", testCases);

    String jsonStr = objectMapper.writeValueAsString(testCaseInfo);
    File infoJson = new File(destDirectoryPath + File.separator + "info");
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(infoJson))) {
      writer.write(jsonStr);
    } catch (IOException e) {
      throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
//    return info here
    return info;
  }

  private List<String> filterNameList(List<String> nameList, String dir) {
    List<String> ret = new ArrayList<>();
    int prefix = 1;
    while (true) {
      String inName = prefix + ".in";
      String outName = prefix + ".out";
      if (nameList.contains(dir + inName) && nameList.contains(dir + outName)) {
        ret.add(inName);
        ret.add(outName);
        prefix++;
      } else {
        return ret.stream().sorted(Comparator.naturalOrder())
            .collect(Collectors.toList());
      }
    }
  }


  @Override
  public String saveFile(String tempPath, String relativeDirectory) throws IOException {
    File tempFile = new File(tempPath);
    String originalFileName = tempFile.getName();
    String absoluteDirectory = directory + relativeDirectory;
    File fileDirectory = new File(absoluteDirectory);
    if (!fileDirectory.exists()) {
      fileDirectory.mkdirs();
    }
    String absolutePath = absoluteDirectory + originalFileName;
    File targetFile = new File(absolutePath);
    Files.copy(tempFile.toPath(), targetFile.toPath());
    tempFile.delete();
    // return relative path
    return relativeDirectory + originalFileName;
  }

  @Override
  public StreamingResponseBody downloadTestCase(Long problemId, HttpServletResponse response) {
    User user = UserContext.getCurrentUser();
    Problem problem = problemRepository.findById(problemId)
        .orElseThrow(() -> new AppException(ErrorCode.PROBLEM_NOTFOUND));
    if (!CommonUtil.ensureCreatedBy(problem, user)) {
      throw new AppException(ErrorCode.OBJECT_NOT_CREATED_BY_USER);
    }
    String testcaseDir = directory + File.separator + "problems" + File.separator +
        problem.getTestCaseId() + File.separator;
    if (!Files.isDirectory(Paths.get(testcaseDir))) {
      throw new AppException(ErrorCode.TEST_CASE_NOT_FOUND);
    }
    response.setContentType("application/octet-stream");
    response.setHeader("Content-Disposition",
        "attachment;filename=problem_" + problemId + "_test_cases.zip");
    response.setStatus(HttpServletResponse.SC_OK);
//    list all file from test case dir
    List<String> files = FileUtil.listAllFilesFromGivenPath(testcaseDir);
    return out -> {
      /* Create a ZIP output stream from response output stream */
      try (final ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream())) {
        /* Gather all files and put it in a ZIP */
        for (String file : files) {
          FileUtil.addGivenFileToZip(zipOut, file, testcaseDir);
        }
      }
    };
  }
}
