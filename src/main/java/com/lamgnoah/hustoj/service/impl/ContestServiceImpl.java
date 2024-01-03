package com.lamgnoah.hustoj.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lamgnoah.hustoj.domain.AcmProblemStatus;
import com.lamgnoah.hustoj.domain.OiProblemStatus;
import com.lamgnoah.hustoj.domain.UserContext;
import com.lamgnoah.hustoj.domain.enums.ContestRuleType;
import com.lamgnoah.hustoj.domain.enums.ContestStatus;
import com.lamgnoah.hustoj.domain.enums.ContestType;
import com.lamgnoah.hustoj.domain.enums.ErrorCode;
import com.lamgnoah.hustoj.dto.AddPublicProblemDTO;
import com.lamgnoah.hustoj.dto.ContestDTO;
import com.lamgnoah.hustoj.dto.PageDTO;
import com.lamgnoah.hustoj.dto.ProblemDTO;
import com.lamgnoah.hustoj.dto.RankingDTO;
import com.lamgnoah.hustoj.dto.RankingUserDTO;
import com.lamgnoah.hustoj.entity.Contest;
import com.lamgnoah.hustoj.entity.ContestProblem;
import com.lamgnoah.hustoj.entity.Problem;
import com.lamgnoah.hustoj.entity.RankingUser;
import com.lamgnoah.hustoj.entity.Submission;
import com.lamgnoah.hustoj.entity.User;
import com.lamgnoah.hustoj.exception.AppException;
import com.lamgnoah.hustoj.factory.RankingUserFactory;
import com.lamgnoah.hustoj.mapper.ContestMapper;
import com.lamgnoah.hustoj.mapper.ContestProblemMapper;
import com.lamgnoah.hustoj.mapper.RankingUserMapper;
import com.lamgnoah.hustoj.query.ContestProblemQuery;
import com.lamgnoah.hustoj.query.ContestQuery;
import com.lamgnoah.hustoj.query.RankingUserQuery;
import com.lamgnoah.hustoj.repository.ContestProblemRepository;
import com.lamgnoah.hustoj.repository.ContestRepository;
import com.lamgnoah.hustoj.repository.ProblemRepository;
import com.lamgnoah.hustoj.repository.RankingUserRepository;
import com.lamgnoah.hustoj.repository.SubmissionRepository;
import com.lamgnoah.hustoj.repository.UserRepository;
import com.lamgnoah.hustoj.service.ContestService;
import com.lamgnoah.hustoj.service.ProblemService;
import com.lamgnoah.hustoj.utils.CommonUtil;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContestServiceImpl implements ContestService {

  private final ContestRepository contestRepository;
  private final ContestMapper contestMapper;
  private final SubmissionRepository submissionRepository;
  private final ContestProblemRepository contestProblemRepository;
  private final ContestProblemMapper contestProblemMapper;
  private final ProblemRepository problemRepository;
  private final ProblemService problemService;
  private final RankingUserRepository rankingUserRepository;
  private final RankingUserMapper rankingUserMapper;
  private final UserRepository userRepository;
  private final ObjectMapper objectMapper;
  private final RedisTemplate<String,String> redisTemplate;

  @Override
  public ContestDTO create(ContestDTO contestDTO) {
    User user = UserContext.getCurrentUser();

    Optional<Contest> contestExist = contestRepository.findByName(contestDTO.getName());
    if (contestExist.isPresent()) {
      throw new AppException(ErrorCode.HAVE_SAME_NAME_CONTEST);
    }

    Contest contest = contestMapper.dtoToEntity(contestDTO);

    if (contest.getStartDate().isEqual(LocalDateTime.now()) || contest.getStartDate()
        .isBefore(LocalDateTime.now())) {
      throw new AppException(ErrorCode.START_TIME_IS_EARLY_THAN_NOW);
    }

    if (contest.getStartDate().isEqual(contest.getEndDate()) || contest.getStartDate()
        .isAfter(contest.getEndDate())) {
      throw new AppException(ErrorCode.START_TIME_IS_AFTER_THAN_END_TIME);
    }

    requirePassword(contest);

//    contest.setCreateDate(LocalDateTime.now());
    contest.setAuthor(user);
    return contestMapper.entityToDTO(contestRepository.save(contest));
  }

  @Override
  public ContestDTO update(ContestDTO contestDTO) {
    User user = UserContext.getCurrentUser();
    Contest contest = contestRepository.findById(contestDTO.getId())
        .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_CONTEST));
    if (!CommonUtil.ensureCreatedBy(contest, user)) {
      throw new AppException(ErrorCode.OBJECT_NOT_CREATED_BY_USER);
    }

    if (ContestStatus.ENDED.equals(contest.getStatus())) {
      throw new AppException(ErrorCode.CONTEST_IS_ENDED);
    }
// if contest is not started
    if (!ContestStatus.PROCESSING.equals(contest.getStatus())) { // contest is not started
      if (null != contestDTO.getName()) {
        Optional<Contest> contestOptional = contestRepository.findByName(contestDTO.getName());
        if (contestOptional.isPresent() && !contestOptional.get().getId()
            .equals(contestDTO.getId())) {
          throw new AppException(ErrorCode.HAVE_SAME_NAME_CONTEST);
        }
        contest.setName(contestDTO.getName());
      }
//      Todo
//      if (null != contestDTO.getCouldShare()) {
//        contest.setCouldShare(contestDTO.getCouldShare());
//      }

      if (null != contestDTO.getContestType()) {
        contest.setContestType(ContestType.valueOf(contestDTO.getContestType()));
      }

      if (null != contestDTO.getDescription()) {
        contest.setDescription(contestDTO.getDescription());
      }

      if (null != contestDTO.getStartDate()) {
        contest.setStartDate(contestDTO.getStartDate());
      }

      if (null != contestDTO.getPassword()) {
        contest.setPassword(contestDTO.getPassword());
      }
      if (null != contestDTO.getEndDate()) {
        contest.setEndDate(contestDTO.getEndDate());
      }
      if (null != contestDTO.getVisible()) {
        contest.setVisible(contestDTO.getVisible());
      }
    }

    return contestMapper.entityToDTO(contestRepository.save(contest));
  }

  @Override
  @Transactional
  public void delete(Long id) {
    User user = UserContext.getCurrentUser();
    Contest contest = contestRepository.findById(id)
        .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_CONTEST));
    if (!CommonUtil.ensureCreatedBy(contest, user)) {
      throw new AppException(ErrorCode.OBJECT_NOT_CREATED_BY_USER);
    }
    Set<RankingUser> rankingUserList = rankingUserRepository.findByContest(contest);
    submissionRepository.deleteAllByContest(contest);
    rankingUserRepository.deleteAll(rankingUserList);
    contestProblemRepository.deleteAllByContest(contest);
    contestRepository.delete(contest);
  }

  @Override
  public PageDTO<ProblemDTO> findAllProblems(Long id, Integer page, Integer pageSize,
      ContestProblemQuery contestProblemQuery) throws JsonProcessingException {
    User user = UserContext.getCurrentUser();
    Pageable pageable = PageRequest.of(page, pageSize);

    Contest contest = contestRepository.findById(id)
        .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_CONTEST));

    if (!user.isAdmin() && ContestStatus.NOT_STARTED.equals(contest.getStatus())) {
      throw new AppException(ErrorCode.CONTEST_NOT_GOING);
    }
    List<ContestProblem> contestProblemList = contestProblemRepository.findByContestAndParam(
        contest, contestProblemQuery, pageable);
    List<ProblemDTO> problemDTOs = contestProblemMapper.toContestProblemDTOs(contestProblemList);
    for (ProblemDTO dto : problemDTOs) {
      addContestProblemStatus(dto, user);
    }

    return new PageDTO<>(page, pageSize, (long) problemDTOs.size(), problemDTOs);
  }

  private void addContestProblemStatus(ProblemDTO dto, User user) throws JsonProcessingException {
    String acmProblemsStatusJson = user.getAcmProblemsStatus();
    String oiProblemsStatusJson = user.getOiProblemsStatus();
    if (dto.getRuleType().equals(ContestRuleType.ACM.name())) {
      AcmProblemStatus acmProblemStatus = objectMapper.readValue(acmProblemsStatusJson,
          AcmProblemStatus.class);
      if (acmProblemStatus.getContestProblem() == null
          || acmProblemStatus.getContestProblem().get(dto.getId()) == null) {
        dto.setMyStatus(null);
        return;
      }
      dto.setMyStatus(acmProblemStatus.getContestProblem().get(dto.getId()).getStatus());
    } else {
      OiProblemStatus oiProblemStatus = objectMapper.readValue(oiProblemsStatusJson,
          OiProblemStatus.class);
      if (oiProblemStatus.getContestProblem() == null
          || oiProblemStatus.getContestProblem().get(dto.getId()) == null) {
        dto.setMyStatus(null);
        return;
      }
      dto.setMyStatus(oiProblemStatus.getContestProblem().get(dto.getId()).getStatus());
    }
  }


  @Override
  public PageDTO<ContestDTO> findCriteria(Integer page, Integer size, ContestQuery contestQuery) {
    User user = UserContext.getCurrentUser();
    Pageable pageable = PageRequest.of(page, size, Direction.DESC, "startDate");
    Specification<Contest> cs = (root, criteriaQuery, criteriaBuilder) -> {
      List<Predicate> predicateList = new ArrayList<>();

      String name = contestQuery.getName();
      if (null != name && !name.isEmpty()) {
        predicateList.add(
            criteriaBuilder.like(root.get("name").as(String.class), "%" + name + "%"));
      }

      ContestStatus status = contestQuery.getStatus();
      if (null != status) {
        predicateList.add(criteriaBuilder.equal(root.get("status"), status));
      }

      ContestType type = contestQuery.getType();
      if (null != type) {
        predicateList.add(criteriaBuilder.equal(root.get("contestType"), type));
      }

      ContestRuleType ruleType = contestQuery.getRuleType();
      if (null != ruleType) {
        predicateList.add(criteriaBuilder.equal(root.get("contestRuleType"), ruleType));
      }

      if (!CommonUtil.isAdmin(user)) {
        predicateList.add(criteriaBuilder.equal(root.get("visible").as(Boolean.class), true));
      }

      Predicate[] p = new Predicate[predicateList.size()];
      return criteriaBuilder.and(predicateList.toArray(p));
    };
    List<Contest> contestList = contestRepository.findAll(cs, pageable).getContent();
    for (Contest contest : contestList) {
      if ((contest.getStartDate().isBefore(LocalDateTime.now()) || contest.getStartDate()
          .isEqual(LocalDateTime.now())) && contest.getStatus().equals(ContestStatus.NOT_STARTED)) {
        setContestStatus(contest, ContestStatus.PROCESSING);
      }

      if ((contest.getEndDate().isBefore(LocalDateTime.now())) || (contest.getEndDate()
          .isEqual(LocalDateTime.now()))) {
        setContestStatus(contest, ContestStatus.ENDED);
      }
    }
    contestList = contestRepository.saveAll(contestList);
    Long count = contestRepository.count(cs);
    List<ContestDTO> contestDTOList = contestMapper.toContestDTOs(contestList);
    return new PageDTO<>(page, size, count, contestDTOList);
  }

  @Override
  public ContestDTO partUpdate(ContestDTO contestDTO) {
    User user = UserContext.getCurrentUser();
    Contest contest = contestRepository.findById(contestDTO.getId())
        .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_CONTEST));
    if (!CommonUtil.ensureCreatedBy(contest, user)) {
      throw new AppException(ErrorCode.OBJECT_NOT_CREATED_BY_USER);
    }

    Boolean visible = contestDTO.getVisible();
    if (null != visible) {
      contest.setVisible(visible);
    }
    return contestMapper.entityToDTO(contestRepository.save(contest));
  }

  @Override
  public ContestDTO findById(Long id) {
    User user = UserContext.getCurrentUser();
    Contest contest = contestRepository.findById(id)
        .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_CONTEST));
    Optional<RankingUser> rankingUserOptional = rankingUserRepository.findByContestAndUser(contest,
        user);
//    if user is not super admin or not author of the contest and never join the contest
    if (!CommonUtil.isSuperAdmin(user) && !CommonUtil.ensureCreatedBy(contest, user)
        && rankingUserOptional.isEmpty()) {
      if (ContestType.SECRET_WITH_PASSWORD.equals(contest.getContestType())) {
        throw new AppException(ErrorCode.NOT_PASS_CONTEST_USER);
      }
    }
    return contestMapper.entityToDTO(contest);
  }

  @Override
  public ProblemDTO createContestProblem(Long contestId, ProblemDTO problemDTO)
      throws JsonProcessingException {
    User user = UserContext.getCurrentUser();
    Contest contest = contestRepository.findById(contestId)
        .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_CONTEST));
    if (!CommonUtil.ensureCreatedBy(contest, user)) {
      throw new AppException(ErrorCode.OBJECT_NOT_CREATED_BY_USER);
    }
    if (contest.getStatus().equals(ContestStatus.ENDED)) {
      throw new AppException(ErrorCode.CONTEST_IS_ENDED);
    }
    String problemCode = problemDTO.getProblemCode();
    Optional<Problem> existsProblemByProblemCode = contestProblemRepository.findProblemByProblemCode(
        contestId, problemCode);
    if (existsProblemByProblemCode.isPresent()) {
      throw new AppException(ErrorCode.CONTEST_PROBLEM_EXISTS_CODE_ALREADY);
    }

    String problemTitle = problemDTO.getTitle();
    Optional<Problem> existsProblemByProblemtitle = contestProblemRepository.findProblemByProblemTitle(
        contestId, problemTitle);

    if (existsProblemByProblemtitle.isPresent()) {
      throw new AppException(ErrorCode.CONTEST_PROBLEM_EXISTS_TITLE_ALREADY);
    }
    problemDTO.setContestId(contestId);
    ProblemDTO dto = problemService.create(problemDTO);
    Problem problem = problemRepository.findById(dto.getId())
        .orElseThrow(() -> new AppException(ErrorCode.PROBLEM_NOTFOUND));
    problem.setCreateInContest(true);
    problemRepository.save(problem);
    ContestProblem contestProblem = new ContestProblem();
    contestProblem.setContest(contest);
    contestProblem.setProblem(problem);
    contestProblem.setVisible(true);
    contestProblemRepository.save(contestProblem);
    return dto;
  }

  @Override
  public ProblemDTO addProblem(Long contestId, AddPublicProblemDTO dto) {
    Long problemId = dto.getProblemId();
    String problemCode = dto.getProblemCode();
    User user = UserContext.getCurrentUser();
    Contest contest = contestRepository.findById(contestId)
        .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_CONTEST));
    if (!CommonUtil.ensureCreatedBy(contest, user)) {
      throw new AppException(ErrorCode.OBJECT_NOT_CREATED_BY_USER);
    }
    List<ContestProblem> contestProblemList = contestProblemRepository.findByContest(contest);
    List<Problem> problemList = new ArrayList<>();
    for (ContestProblem contestProblem : contestProblemList) {
      problemList.add(contestProblem.getProblem());
    }
    Problem problem = problemRepository.findById(problemId)
        .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_PROBLEM));
    if (!problem.getRuleType().equals(contest.getContestRuleType())) {
      throw new AppException(ErrorCode.WRONG_PROBLEM_RULE_TYPE);
    }
//    check if problem not already exist in the contest
    if (problemList.contains(problem)) {
      throw new AppException(ErrorCode.PROBLEM_ALREADY_IN_CONTEST);
    }
    Problem newProblem = new Problem(problem);
    newProblem.setProblemCode(problemCode);
    newProblem.setCreateInContest(true);
    problemRepository.save(newProblem);
    ContestProblem contestProblem = new ContestProblem();
    contestProblem.setProblem(newProblem);
    contestProblem.setContest(contest);
    contestProblem.setVisible(true);
    contestProblemRepository.save(contestProblem);
    return contestProblemMapper.entityToDTO(contestProblem);
  }


  @Override
  public ProblemDTO updateProblem(Long contestId, Long problemId, ProblemDTO problemDTO)
      throws JsonProcessingException {
    User user = UserContext.getCurrentUser();
    Contest contest = contestRepository.findById(contestId)
        .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_CONTEST));
    if (!CommonUtil.ensureCreatedBy(contest, user)) {
      throw new AppException(ErrorCode.OBJECT_NOT_CREATED_BY_USER);
    }

    Problem problem = problemRepository.findById(problemId)
        .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_PROBLEM));

    Optional<ContestProblem> contestProblemOptional = contestProblemRepository.findByContestAndProblem(
        contest, problem);
    if (contestProblemOptional.isEmpty()) {
      throw new AppException(ErrorCode.NO_SUCH_PROBLEM_IN_CONTEST);
    }
    ContestProblem contestProblem = contestProblemOptional.get();
    if (!CommonUtil.ensureCreatedBy(contestProblem, user)) {
      throw new AppException(ErrorCode.OBJECT_NOT_CREATED_BY_USER);
    }

    if (!contest.getStatus()
        .equals(ContestStatus.ENDED)) { // only update problem when contest not ended
      if (problemDTO.getVisible() != null) { // update visible in problem contest
        contestProblem.setVisible(problemDTO.getVisible());
      }
      problemService.update(problemDTO);
      contestProblemRepository.save(contestProblem);
    } else {
      throw new AppException(ErrorCode.CANNOT_UPDATE_PROBLEM_WHEN_CONTEST_ENDED);
    }
    return contestProblemMapper.entityToDTO(contestProblem);
  }

  @Override
  public ProblemDTO getContestProblemDetail(Long contestId, Long problemId)
      throws JsonProcessingException {
    User user = UserContext.getCurrentUser();
    Contest contest = contestRepository.findById(contestId)
        .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_CONTEST));
    if (!CommonUtil.isAdmin(user) && ContestStatus.NOT_STARTED.equals(contest.getStatus())) {
      throw new AppException(ErrorCode.CONTEST_NOT_GOING);
    }
    Problem problem = problemRepository.findById(problemId)
        .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_PROBLEM));
    Optional<ContestProblem> contestProblemOptional = contestProblemRepository.findByContestAndProblem(
        contest, problem);
    if (contestProblemOptional.isEmpty()) {
      throw new AppException(ErrorCode.NO_SUCH_PROBLEM_IN_CONTEST);
    }

    ContestProblem contestProblem = contestProblemOptional.get();
    ProblemDTO dto = contestProblemMapper.entityToDTO(contestProblem);
    dto.setTestcaseInfos(objectMapper.readValue(problem.getTestCaseScore(), new TypeReference<>() {}));
    addContestProblemStatus(dto, user);
    return dto;
  }

  @Override
  public ProblemDTO updateContestProblemVisible(Long contestId, Long problemId, Boolean visible) {
    User user = UserContext.getCurrentUser();
    Contest contest = contestRepository.findById(contestId)
        .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_CONTEST));
    if (!CommonUtil.ensureCreatedBy(contest, user)) {
      throw new AppException(ErrorCode.OBJECT_NOT_CREATED_BY_USER);
    }
    Problem problem = problemRepository.findById(problemId)
        .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_PROBLEM));
    Optional<ContestProblem> contestProblemOptional = contestProblemRepository.findByContestAndProblem(
        contest, problem);

    if (contestProblemOptional.isEmpty()) {
      throw new AppException(ErrorCode.NO_SUCH_PROBLEM_IN_CONTEST);
    }
    ContestProblem contestProblem = contestProblemOptional.get();
    if (!CommonUtil.ensureCreatedBy(contestProblem, user)) {
      throw new AppException(ErrorCode.OBJECT_NOT_CREATED_BY_USER);
    }
    contestProblem.setVisible(visible);
    contestProblemRepository.save(contestProblem);
    return contestProblemMapper.entityToDTO(contestProblem);
  }

  @Override
  public void deleteContestProblem(Long contestId, Long problemId) {
    User user = UserContext.getCurrentUser();
    Contest contest = contestRepository.findById(contestId)
        .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_CONTEST));
    if (!CommonUtil.ensureCreatedBy(contest, user)) {
      throw new AppException(ErrorCode.OBJECT_NOT_CREATED_BY_USER);
    }
    Problem problem = problemRepository.findById(problemId)
        .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_PROBLEM));
    Optional<ContestProblem> contestProblemOptional = contestProblemRepository.findByContestAndProblem(
        contest, problem);
    if (contestProblemOptional.isEmpty()) {
      throw new AppException(ErrorCode.NO_SUCH_PROBLEM_IN_CONTEST);
    }
    ContestProblem contestProblem = contestProblemOptional.get();
    if (!CommonUtil.ensureCreatedBy(contestProblem, user)) {
      throw new AppException(ErrorCode.OBJECT_NOT_CREATED_BY_USER);
    }
    Optional<Submission> contestProblemSubmission = submissionRepository.findByContestAndProblem(
        contest, problem);
    if (contestProblemSubmission.isPresent()) {
      throw new AppException(ErrorCode.CANNOT_DELETE_CONTEST_PROBLEM_HAS_SUBMISSION);
    }
    contestProblemRepository.delete(contestProblem);
  }

  @Override
  public ContestDTO joinContest(Long contestId, String password) {
    User user = UserContext.getCurrentUser();
    Contest contest = contestRepository.findById(contestId)
        .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_CONTEST));
    switch (contest.getContestType()) {
      case PUBLIC:
        addUserToRanking(contest, user);
        break;
      case SECRET_WITH_PASSWORD:
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(password, contest.getPassword())) {
          throw new AppException(ErrorCode.BAD_PASSWORD);
        }
        addUserToRanking(contest, user);
        break;
      default:
    }
    return contestMapper.entityToDTO(contest);

  }

  @Override
  public PageDTO<RankingUserDTO> findAllUsers(Integer page, Integer pageSize,
      RankingUserQuery query, Long id) {
    Contest contest = contestRepository.findById(id)
        .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_CONTEST));
    Pageable pageable = PageRequest.of(page, pageSize);
    List<RankingUser> rankingUserList = rankingUserRepository.findByContestAndParams(contest, query,
        pageable).getContent();
    List<RankingUserDTO> rankingUserDTOs = rankingUserList.stream()
        .map(rankingUserMapper::entityToDTO).collect(Collectors.toList());
    return new PageDTO<>(page, pageSize, (long) rankingUserDTOs.size(), rankingUserDTOs);
  }

  @Override
  @Transactional
  public void deleteUsers(List<Long> userIdList, Long contetsId) {
    Contest contest = contestRepository.findById(contetsId)
        .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_CONTEST));
    for (Long userId: userIdList){
      rankingUserRepository.deleteByContestAndUserId(contest, userId);
    }
//    List<RankingUser> rankingUserList = rankingUserRepository.findAllByUserId(userIdList);
//    rankingUserRepository.deleteAll(rankingUserList);
  }

  @Override
  public List<RankingUserDTO> addUsers(List<Long> userIdList, Long contestId) {
    Contest contest = contestRepository.findById(contestId)
        .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_CONTEST));

    Set<User> userSet = rankingUserRepository.findByContest(contest).stream()
        .map(RankingUser::getUser).collect(Collectors.toSet());
    List<RankingUser> addRankingUserList = new ArrayList<>();
    List<User> userList = userRepository.findAllById(userIdList);
    for (User user : userList) {
      if (!userSet.contains(user)) {
        RankingUser rankingUser = rankingUserRepository.save(
            RankingUserFactory.create(user, contest));
        addRankingUserList.add(rankingUser);
      } else {
        throw new AppException(ErrorCode.USER_ALREADY_IN_CONTEST);
      }
    }
    return rankingUserMapper.toRankingUserDTOs(addRankingUserList);
  }

  @Override
  public PageDTO<ContestDTO> adminGetContests(Integer page, Integer size,
      ContestQuery contestQuery) {
    User user = UserContext.getCurrentUser();
    Pageable pageable = PageRequest.of(page, size, Direction.DESC, "startDate");
    Specification<Contest> cs = (root, criteriaQuery, criteriaBuilder) -> {
      List<Predicate> predicateList = new ArrayList<>();
      String name = contestQuery.getName();
//          search by name
      if (null != name && !name.isEmpty()) {
        predicateList.add(
            criteriaBuilder.like(root.get("name").as(String.class), "%" + name + "%"));
      }
//          if user is admin and is not super admin -> can only query contest create by self
      if (user != null && user.isAdmin() && !CommonUtil.isSuperAdmin(user)) {
        predicateList.add(criteriaBuilder.equal(root.get("author").get("id"), user.getId()));
      }

      Predicate[] p = new Predicate[predicateList.size()];
      return criteriaBuilder.and(predicateList.toArray(p));
    };
    List<Contest> contestList = contestRepository.findAll(cs, pageable).getContent();
    for (Contest contest : contestList) {
      if ((contest.getStartDate().isBefore(LocalDateTime.now()) || contest.getStartDate()
          .isEqual(LocalDateTime.now())) && contest.getStatus().equals(ContestStatus.NOT_STARTED)) {
        setContestStatus(contest, ContestStatus.PROCESSING);
      }

      if ((contest.getEndDate().isBefore(LocalDateTime.now())) || (contest.getEndDate()
          .isEqual(LocalDateTime.now()))) {
        setContestStatus(contest, ContestStatus.ENDED);
      }
    }
    contestList = contestRepository.saveAll(contestList);
    Long count = contestRepository.count(cs);
    List<ContestDTO> contestDTOList = contestMapper.toContestDTOs(contestList);
    return new PageDTO<>(page, size, count, contestDTOList);
  }

  @Override
  public ContestDTO adminFindById(Long id) {
    User user = UserContext.getCurrentUser();
    Contest contest = contestRepository.findById(id)
        .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_CONTEST));
    if (!CommonUtil.ensureCreatedBy(contest, user)) {
      throw new AppException(ErrorCode.OBJECT_NOT_CREATED_BY_USER);
    }
    return contestMapper.entityToDTO(contest);
  }

  @Override
  public RankingDTO getRanking(Long contestId) throws JsonProcessingException {
    Optional<Contest> contestOptional = contestRepository.findById(contestId);
    if (contestOptional.isEmpty()) {
      throw new AppException(ErrorCode.NO_SUCH_CONTEST);
    }
    Contest contest = contestOptional.get();
    RankingDTO rankingDTO = new RankingDTO();
    if (contest.getStatus() == ContestStatus.PROCESSING){
      // get ranking from redis
      Set<String> userIds = redisTemplate.opsForZSet().reverseRange("contest:" + contestId, 0, -1);
      log.info("userIds: {}", userIds);
      assert userIds != null;
      List<RankingUserDTO> rankingUserDtos = new ArrayList<>();
      if (contest.getContestRuleType().equals(ContestRuleType.ACM)){
        userIds.forEach((userID) -> {
          Map<Object, Object> objectMap = redisTemplate.opsForHash()
              .entries("contest:" + contestId + ":user:" + userID);
          RankingUserDTO rankingUserDTO = new RankingUserDTO();
          rankingUserDTO.setSubmitCount(Integer.parseInt((String) objectMap.get("submitCount")));
          rankingUserDTO.setAcceptCount(Integer.parseInt((String) objectMap.get("acceptCount")));
          rankingUserDTO.setTime(Long.parseLong((String) objectMap.get("time")));
          rankingUserDTO.setSubmissionInfo(objectMap.get("submission_info").toString());
          rankingUserDTO.setUserName(objectMap.get("username").toString());
          rankingUserDtos.add(rankingUserDTO);
        });
      }
      if (contest.getContestRuleType().equals(ContestRuleType.OI)){
        userIds.forEach((userID) -> {
          Map<Object, Object> objectMap = redisTemplate.opsForHash()
              .entries("contest:" + contestId + ":user:" + userID);
          RankingUserDTO rankingUserDTO = new RankingUserDTO();
          rankingUserDTO.setScore(Integer.parseInt((String) objectMap.get("score")));
          rankingUserDTO.setSubmissionInfo(objectMap.get("submission_info").toString());
          rankingUserDTO.setUserName(objectMap.get("username").toString());
          rankingUserDtos.add(rankingUserDTO);
        });
      }
      rankingDTO.setRankingUserDTOs(rankingUserDtos);
    } else {
//      get rank from db
      if(contest.getContestRuleType().equals(ContestRuleType.ACM)){
        List<RankingUser> rankingUserList = rankingUserRepository.findByContestOrderByAcceptCountDescTimeAsc(contest);
        List<RankingUserDTO> dtos = new ArrayList<>();
        for (RankingUser ru: rankingUserList){
          RankingUserDTO rankingUserDTO = rankingUserMapper.entityToDTO(ru);
          rankingUserDTO.setSubmissionInfo(objectMapper.writeValueAsString(ru.getSubmissionInfo()));
          dtos.add(rankingUserDTO);
        }
        rankingDTO.setRankingUserDTOs(dtos);
      } else {
        List<RankingUser> rankingUserList = rankingUserRepository.findByContestOrderByScoreDesc(contest);
        List<RankingUserDTO> dtos = new ArrayList<>();
        for (RankingUser ru: rankingUserList){
          RankingUserDTO rankingUserDTO = rankingUserMapper.entityToDTO(ru);
          rankingUserDTO.setSubmissionInfo(objectMapper.writeValueAsString(ru.getSubmissionInfo()));
          dtos.add(rankingUserDTO);
        }
        rankingDTO.setRankingUserDTOs(dtos);
      }
    }
    return rankingDTO;
  }

  @Override
  public PageDTO<ProblemDTO> adminFindAllProblems(Long id, Integer page, Integer size,
      ContestProblemQuery contestProblemQuery) {
    User user = UserContext.getCurrentUser();
    Pageable pageable = PageRequest.of(page, size);

    Contest contest = contestRepository.findById(id)
        .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_CONTEST));

    List<ContestProblem> contestProblemList = contestProblemRepository.adminFindByContestAndParam(
        contest, contestProblemQuery, pageable);
    List<ProblemDTO> problemDTOs = contestProblemMapper.toContestProblemDTOs(contestProblemList);
    return new PageDTO<>(page, size, (long) problemDTOs.size(), problemDTOs);
  }

  private void addUserToRanking(Contest contest, User user) {
    Set<User> userSet = rankingUserRepository.findByContest(contest).stream()
        .map(RankingUser::getUser).collect(Collectors.toSet());
    if (!userSet.contains(user)) {
//      save ranking user
      rankingUserRepository.save(RankingUserFactory.create(user, contest));
    }
  }

  private void requirePassword(Contest contest) throws AppException {
    if (ContestType.SECRET_WITH_PASSWORD.equals(contest.getContestType())
        && contest.getPassword() == null) {
      throw new AppException(ErrorCode.NO_PASS_PROVIDED);
    }
  }

  private void setContestStatus(Contest contest, ContestStatus status) throws AppException {
    switch (status) {
      case PROCESSING:
        if (contest.getStatus() != ContestStatus.NOT_STARTED) {
          throw new AppException(ErrorCode.CAN_ONLY_CHANGE_FROM_NOT_STARTED);
        }
        contest.setStatus(ContestStatus.PROCESSING);
//        contest.setStartDate(LocalDateTime.now());
        contestRepository.save(contest);
        break;
      case ENDED:
        contest.setStatus(ContestStatus.ENDED);
//        contest.setEndDate(LocalDateTime.now());
        break;
      default:
        throw new AppException(ErrorCode.BAD_CONTEST_STATUS);
    }
  }
}
