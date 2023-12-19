package com.lamgnoah.hustoj.controller;

import com.lamgnoah.hustoj.exception.AppException;
import com.lamgnoah.hustoj.service.FileService;
import com.lamgnoah.hustoj.utils.NetResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
public class FileRestController {

  private final FileService fileService;

  @Autowired
  public FileRestController(FileService fileService) {
    this.fileService = fileService;
  }

  @RequestMapping(value = "/api/v1/upload/testcase")
  public NetResult upload(@RequestBody MultipartFile file, HttpServletRequest request)
      throws AppException, IOException {
    NetResult netResult = new NetResult();
    netResult.data = fileService.uploadTestCase(file, request);
    netResult.code = 200;
    netResult.message = "";
    return netResult;
  }

  @GetMapping("/api/v1/download/testcase")
  public ResponseEntity<StreamingResponseBody> download( @RequestParam Long problemId, HttpServletResponse response)
      throws AppException {
    return new ResponseEntity<>(fileService.downloadTestCase(problemId, response), HttpStatus.OK);
  }

}
