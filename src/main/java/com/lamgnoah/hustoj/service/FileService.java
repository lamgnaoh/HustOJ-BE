package com.lamgnoah.hustoj.service;


import com.lamgnoah.hustoj.dto.FileUploadingDto;
import com.lamgnoah.hustoj.exception.AppException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

public interface FileService {


 FileUploadingDto uploadTestCase(MultipartFile file, HttpServletRequest request)
     throws AppException, IOException;

 String saveFile(String tempPath, String relativeDirectory) throws IOException;

 StreamingResponseBody downloadTestCase(Long problemId, HttpServletResponse response);
}
