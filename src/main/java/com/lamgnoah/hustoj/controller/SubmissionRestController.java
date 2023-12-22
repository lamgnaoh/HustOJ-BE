package com.lamgnoah.hustoj.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lamgnoah.hustoj.dto.PageDTO;
import com.lamgnoah.hustoj.dto.SubmissionDTO;
import com.lamgnoah.hustoj.exception.AppException;
import com.lamgnoah.hustoj.query.SubmissionQuery;
import com.lamgnoah.hustoj.service.SubmissionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1")
@RequiredArgsConstructor
public class SubmissionRestController {

  private final SubmissionService submissionService;

  @PostMapping(value = "/problems/{id}/submissions")
  public SubmissionDTO createPracticeSubmissions(
      @PathVariable Long id,
      @Validated @RequestBody SubmissionDTO submissionDTO)
      throws AppException, JsonProcessingException {
    submissionDTO.setProblemId(id);
    return submissionService.createPracticeSubmission(submissionDTO);
  }

  @PostMapping(value = "/contests/{contestId}/problems/{problemId}/submissions")
  public SubmissionDTO createContestSubmissions(
      @PathVariable Long contestId,
      @PathVariable Long problemId,
      @Validated @RequestBody SubmissionDTO submissionDTO)
      throws AppException, JsonProcessingException {
    submissionDTO.setProblemId(problemId);
    submissionDTO.setContestId(contestId);
    return submissionService.createContestSubmission(submissionDTO);
  }



  @GetMapping(value = "/submissions")
  public PageDTO<SubmissionDTO> getSubmissions(
      @RequestParam(defaultValue = "0") Integer page,
      @RequestParam(defaultValue = "10") Integer size,
      SubmissionQuery submissionQuery)
      throws AppException {
    return submissionService.findAll(page, size, submissionQuery);
  }

  @GetMapping(value = "/submissions/{id}")
  public SubmissionDTO getSubmission(@PathVariable Long id) throws AppException {
    return submissionService.findById(id);
  }

  @GetMapping(value = "/problems/{id}/submissions")
  public List<SubmissionDTO> getPracticeSubmissions(@PathVariable Long id) throws AppException {
    return submissionService.findByPracticeProblem(id);
  }
  @GetMapping(value = "/contests/{contestId}/problems/{problemId}/submissions")
  public List<SubmissionDTO> getContestSubmissions(
      @PathVariable Long contestId, @PathVariable Long problemId) throws AppException {
    return submissionService.findByContestProblem(contestId, problemId);
  }

  @GetMapping(value = "/contests/{id}/submissions")
  public PageDTO<SubmissionDTO> getSubmissions(
      @PathVariable Long id,
      @RequestParam(defaultValue = "0") Integer page,
      @RequestParam(defaultValue = "10") Integer size)
      throws AppException {
    return submissionService.findAllSubmissionByContest(id, page, size);
  }


}
