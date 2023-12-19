package com.lamgnoah.hustoj.controller;



import com.fasterxml.jackson.core.JsonProcessingException;
import com.lamgnoah.hustoj.dto.PageDTO;
import com.lamgnoah.hustoj.dto.ProblemDTO;
import com.lamgnoah.hustoj.exception.AppException;
import com.lamgnoah.hustoj.query.ProblemQuery;
import com.lamgnoah.hustoj.service.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/problems")
@RequiredArgsConstructor
public class ProblemRestController {

  private final ProblemService problemService;

  @GetMapping
  public PageDTO<ProblemDTO> getProblems(
      ProblemQuery problemQuery ,
      @RequestParam(value = "page", defaultValue = "0") Integer page,
      @RequestParam(value = "size", defaultValue = "10") Integer size)
      throws JsonProcessingException {
    return problemService.findProblems(problemQuery ,page , size);
  }
  @GetMapping("/admin")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
  public PageDTO<ProblemDTO> adminGetProblems(
      ProblemQuery problemQuery ,
      @RequestParam(value = "page", defaultValue = "0") Integer page,
      @RequestParam(value = "size", defaultValue = "10") Integer size){
    return problemService.adminFindProblems(problemQuery ,page , size);
  }

  @GetMapping(value = "/{id}")
  public ResponseEntity<ProblemDTO> getProblem(@PathVariable Long id)
      throws JsonProcessingException {
    ProblemDTO problemDTO = problemService.findProblemById(id);
    return ResponseEntity.ok(problemDTO);
  }

  @GetMapping(value = "/{id}/admin")
  public ResponseEntity<ProblemDTO> adminGetProblem(@PathVariable Long id)
      throws JsonProcessingException {
    ProblemDTO problemDTO = problemService.adminFindProblemById(id);
    return ResponseEntity.ok(problemDTO);
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
  public ResponseEntity<ProblemDTO> createProblem(@Validated @RequestBody ProblemDTO problemDTO) throws
      AppException, JsonProcessingException
  {
    ProblemDTO response = problemService.create(problemDTO);
    return ResponseEntity.ok(response);
  }


  @PutMapping(value = "/{id}")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
  public ProblemDTO updateProblem(@Validated @RequestBody ProblemDTO problemDTO, @PathVariable Long id)
      throws AppException, JsonProcessingException {
      problemDTO.setId(id);
      return problemService.update(problemDTO);
    }


  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
  public ProblemDTO deleteProblem(@PathVariable Long id) throws AppException {
    return problemService.delete(id);
  }

}
