package com.lamgnoah.hustoj.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lamgnoah.hustoj.dto.ContestDTO;
import com.lamgnoah.hustoj.dto.PageDTO;
import com.lamgnoah.hustoj.dto.ProblemDTO;
import com.lamgnoah.hustoj.dto.RankingUserDTO;
import com.lamgnoah.hustoj.exception.AppException;
import com.lamgnoah.hustoj.query.ContestProblemQuery;
import com.lamgnoah.hustoj.query.ContestQuery;
import com.lamgnoah.hustoj.query.RankingUserQuery;
import com.lamgnoah.hustoj.service.ContestService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/contests")
@RequiredArgsConstructor
public class ContestRestController {

  private final ContestService contestService;

  @GetMapping(value = "/{id}")
  public ContestDTO getContest(@PathVariable Long id) throws AppException {
    return contestService.findById(id);
  }

  @GetMapping(value = "/{id}/admin")
  public ContestDTO adminGetContestDetail(@PathVariable Long id) throws AppException {
    return contestService.adminFindById(id);
  }

  @GetMapping
  public PageDTO<ContestDTO> getContests(
      ContestQuery contestQuery,
      @RequestParam(value = "page", defaultValue = "0") Integer page,
      @RequestParam(value = "size", defaultValue = "10") Integer size)
      throws AppException {
    return contestService.findCriteria(page, size, contestQuery);
  }

  @GetMapping("/admin")
  public PageDTO<ContestDTO> adminGetContests(
      ContestQuery contestQuery,
      @RequestParam(value = "page", defaultValue = "0") Integer page,
      @RequestParam(value = "size", defaultValue = "10") Integer size)
      throws AppException {
    return contestService.adminGetContests(page, size, contestQuery);
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
  public ContestDTO createContest(
      @Validated @RequestBody ContestDTO contestDTO)
      throws AppException {
    return contestService.create(contestDTO);
  }

  @PutMapping(value = "/{id}")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
  public ContestDTO updateContest(@Validated @RequestBody ContestDTO contestDTO,
      @PathVariable Long id)
      throws AppException {
    contestDTO.setId(id);
    return contestService.update(contestDTO);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
  public ResponseEntity<Void> deleteContest(@PathVariable Long id) throws AppException {
    contestService.delete(id);
    return ResponseEntity.ok().build();
  }

  @PatchMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  public ContestDTO partUpdateContest(@PathVariable Long id, ContestDTO contestDTO)
      throws AppException {
    contestDTO.setId(id);
    return contestService.partUpdate(contestDTO);
  }

  @PostMapping("/{id}/join")
  public ContestDTO joinContest(@PathVariable Long id, String password) throws AppException {
    return contestService.joinContest(id, password);
  }

  @GetMapping("/{id}/problems")
  public PageDTO<ProblemDTO> getProblems(@PathVariable Long id,
      ContestProblemQuery contestProblemQuery,
      @RequestParam(value = "page", defaultValue = "0") Integer page,
      @RequestParam(value = "size", defaultValue = "10") Integer size)
      throws AppException, JsonProcessingException {
    return contestService.findAllProblems(id , page , size , contestProblemQuery);
  }

  @PostMapping("/{id}/problem/create")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  public ProblemDTO createContestProblem(@PathVariable Long id , @RequestBody ProblemDTO problemDTO)
      throws JsonProcessingException {
    return contestService.createContestProblem(id, problemDTO);
  }


  @PostMapping("/{id}/problem/add")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  public ProblemDTO addPublicProblemToContest(@PathVariable Long id , @RequestBody Long problemId) {
    return contestService.addProblem(id, problemId);
  }

  @PutMapping("/{contestId}/problem/{problemId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  public ProblemDTO updateContestProblem(@PathVariable Long contestId , @PathVariable Long problemId,
  @RequestBody ProblemDTO problemDTO) throws JsonProcessingException {
    problemDTO.setContestId(contestId);
    return contestService.updateProblem(contestId, problemId , problemDTO);
  }

  @GetMapping("/{contestId}/problems/{problemId}")
  public ProblemDTO getContestProblemDetail(@PathVariable Long contestId , @PathVariable Long problemId)
      throws JsonProcessingException {
    return contestService.getContestProblemDetail(contestId, problemId);
  }

  @PutMapping("/{contestId}/problem/{problemId}/visible")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  public ProblemDTO updateContestProblemVisible(@PathVariable Long contestId , @PathVariable Long problemId,
      @RequestBody Boolean visible){
    return contestService.updateContestProblemVisible(contestId, problemId , visible);
  }

  @DeleteMapping("/{contestId}/problems/{problemId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  public ResponseEntity<Void> deleteContestProblem(@PathVariable Long contestId , @PathVariable Long problemId){
    contestService.deleteContestProblem(contestId, problemId);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/{id}/users")
  public PageDTO<RankingUserDTO> getUsers(@PathVariable Long id,
      @RequestParam(value = "page", defaultValue = "0") Integer page,
      @RequestParam(value = "size", defaultValue = "10") Integer size,
      RankingUserQuery rankingUserQuery) throws AppException {
    return contestService.findAllUsers(page, size,rankingUserQuery,id);
  }

  @DeleteMapping("/{id}/users")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  public void deleteUsers(@RequestBody List<Long> userIdList, @PathVariable Long id)
      throws AppException {
    contestService.deleteUsers(userIdList, id);
  }
  @PostMapping("/{id}/users")
  @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
  public List<RankingUserDTO> addUsers(
      @RequestBody List<Long> userIdList, @PathVariable Long id) throws AppException {
    return contestService.addUsers(userIdList, id);
  }


}

