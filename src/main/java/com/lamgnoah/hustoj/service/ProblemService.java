package com.lamgnoah.hustoj.service;



import com.fasterxml.jackson.core.JsonProcessingException;
import com.lamgnoah.hustoj.dto.PageDTO;
import com.lamgnoah.hustoj.dto.ProblemDTO;
import com.lamgnoah.hustoj.exception.AppException;
import com.lamgnoah.hustoj.query.ProblemQuery;



public interface ProblemService {

  ProblemDTO findProblemById(Long id) throws JsonProcessingException;
  PageDTO<ProblemDTO> findProblems(ProblemQuery problemQuery ,Integer page, Integer size)
      throws JsonProcessingException;
  ProblemDTO create(ProblemDTO problemDTO) throws JsonProcessingException ;
  ProblemDTO update(ProblemDTO problemDTO) throws JsonProcessingException;
  ProblemDTO delete(Long id) throws AppException ;
  PageDTO<ProblemDTO> adminFindProblems(ProblemQuery problemQuery, Integer page, Integer size);
  ProblemDTO adminFindProblemById(Long id) throws JsonProcessingException;
}
