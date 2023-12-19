package com.lamgnoah.hustoj.service.impl;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lamgnoah.hustoj.domain.UserContext;
import com.lamgnoah.hustoj.domain.enums.ContestRuleType;
import com.lamgnoah.hustoj.domain.enums.Difficulty;
import com.lamgnoah.hustoj.domain.enums.ErrorCode;
import com.lamgnoah.hustoj.dto.TestcaseInfoDTO;
import com.lamgnoah.hustoj.dto.PageDTO;
import com.lamgnoah.hustoj.dto.ProblemDTO;
import com.lamgnoah.hustoj.domain.AcmProblemStatus;
import com.lamgnoah.hustoj.domain.OiProblemStatus;
import com.lamgnoah.hustoj.entity.Contest;
import com.lamgnoah.hustoj.entity.ContestProblem;
import com.lamgnoah.hustoj.entity.Problem;
import com.lamgnoah.hustoj.entity.SampleIO;
import com.lamgnoah.hustoj.entity.Tag;
import com.lamgnoah.hustoj.entity.User;
import com.lamgnoah.hustoj.exception.AppException;
import com.lamgnoah.hustoj.mapper.ProblemMapper;
import com.lamgnoah.hustoj.mapper.TagMapper;
import com.lamgnoah.hustoj.query.ProblemQuery;
import com.lamgnoah.hustoj.repository.ContestProblemRepository;
import com.lamgnoah.hustoj.repository.ContestRepository;
import com.lamgnoah.hustoj.repository.ProblemRepository;
import com.lamgnoah.hustoj.repository.SubmissionRepository;
import com.lamgnoah.hustoj.repository.TagRepository;
import com.lamgnoah.hustoj.service.ProblemService;
import com.lamgnoah.hustoj.utils.CommonUtil;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProblemServiceImpl implements ProblemService {

  private final SubmissionRepository submissionRepository;
  private final ProblemMapper problemMapper;
  private final ProblemRepository problemRepository;
  private final TagRepository tagRepository;
  private final TagMapper tagMapper;
  private final ObjectMapper objectMapper;
  private final ContestProblemRepository contestProblemRepository;
  private final ContestRepository contestRepository;

  @PostConstruct
  private void init() {
    objectMapper.setSerializationInclusion(Include.NON_NULL);
  }

  @Override
  public ProblemDTO findProblemById(Long id) throws JsonProcessingException {
    User user = UserContext.getCurrentUser();
    Problem problem = problemRepository.findById(id)
        .orElseThrow(() -> new AppException(ErrorCode.PROBLEM_NOTFOUND));
    List<SampleIO> sampleIOList = objectMapper.readValue(problem.getSampleIO(),
        new TypeReference<List<SampleIO>>() {
        });
//    test data score
    List<TestcaseInfoDTO> testcaseInfoList = objectMapper.readValue(problem.getTestCaseScore(),
        new TypeReference<List<TestcaseInfoDTO>>() {
        });
    ProblemDTO dto = problemMapper.entityToDTO(problem);
    dto.setSampleIOList(sampleIOList);
    dto.setTestcaseInfos(testcaseInfoList);
    if (user != null) {
      addProblemStatus(dto, user);
    }
    return dto;
  }

  private void addProblemStatus(ProblemDTO dto, User user) throws JsonProcessingException {
    String acmProblemsStatusJson = user.getAcmProblemsStatus();
    String oiProblemsStatusJson = user.getOiProblemsStatus();
    if (dto.getRuleType().equals(ContestRuleType.ACM.name())) {
      AcmProblemStatus acmProblemStatus = objectMapper.readValue(acmProblemsStatusJson,
          AcmProblemStatus.class);
      if (acmProblemStatus.getProblems() == null
          || acmProblemStatus.getProblems().get(dto.getId()) == null) {
        dto.setMyStatus(null);
        return;
      }
      dto.setMyStatus(acmProblemStatus.getProblems().get(dto.getId()).getStatus());
    } else {
      OiProblemStatus oiProblemStatus = objectMapper.readValue(oiProblemsStatusJson,
          OiProblemStatus.class);
      if (oiProblemStatus.getProblems() == null
          || oiProblemStatus.getProblems().get(dto.getId()) == null) {
        dto.setMyStatus(null);
        return;
      }
      dto.setMyStatus(oiProblemStatus.getProblems().get(dto.getId()).getStatus());
    }
  }

  @Override
  public PageDTO<ProblemDTO> findProblems(ProblemQuery problemQuery, Integer page, Integer size)
      throws JsonProcessingException {
    User user = UserContext.getCurrentUser();
    Pageable pageable = PageRequest.of(page, size, Sort.Direction.ASC, "id");
    Specification<Problem> ps = (root, criteriaQuery, criteriaBuilder) -> {
      List<Predicate> predicateList = new ArrayList<>();
      String problemCode = problemQuery.getProblemCode();
//          find by problem code
      if (!CommonUtil.isNull(problemCode)) {
        predicateList.add(criteriaBuilder.like(root.get("problemCode").as(String.class),
            "%" + problemCode + "%"));
      }

      String title = problemQuery.getTitle();
      if (!CommonUtil.isNull(title)) {
        predicateList.add(
            criteriaBuilder.like(root.get("title").as(String.class), "%" + title + "%"));
      }

      String keyword = problemQuery.getKeyword();
      if (!CommonUtil.isNull(keyword)) {
        predicateList.add(criteriaBuilder.or(
            criteriaBuilder.like(root.get("title").as(String.class), "%" + keyword + "%"),
            criteriaBuilder.like(root.get("problemCode").as(String.class), "%" + keyword + "%")));
      }

      Difficulty difficulty = problemQuery.getDifficulty();
      if (null != difficulty) {
        predicateList.add(criteriaBuilder.equal(root.get("difficulty"), difficulty));
      }

      String tags = problemQuery.getTags(); // get tag id
      List<Tag> tagList = new ArrayList<>();
      if (null != tags && !tags.isEmpty()) {
        List<Long> tagIdList = Arrays.stream(tags.split(",")).map(Long::valueOf)
            .collect(Collectors.toList());
        tagList = tagRepository.findAllById(tagIdList);
      }

      if (!tagList.isEmpty()) {
        for (Tag tag : tagList) {
          predicateList.add(criteriaBuilder.isMember(tag, root.get("tagList")));
        }
      }

      if (user == null // guest not register to system
          || !CommonUtil.isAdmin(user)) {
        predicateList.add(criteriaBuilder.equal(root.get("visible"), true));
      }

//          ContestRuleType contestRuleType =problemQuery.getContestRuleType();
//          if (null != contestRuleType){
//            predicateList.add(criteriaBuilder.equal(root.get("ruleType"), contestRuleType));
//          }
      predicateList.add(
          criteriaBuilder.or(criteriaBuilder.equal(root.get("createInContest"), false)));
      Predicate[] p = new Predicate[predicateList.size()];
      return criteriaBuilder.and(predicateList.toArray(p));
    };
    List<Problem> problemList = problemRepository.findAll(ps, pageable).getContent();
    long count = problemRepository.count(ps);
    List<ProblemDTO> problemDTOs = problemMapper.toProblemDTOs(problemList);
    if (user != null) {
      for (ProblemDTO dto : problemDTOs) {
        addProblemStatus(dto, user);
      }
    }
    return new PageDTO<>(page, size, count, problemDTOs);
  }

  @Override
  public ProblemDTO create(ProblemDTO problemDTO) throws JsonProcessingException {
    User user = UserContext.getCurrentUser();

    if (problemDTO.getContestId() == null) {
      Optional<Problem> problemByCode = problemRepository.findByProblemCode(
          problemDTO.getProblemCode());
      if (problemByCode.isPresent()) {
        throw new AppException(ErrorCode.PROBLEM_EXISTS_CODE_ALREADY);
      }
      Optional<Problem> problemByTitle = problemRepository.findByTitle(problemDTO.getTitle());
      if (problemByTitle.isPresent()) {
        throw new AppException(ErrorCode.PROBLEM_EXISTS_TITLE_ALREADY);
      }
    } else {
      Contest contest = contestRepository.findById(problemDTO.getContestId())
          .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_CONTEST));
      Optional<ContestProblem> problemByCode = contestProblemRepository.findByContestAndProblemProblemCode(
          contest, problemDTO.getProblemCode());
      if (problemByCode.isPresent()) {
        throw new AppException(ErrorCode.PROBLEM_EXISTS_CODE_ALREADY);
      }
      Optional<ContestProblem> problemByTitle = contestProblemRepository.findByContestAndProblemTitle(
          contest, problemDTO.getTitle());
      if (problemByTitle.isPresent()) {
        throw new AppException(ErrorCode.PROBLEM_EXISTS_CODE_ALREADY);
      }
    }


    Problem problem = problemMapper.dtoToEntity(problemDTO);
    problem.setAuthor(user);
    problem.setTagList(new HashSet<>());
    Set<Tag> tagSet = new HashSet<>(
        processTagSet(problem, tagMapper.toTags(problemDTO.getTagList())));
    problem.setTagList(tagSet);
    if (!validateSampleIO(problemDTO.getSampleIOList())) {
      throw new AppException(ErrorCode.SAMPLE_IO_INVALID);
    }
    problem.setSampleIO(objectMapper.writeValueAsString(problemDTO.getSampleIOList()));
    problem.setTestCaseScore(objectMapper.writeValueAsString(problemDTO.getTestcaseInfos()));
    int totalScore = 0;
    if (Objects.equals(problemDTO.getRuleType(), ContestRuleType.OI.name())) {
      for (TestcaseInfoDTO info : problemDTO.getTestcaseInfos()) {
        totalScore += info.getScore();
      }
    }
    problem.setTotalScore(totalScore);
    problem = problemRepository.save(problem);
    return problemMapper.entityToDTO(problem);
  }

  private Collection<Tag> processTagSet(Problem problem, Set<Tag> tagSet) {
    Set<Tag> existTags = problem.getTagList();
    Set<Tag> removedTags = new HashSet<>(existTags);
    for (Tag t : tagSet) {
      if (!existTags.contains(t)) {
        Optional<Tag> tagOptional = tagRepository.findByName(t.getName());
        if (tagOptional.isPresent()) {
          Tag tag = tagOptional.get();
          tag.setProblemCount(tag.getProblemCount() + 1);
          tag.getProblemList().add(problem);
          existTags.add(tag);
        } else {
          Tag newTag = new Tag();
          newTag.setName(t.getName());
          newTag.setProblemCount(1L);
          newTag.getProblemList().add(problem);
          existTags.add(newTag);
        }
      } else {
        removedTags.remove(t);
      }
    }
    for (Tag removedTag : removedTags) {
      Optional<Tag> removeTagOptional = tagRepository.findByName(removedTag.getName());
      if (removeTagOptional.isPresent()) {
        Tag removeTag = removeTagOptional.get();
        removeTag.setProblemCount(removeTag.getProblemCount() - 1);
        removeTag.getProblemList().remove(problem);
      }
    }
    existTags.removeAll(removedTags);
//      get remove tag
//    each removetag , remove to existTags and problemCount decrease 1 and remove problem in problemList
    return tagRepository.saveAll(existTags);
  }

  private boolean validateSampleIO(List<SampleIO> sampleIOList) {
    for (SampleIO sampleIO : sampleIOList) {
      if (CommonUtil.isNull(sampleIO.getInput()) || CommonUtil.isNull(sampleIO.getOutput())) {
        return false;
      }
    }
    return true;
  }

  @Override
  public ProblemDTO update(ProblemDTO problemDTO) throws JsonProcessingException {
    User user = UserContext.getCurrentUser();
    Long id = problemDTO.getId();
    Problem problem = problemRepository.findById(id)
        .orElseThrow(() -> new AppException(ErrorCode.PROBLEM_NOTFOUND));
    if (!CommonUtil.ensureCreatedBy(problem, user)) {
      throw new AppException(ErrorCode.OBJECT_NOT_CREATED_BY_USER);
    }

    if (null != problemDTO.getProblemCode() && !problem.getProblemCode()
        .equals(problemDTO.getProblemCode())) {
      if (problemDTO.getContestId() == null) {
        Optional<Problem> p = problemRepository.findByProblemCode(problemDTO.getProblemCode());
        if (p.isPresent() && !p.get().getId().equals(id)) {
          throw new AppException(ErrorCode.PROBLEM_EXISTS_CODE_ALREADY);
        }
      } else {
        Contest contest = contestRepository.findById(problemDTO.getContestId())
            .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_CONTEST));
        Optional<ContestProblem> p = contestProblemRepository.findByContestAndProblemProblemCode(
            contest, problemDTO.getProblemCode());
        if (p.isPresent() && !p.get().getProblem().getId().equals(id)) {
          throw new AppException(ErrorCode.PROBLEM_EXISTS_CODE_ALREADY);
        }
      }
      problem.setProblemCode(problemDTO.getProblemCode());
    }

    if (null != problemDTO.getVisible()) {
      problem.setVisible(problemDTO.getVisible());
    }

    if (!validateSampleIO(problemDTO.getSampleIOList())) {
      throw new AppException(ErrorCode.SAMPLE_IO_INVALID);
    }
    problem.setSampleIO(objectMapper.writeValueAsString(problemDTO.getSampleIOList()));

    if (null != problemDTO.getTitle() && !problem.getTitle().equals(problemDTO.getTitle())) {
      if (problemDTO.getContestId() == null) {
        Optional<Problem> p = problemRepository.findByTitle(problemDTO.getTitle());
        if (p.isPresent() && !p.get().getId().equals(id)) {
          throw new AppException(ErrorCode.PROBLEM_EXISTS_TITLE_ALREADY);
        }
      } else {
        Contest contest = contestRepository.findById(problemDTO.getContestId())
            .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_CONTEST));
        Optional<ContestProblem> p = contestProblemRepository.findByContestAndProblemTitle(contest,
            problemDTO.getTitle());
        if (p.isPresent() && !p.get().getProblem().getId().equals(id)) {
          throw new AppException(ErrorCode.PROBLEM_EXISTS_TITLE_ALREADY);
        }
      }
      problem.setTitle(problemDTO.getTitle());
    }
    if (null != problemDTO.getDifficulty()) {
      problem.setDifficulty(Difficulty.valueOf(problemDTO.getDifficulty()));
    }

    if (null != problemDTO.getDescription()) {
      problem.setDescription(problemDTO.getDescription());
    }

    if (null != problemDTO.getHint()) {
      problem.setHint(problemDTO.getHint());
    }

    if (null != problemDTO.getInputDescription()) {
      problem.setInputDescription(problemDTO.getInputDescription());
    }

    if (null != problemDTO.getOutputDescription()) {
      problem.setOutputDescription(problemDTO.getOutputDescription());
    }

    if (null != problemDTO.getRamLimit()) {
      problem.setRamLimit(problemDTO.getRamLimit());
    }

    if (null != problemDTO.getTimeLimit()) {
      problem.setTimeLimit(problemDTO.getTimeLimit());
    }

    if (null != problemDTO.getTestCaseId()) {
      problem.setTestCaseId(problemDTO.getTestCaseId());
    }

    if (null != problemDTO.getRuleType()) {
      problem.setRuleType(ContestRuleType.valueOf(problemDTO.getRuleType()));
    }

    if (null != problemDTO.getTestcaseInfos()) {
      problem.setTestCaseScore(objectMapper.writeValueAsString(problemDTO.getTestcaseInfos()));
      int totalScore = 0;
      if (Objects.equals(problemDTO.getRuleType(), ContestRuleType.OI.name())) {
        for (TestcaseInfoDTO info : problemDTO.getTestcaseInfos()) {
          totalScore += info.getScore();
        }
      }
      problem.setTotalScore(totalScore);
    }

    if (null != problemDTO.getSpecialJudged()) {
      problem.setSpecialJudged(problemDTO.getSpecialJudged());
    }

    if (null != problemDTO.getTagList()) {
      Set<Tag> tagSet = new HashSet<>(
          processTagSet(problem, tagMapper.toTags(problemDTO.getTagList())));
      problem.setTagList(tagSet);
    }
    return problemMapper.entityToDTO(problemRepository.save(problem));

  }

  @Transactional
  public ProblemDTO delete(Long id) throws AppException {
    User user = UserContext.getCurrentUser();
    Problem problem = problemRepository.findById(id)
        .orElseThrow(() -> new AppException(ErrorCode.PROBLEM_NOTFOUND));
    if (!CommonUtil.ensureCreatedBy(problem, user)) {
      throw new AppException(ErrorCode.OBJECT_NOT_CREATED_BY_USER);
    }
    if (!contestProblemRepository.findByProblem(problem).isEmpty()) {
      throw new AppException(ErrorCode.PROBLEM_REFERENCED);
    }
    Set<Tag> tagSet = problem.getTagList();
    for (Tag tag : tagSet) {
      tag.setProblemCount(tag.getProblemCount() - 1);
    }
    tagRepository.saveAll(tagSet);
    submissionRepository.deleteByProblemId(id);
    problemRepository.delete(problem);
    return problemMapper.entityToDTO(problem);
  }

  @Override
  public PageDTO<ProblemDTO> adminFindProblems(ProblemQuery problemQuery, Integer page,
      Integer size) {
    User user = UserContext.getCurrentUser();

    Pageable pageable = PageRequest.of(page, size, Sort.Direction.ASC, "id");
    Specification<Problem> ps = (root, criteriaQuery, criteriaBuilder) -> {
      List<Predicate> predicateList = new ArrayList<>();
      String keyword = problemQuery.getKeyword();
      if (!CommonUtil.isNull(keyword)) {
        predicateList.add(criteriaBuilder.or(
            criteriaBuilder.like(root.get("title").as(String.class), "%" + keyword + "%"),
            criteriaBuilder.like(root.get("problemCode").as(String.class), "%" + keyword + "%")));
      }

      Boolean visible = problemQuery.getVisible();
      if (visible != null && visible && CommonUtil.isAdmin(
          user)) { // turn on visible in admin screen
        predicateList.add(criteriaBuilder.equal(root.get("visible"), true));
      }

      ContestRuleType contestRuleType = problemQuery.getContestRuleType();
      if (null != contestRuleType) {
        predicateList.add(criteriaBuilder.equal(root.get("ruleType"), contestRuleType));
      }
//          filter by author id if user is not super admin and not manage all problem
      if (user != null && user.isAdmin() && !user.canManageAllProblem() && !CommonUtil.isSuperAdmin(
          user)) {
        predicateList.add(criteriaBuilder.equal(root.get("author").get("id"), user.getId()));
      }

      predicateList.add(
          criteriaBuilder.or(criteriaBuilder.equal(root.get("createInContest"), false)));

      Predicate[] p = new Predicate[predicateList.size()];
      return criteriaBuilder.and(predicateList.toArray(p));
    };
    List<Problem> problemList = problemRepository.findAll(ps, pageable).getContent();
    long count = problemRepository.count(ps);

    return new PageDTO<>(page, size, count, problemMapper.toProblemDTOs(problemList));
  }

  @Override
  public ProblemDTO adminFindProblemById(Long id) throws JsonProcessingException {
    User user = UserContext.getCurrentUser();
    Problem problem = problemRepository.findById(id)
        .orElseThrow(() -> new AppException(ErrorCode.PROBLEM_NOTFOUND));
    if (!CommonUtil.ensureCreatedBy(problem, user)) {
      throw new AppException(ErrorCode.OBJECT_NOT_CREATED_BY_USER);
    }
    List<SampleIO> sampleIOList = objectMapper.readValue(problem.getSampleIO(),
        new TypeReference<>() {
        });
//    test data score
    List<TestcaseInfoDTO> TestcaseInfoList = objectMapper.readValue(problem.getTestCaseScore(),
        new TypeReference<>() {
        });
    ProblemDTO dto = problemMapper.entityToDTO(problem);
    dto.setSampleIOList(sampleIOList);
    dto.setTestcaseInfos(TestcaseInfoList);
    return dto;
  }
}
