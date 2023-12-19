package com.lamgnoah.hustoj.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lamgnoah.hustoj.dto.PageDTO;
import com.lamgnoah.hustoj.dto.SubmissionDTO;
import com.lamgnoah.hustoj.query.SubmissionQuery;
import java.util.List;

public interface SubmissionService {

  SubmissionDTO createPracticeSubmission(SubmissionDTO submissionDTO)
      throws JsonProcessingException;

  SubmissionDTO findById(Long id);

  PageDTO<SubmissionDTO> findAll(Integer page, Integer size, SubmissionQuery submissionQuery);


  List<SubmissionDTO> findByPracticeProblem(Long id);

  void counter(SubmissionDTO submissionDTO) throws JsonProcessingException;

  SubmissionDTO createContestSubmission(SubmissionDTO submissionDTO) throws JsonProcessingException;
}
