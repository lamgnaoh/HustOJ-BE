package com.lamgnoah.hustoj.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lamgnoah.hustoj.dto.ContestDTO;
import com.lamgnoah.hustoj.dto.PageDTO;
import com.lamgnoah.hustoj.dto.ProblemDTO;
import com.lamgnoah.hustoj.dto.RankingUserDTO;
import com.lamgnoah.hustoj.query.ContestProblemQuery;
import com.lamgnoah.hustoj.query.ContestQuery;
import com.lamgnoah.hustoj.query.RankingUserQuery;
import java.util.List;

public interface ContestService {

  ContestDTO create(ContestDTO contestDTO);

  ContestDTO update(ContestDTO contestDTO);

  void delete(Long id);

  PageDTO<ProblemDTO> findAllProblems(Long id , Integer page , Integer pageSize , ContestProblemQuery contestProblemQuery)
      throws JsonProcessingException;

  PageDTO<ContestDTO> findCriteria(Integer page, Integer size, ContestQuery contestQuery);

  ContestDTO partUpdate(ContestDTO contestDTO);

  ContestDTO findById(Long id);

  ProblemDTO createContestProblem(Long id, ProblemDTO problemDTO) throws JsonProcessingException;

  ProblemDTO addProblem(Long id, Long problemId);

  ProblemDTO updateProblem(Long contestId, Long problemId , ProblemDTO problemDTO)
      throws JsonProcessingException;

  ProblemDTO getContestProblemDetail(Long contestId, Long problemId) throws JsonProcessingException;

  ProblemDTO updateContestProblemVisible(Long contestId, Long problemId, Boolean visible);

  void  deleteContestProblem(Long contestId, Long problemId);

  ContestDTO joinContest(Long contestId, String password);

  PageDTO<RankingUserDTO> findAllUsers(Integer page , Integer pageSize , RankingUserQuery query ,Long id);

  void deleteUsers(List<Long> userIdList, Long id);

  List<RankingUserDTO> addUsers(List<Long> userIdList, Long id);

  PageDTO<ContestDTO> adminGetContests(Integer page, Integer size, ContestQuery contestQuery);

  ContestDTO adminFindById(Long id);
}
