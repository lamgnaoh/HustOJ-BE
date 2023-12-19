package com.lamgnoah.hustoj.service.impl;

import static com.lamgnoah.hustoj.domain.enums.Result.COMPILE_ERROR;
import static com.lamgnoah.hustoj.domain.enums.Result.JUDGE_CLIENT_ERROR;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lamgnoah.hustoj.domain.AcmContestRankInfo;
import com.lamgnoah.hustoj.domain.AcmContestRankInfo.ContestProblemSubmitInfo;
import com.lamgnoah.hustoj.domain.UserContext;
import com.lamgnoah.hustoj.domain.enums.ContestRuleType;
import com.lamgnoah.hustoj.domain.enums.ContestStatus;
import com.lamgnoah.hustoj.domain.enums.ContestType;
import com.lamgnoah.hustoj.domain.enums.Language;
import com.lamgnoah.hustoj.domain.enums.Result;
import com.lamgnoah.hustoj.domain.pojos.JudgeResponse;
import com.lamgnoah.hustoj.domain.pojos.JudgeResult;
import com.lamgnoah.hustoj.dto.PageDTO;
import com.lamgnoah.hustoj.dto.SubmissionDTO;
import com.lamgnoah.hustoj.dto.TestcaseInfoDTO;
import com.lamgnoah.hustoj.domain.AcmProblemStatus;
import com.lamgnoah.hustoj.domain.AcmProblemStatus.ContestProblemStatus;
import com.lamgnoah.hustoj.domain.AcmProblemStatus.ProblemStatus;
import com.lamgnoah.hustoj.entity.Contest;
import com.lamgnoah.hustoj.entity.ContestProblem;
import com.lamgnoah.hustoj.domain.OiProblemStatus;
import com.lamgnoah.hustoj.entity.Problem;
import com.lamgnoah.hustoj.entity.RankingUser;
import com.lamgnoah.hustoj.entity.Submission;
import com.lamgnoah.hustoj.domain.enums.ErrorCode;
import com.lamgnoah.hustoj.entity.User;
import com.lamgnoah.hustoj.exception.AppException;
import com.lamgnoah.hustoj.judgeConfig.LanguageConfig;
import com.lamgnoah.hustoj.mapper.SubmissionMapper;
import com.lamgnoah.hustoj.query.SubmissionQuery;
import com.lamgnoah.hustoj.repository.ContestProblemRepository;
import com.lamgnoah.hustoj.repository.ContestRepository;
import com.lamgnoah.hustoj.repository.ProblemRepository;
import com.lamgnoah.hustoj.repository.RankingUserRepository;
import com.lamgnoah.hustoj.repository.SubmissionRepository;
import com.lamgnoah.hustoj.repository.UserRepository;
import com.lamgnoah.hustoj.service.SubmissionService;
import com.lamgnoah.hustoj.utils.CommonUtil;
import com.lamgnoah.hustoj.utils.HttpUtil;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.criteria.Predicate;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {

  private final ProblemRepository problemRepository;
  private final UserRepository userRepository;
  private final SubmissionRepository submissionRepository;
  private final SubmissionMapper submissionMapper;
  private final RankingUserRepository rankingUserRepository;
  private final ObjectMapper objectMapper;
  private final HttpUtil http;

  @Value("${judger.url}")
  private String judgeServerBaseURL;
  private String judgeURL;
  private final ContestRepository contestRepository;
  private final ContestProblemRepository contestProblemRepository;


  @PostConstruct
  public void init(){
    this.judgeURL  = judgeServerBaseURL + "/judge";
  }


  @Override
  public SubmissionDTO createPracticeSubmission(SubmissionDTO submissionDTO)
      throws JsonProcessingException {
    User user = UserContext.getCurrentUser();
    Optional<Problem> problemOptional = problemRepository.findById(submissionDTO.getProblemId());
    if (problemOptional.isEmpty()) {
      throw new AppException(ErrorCode.PROBLEM_NOTFOUND);
    }
    Problem problem = problemOptional.get();

    Submission submission = submissionMapper.dtoToEntity(submissionDTO);
    submission.setAuthor(user);
    submission.setContest(null);
    submission.setProblem(problem);
    submission.setIsPractice(true);

    JudgeResult judgeResult = judge(submission, problem);

    submission.setDuration(judgeResult.getRealTime());
    submission.setResult(judgeResult.getResult());
    submission.setResultDetail(objectMapper.writeValueAsString(judgeResult));
    submission.setMemory(judgeResult.getMemory());
    SubmissionDTO dto = submissionMapper.entityToDTO(submissionRepository.save(submission));
    counter(dto);
    return dto;
  }

  @Override
  public SubmissionDTO createContestSubmission(SubmissionDTO submissionDTO)
      throws JsonProcessingException {
    User user = UserContext.getCurrentUser();
    Submission submission = submissionMapper.dtoToEntity(submissionDTO);
    submission.setAuthor(user);
    Contest contest =
        contestRepository
            .findById(submissionDTO.getContestId())
            .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_CONTEST));
    Optional<RankingUser> rankingUserOptional = rankingUserRepository.findByContestAndUser(contest, user);
    if (contest.getContestType().equals(ContestType.PUBLIC) && rankingUserOptional.isEmpty()) {
//       user not submit any problem to become contest competitor
      RankingUser rankingUser = RankingUser.builder()
          .contest(contest)
          .user(user)
          .build();
      rankingUserRepository.save(rankingUser); // create ranking user
    }
    if (!user.isAdmin()) {
      requireContestUser(contest, rankingUserOptional);
      requireContestOnGoing(contest);
    }
    Problem problem =
        problemRepository
            .findById(submissionDTO.getProblemId())
            .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_PROBLEM));
    submission.setContest(contest);
    submission.setIsPractice(false);
    submission.setProblem(problem);
    JudgeResult judgeResult = judge(submission, problem);

    submission.setDuration(judgeResult.getRealTime());
    submission.setResult(judgeResult.getResult());
    submission.setResultDetail(objectMapper.writeValueAsString(judgeResult));
    submission.setMemory(judgeResult.getMemory());
    submissionRepository.save(submission);

    if (rankingUserOptional.isPresent()){
      RankingUser rankingUser = rankingUserOptional.get();
      ContestProblem contestProblem =
          contestProblemRepository
              .findByContestAndProblem(contest, problem)
              .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_PROBLEM_IN_CONTEST));
      if (contest.getContestRuleType().equals(ContestRuleType.ACM)){
        updateACMContestRank(rankingUser,submission,contestProblem);
      }


    }

    return null;
  }

  private void updateACMContestRank(RankingUser rankingUser, Submission submission, ContestProblem contestProblem)
      throws JsonProcessingException {
    String submissionInfoJson = rankingUser.getSubmissionInfo();
    AcmContestRankInfo acmContestRankInfo = objectMapper.readValue(submissionInfoJson,
        AcmContestRankInfo.class);
    if (acmContestRankInfo.getProblemSubmitInfo().get(submission.getProblem().getId()) == null){ // if problem is not submit yet
      ContestProblemSubmitInfo info = ContestProblemSubmitInfo.builder()
          .isAc(false)
          .acTime(0L)
          .errorNumber(0)
          .isFirstAc(false)
          .build();
      if (Result.ACCEPTED.equals(submission.getResult())){ // submission is accepted
        rankingUser.increaseAcceptCount();
        info.setIsAc(true);
        long duration = Duration.between(contestProblem.getContest().getStartDate(), submission.getCreateDate()).toMillis();
        info.setAcTime(duration);
        rankingUser.addTime(info.getAcTime());
        if (contestProblem.getAcceptCount() == 1){
          info.setIsFirstAc(true);
        }
      }
      acmContestRankInfo.getProblemSubmitInfo().put(submission.getProblem().getId(), info);
    } else {
      ContestProblemSubmitInfo info = acmContestRankInfo.getProblemSubmitInfo().get(submission.getProblem().getId());
      if (Boolean.FALSE.equals(info.getIsAc())){ // if problem is not accepted previous
        rankingUser.increaseSubmitCount();
        if (Result.ACCEPTED.equals(submission.getResult())) { // submission is accepted
          rankingUser.increaseAcceptCount();
          info.setIsAc(true);
          long duration = Duration.between(contestProblem.getContest().getStartDate(),
              submission.getCreateDate()).toMillis();
          info.setAcTime(duration);
          rankingUser.addTime(info.getAcTime() + info.getErrorNumber() * 20 * 60 * 1000); // 20m is penalty time
          if (contestProblem.getAcceptCount() == 1){
            info.setIsFirstAc(true);
          }
        } else if (!Result.COMPILE_ERROR.equals(submission.getResult())){ // submission is wrong answer
          info.setErrorNumber(info.getErrorNumber() + 1);
        }
      }
    }
    rankingUser.setSubmissionInfo(objectMapper.writeValueAsString(acmContestRankInfo));
    rankingUserRepository.save(rankingUser);
  }

  @Override
  public SubmissionDTO findById(Long id) throws AppException {
    User user = UserContext.getCurrentUser();

    Submission submission =
        submissionRepository
            .findById(id)
            .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_SUBMISSION));
    Contest contest = submission.getContest();
    if (!submission.getAuthor().getId().equals(user.getId())
        && !CommonUtil.isAdmin(user)
        && contest != null
        && Boolean.TRUE.equals(!submission.getIsPractice())) {
      requireContestUser(contest, rankingUserRepository.findByContestAndUser(contest, user));
    }
    return submissionMapper.entityToDTO(submission);
  }

  private void requireContestUser(Contest contest, Optional<RankingUser> rankingUserOptional) {
    if (rankingUserOptional.isEmpty()) {
      ContestType type = contest.getContestType();
      if (Objects.requireNonNull(type) == ContestType.SECRET_WITH_PASSWORD) {
        throw new AppException(ErrorCode.NOT_PASS_CONTEST_USER);
      }
      throw new AppException(ErrorCode.NOT_PUBLIC_CONTEST_USER);
    }
  }

  @Override
  public PageDTO<SubmissionDTO> findAll(Integer page, Integer size,
      SubmissionQuery submissionQuery) {
    User currentUser = UserContext.getCurrentUser();
    Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createDate");

    Long problemId = submissionQuery.getProblemId();
    Problem problem = null;
    if (null != problemId) {
      problem =
          problemRepository
              .findById(problemId)
              .orElseThrow(() -> new AppException(ErrorCode.PROBLEM_NOTFOUND));
    }

    String username = submissionQuery.getUsername();
    User user = null;
    if (null != username) {
      user =
          userRepository
              .findUserByUsername(username)
              .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_USER));
    } else if (Boolean.TRUE.equals(submissionQuery.getIsPersonal())) {
      user = currentUser;
    }

    Problem finalProblem = problem;
    User finalUser = user;
    Specification<Submission> specification =
        (root, criteriaQuery, criteriaBuilder) -> {
          List<Predicate> predicateList = new ArrayList<>();

          Boolean isPersonal = submissionQuery.getIsPersonal();
          if (null != isPersonal && isPersonal) {
            predicateList.add(criteriaBuilder.equal(root.get("author"), currentUser ));
          }

          if (null != finalProblem) {
            predicateList.add(criteriaBuilder.equal(root.get("problem"), finalProblem));
          }

          if (null != finalUser) {
            predicateList.add(criteriaBuilder.equal(root.get("author"), finalUser));
          }

          Language language = submissionQuery.getLanguage();
          if (null != language) {
            predicateList.add(criteriaBuilder.equal(root.get("language"), language));
          }

          Boolean isPractice = submissionQuery.getIsPractice();
          if (null != isPractice) {
            predicateList.add(criteriaBuilder.equal(root.get("isPractice"), isPractice));
          }

          Predicate[] p = new Predicate[predicateList.size()];
          return criteriaBuilder.and(predicateList.toArray(p));
        };
    List<Submission> submissionList =  submissionRepository.findAll(specification, pageable).getContent();

    List<SubmissionDTO> submissionDTOList = submissionMapper.toSubmissionDTOs(submissionList);
    long count = submissionRepository.count(specification);
    return new PageDTO<>(page, size, count, submissionDTOList);
  }

  @Override
  public List<SubmissionDTO> findByPracticeProblem(Long problemId) {
    Problem problem =
        problemRepository
            .findById(problemId)
            .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_PROBLEM));
    User user = UserContext.getCurrentUser();
    List<Submission> submissionList =
        submissionRepository.findByProblemAndIsPracticeAndAuthor(problem, true, user);
    return submissionMapper.toSubmissionDTOs(submissionList);
  }

  @Override
  public void counter(SubmissionDTO submissionDTO) throws JsonProcessingException {
    User user =
        userRepository
            .findById(submissionDTO.getAuthorId())
            .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_USER));
    Problem problem =
        problemRepository
            .findById(submissionDTO.getProblemId())
            .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_PROBLEM));

    userStatCounter(submissionDTO, user , problem);
    problemStatCounter(submissionDTO, problem);
  }



  private void problemStatCounter(SubmissionDTO submissionDTO, Problem problem) {
    if (submissionDTO.getContestId() != null) {
      Contest contest = contestRepository
          .findById(submissionDTO.getContestId())
          .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_CONTEST));
      ContestProblem contestProblem = contestProblemRepository
          .findByContestAndProblem(contest, problem)
          .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_PROBLEM_IN_CONTEST));
      contestProblem.setSubmitCount(contestProblem.getSubmitCount() + 1);
      if (Result.ACCEPTED.equals(Result.valueOf(submissionDTO.getResult()))) {
        contestProblem.setAcceptCount(contestProblem.getAcceptCount() + 1);
      }
      contestProblem.setAcceptRate(contestProblem.getAcceptCount() * 1.0 / contestProblem.getSubmitCount());
      contestProblemRepository.save(contestProblem);
    } else {
      problem.setSubmitCount(problem.getSubmitCount() + 1);
      if (Result.ACCEPTED.equals(Result.valueOf(submissionDTO.getResult()))) {
        problem.setAcceptCount(problem.getAcceptCount() + 1);
      }
      problem.setAcceptRate(problem.getAcceptCount() * 1.0 / problem.getSubmitCount());
      problemRepository.save(problem);
    }
  }

  private void userStatCounter(SubmissionDTO submissionDTO, User user , Problem problem)
      throws JsonProcessingException {
    user.setSubmitCount(user.getSubmitCount() + 1);
    if (Result.ACCEPTED.equals(Result.valueOf(submissionDTO.getResult()))) {
      user.setAcCount(user.getAcCount() + 1);
    }
    user.setAcRate(user.getAcCount() * 1.0 / user.getSubmitCount());
    if (submissionDTO.getContestId() == null){
      updateProblemStatus(submissionDTO, user, problem);
    } else{
      updateContestProblemStatus(submissionDTO , user , problem);
    }
    userRepository.save(user);
  }

  private void updateContestProblemStatus(SubmissionDTO submissionDTO, User user, Problem problem)
      throws JsonProcessingException {
    Contest contest = contestRepository
        .findById(submissionDTO.getContestId())
        .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_CONTEST));
    if (contest.getContestRuleType().equals(ContestRuleType.ACM)) {
      String acmProblemStatusJson = user.getAcmProblemsStatus();
      AcmProblemStatus acmProblemStatus = objectMapper.readValue(acmProblemStatusJson,
          AcmProblemStatus.class);
      if (!acmProblemStatus.getContestProblem().containsKey(problem.getId())) {
        acmProblemStatus.getContestProblem().put(problem.getId(),
            AcmProblemStatus.ContestProblemStatus.builder()
                .id(problem.getId())
                .status(submissionDTO.getResult())
                .build());
      } else { // if contest problem already exist
        ContestProblemStatus contestProblemStatus = acmProblemStatus.getContestProblem().get(problem.getId());
        if (!contestProblemStatus.getStatus().equals(Result.ACCEPTED.name())){ // if previous submit is not accepted
          contestProblemStatus.setStatus(submissionDTO.getResult());
          acmProblemStatus.getContestProblem().put(problem.getId(), contestProblemStatus);
        }
      }
      user.setAcmProblemsStatus(objectMapper.writeValueAsString(acmProblemStatus));
    } else {
      String oiProblemStatusJson = user.getOiProblemsStatus();
      OiProblemStatus oiProblemStatus = objectMapper.readValue(oiProblemStatusJson, new TypeReference<>() {});
      String detail = submissionDTO.getResultDetail();
      JudgeResult judgeResult = objectMapper.readValue(detail, JudgeResult.class);
      if (!oiProblemStatus.getContestProblem().containsKey(problem.getId())){
        oiProblemStatus.getContestProblem().put(problem.getId(),
            OiProblemStatus.ContestProblemStatus.builder()
                .id(problem.getId())
                .status(submissionDTO.getResult())
                .score(judgeResult.getScore())
                .build());
      } else {
        oiProblemStatus.getContestProblem().get(problem.getId()).setScore(judgeResult.getScore());
        oiProblemStatus.getContestProblem().get(problem.getId()).setStatus(submissionDTO.getResult());
      }
      user.setOiProblemsStatus(objectMapper.writeValueAsString(oiProblemStatus));
    }
  }

  private void updateProblemStatus(SubmissionDTO submissionDTO, User user, Problem problem)
      throws JsonProcessingException {
    if (problem.getRuleType().equals(ContestRuleType.ACM)){
      String acmProblemStatusJson = user.getAcmProblemsStatus();
      AcmProblemStatus acmProblemStatus = objectMapper.readValue(acmProblemStatusJson, AcmProblemStatus.class);
      if (!acmProblemStatus.getProblems().containsKey(problem.getId())){
        acmProblemStatus.getProblems().put(problem.getId(),
            AcmProblemStatus.ProblemStatus.builder()
                .id(problem.getId())
                .status(submissionDTO.getResult())
                .build());
      } else {
        ProblemStatus problemStatus = acmProblemStatus.getProblems().get(problem.getId());
        if (!problemStatus.getStatus().equals(Result.ACCEPTED.name())){ // if previous submit is not accepted
          problemStatus.setStatus(submissionDTO.getResult());
          acmProblemStatus.getProblems().put(problem.getId(), problemStatus);
        }
      }
      user.setAcmProblemsStatus(objectMapper.writeValueAsString(acmProblemStatus));
    } else {
      String oiProblemStatusJson = user.getOiProblemsStatus();
      OiProblemStatus oiProblemStatus = objectMapper.readValue(oiProblemStatusJson, new TypeReference<>() {});
      if (!oiProblemStatus.getProblems().containsKey(problem.getId())){
        String detail = submissionDTO.getResultDetail();
        JudgeResult judgeResult = objectMapper.readValue(detail, JudgeResult.class);
        oiProblemStatus.getProblems().put(problem.getId(),
            OiProblemStatus.ProblemStatus.builder()
                .id(problem.getId())
                .status(submissionDTO.getResult())
                .score(judgeResult.getScore())
                .build());
      } else { // problem already in oi_problem_status
        if (!oiProblemStatus.getProblems().get(problem.getId()).getStatus().equals(Result.ACCEPTED.name())){
          Integer lastTimeScore = oiProblemStatus.getProblems().get(problem.getId()).getScore();
          Integer thisTimeScore = objectMapper
              .readValue(submissionDTO.getResultDetail(), JudgeResult.class)
              .getScore();
          user.addScore(lastTimeScore , thisTimeScore);
        }
      }
      user.setOiProblemsStatus(objectMapper.writeValueAsString(oiProblemStatus));
    }
  }


  private JudgeResult judge(Submission submission, Problem problem) throws JsonProcessingException {
    HashMap<String, Object> reqBody = new HashMap<>();
    reqBody.put("src", submission.getCode().replace("\\n", "\n"));
    reqBody.put("language_config", LanguageConfig.getLanguageConfig(submission.getLanguage()));
    reqBody.put("max_cpu_time", problem.getTimeLimit());
    reqBody.put("max_memory", problem.getRamLimit() * 1024 * 1024);
    reqBody.put("test_case_id", problem.getTestCaseId());
    reqBody.put("output", "true");
    String reqBodyJson = objectMapper.writeValueAsString(reqBody);
    String response;
    try {
      response = http.post(judgeURL, reqBodyJson);
    } catch (IOException e) {
      e.printStackTrace();
      throw new AppException(ErrorCode.JUDGE_POST_ERROR);
    }
    JsonNode resBodyJson = objectMapper.readTree(response);
    String err = resBodyJson.get("err").textValue();
    JudgeResult result = new JudgeResult();
    if (!CommonUtil.isNull(err)) {
      if (err.equals(COMPILE_ERROR.name())) {
        result.setResult(COMPILE_ERROR);
      } else if (err.equals(JUDGE_CLIENT_ERROR.name())) {
        result.setResult(JUDGE_CLIENT_ERROR);
      } else {
        result.setResult(Result.SYSTEM_ERROR);
      }
      result.setMessage(resBodyJson.get("data").textValue());
      result.setScore(0);
      return result;
    }
    List<JudgeResponse> data = objectMapper.readValue(resBodyJson.get("data").toString(), new TypeReference<>() {});
    Integer maxMemory = 0;
    Integer maxCPUTime = 0;
    Integer maxRealTime = 0;
    Integer passedCount = 0;
    Integer wrongAnswerCount = 0;
    Integer cpuTimeLimitExceededCount = 0;
    Integer timeLimitExceededCount = 0;
    Integer memoryLimitExceededCount = 0;
    Integer score = problem.getRuleType().equals(ContestRuleType.OI) ? 0 : null;
    result.setResult(Result.ACCEPTED);
    boolean resultResolved = false;
    for (JudgeResponse res : data) {
      if (res.getCpu_time() > maxCPUTime) {
        maxCPUTime = res.getCpu_time();
      }
      if (res.getMemory() > maxMemory) {
        maxMemory = res.getMemory();
      }
      if (res.getReal_time() > maxRealTime) {
        maxRealTime = res.getReal_time();
      }

      switch (res.getResult()) {
        case 0:
          passedCount++;
          break;
        case -1:
          wrongAnswerCount++;
          break;
        case 1:
          cpuTimeLimitExceededCount++;
          break;
        case 2:
          timeLimitExceededCount++;
          break;
        case 3:
          memoryLimitExceededCount++;
          break;
        default:
      }
      if (problem.getRuleType().equals(ContestRuleType.OI) && (res.getResult() == 0)) { // accepted
          List<TestcaseInfoDTO> testcaseInfos = objectMapper.readValue(problem.getTestCaseScore(),
              new TypeReference<>(){});
          score += testcaseInfos.get(res.getTest_case() - 1).getScore();
      }

      if (!resultResolved) {
        Integer r = res.getResult();
        if (r == -1 || r == 1 || r == 2 || r == 3 || r == 4 || r == 5) {
          result.setResult(integerToResult(r));
          resultResolved = true;
        }
      }
    }
    result.setCpuTime(maxCPUTime);
    result.setMemory(maxMemory);
    result.setRealTime(maxRealTime);
    result.setTotalCount(data.size());
    result.setPassedCount(passedCount);
    result.setWrongAnswerCount(wrongAnswerCount);
    result.setCpuTimeLimitExceededCount(cpuTimeLimitExceededCount);
    result.setMemoryLimitExceededCount(memoryLimitExceededCount);
    result.setTimeLimitExceededCount(timeLimitExceededCount);
    result.setScore(score);
    return result;
  }

  private Result integerToResult(Integer integer) {
    switch (integer) {
      case -1:
        return Result.WRONG_ANSWER;
      case 1:
        return Result.CPU_TIME_LIMIT_EXCEEDED;
      case 2:
        return Result.TIME_LIMIT_EXCEEDED;
      case 3:
        return Result.MEMORY_LIMIT_EXCEEDED;
      case 4:
        return Result.RUNTIME_ERROR;
      case 5:
        return Result.SYSTEM_ERROR;
      default:
        return Result.SYSTEM_ERROR;
    }
  }
  private void requireContestOnGoing(Contest contest) throws AppException {
    if (!ContestStatus.PROCESSING.equals(contest.getStatus())
        || (LocalDateTime.now().isBefore(contest.getStartDate())
        || LocalDateTime.now().isAfter(contest.getEndDate()))) {
      throw new AppException(ErrorCode.CONTEST_NOT_GOING);
    }
  }
}
