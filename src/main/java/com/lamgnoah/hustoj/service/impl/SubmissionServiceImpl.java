package com.lamgnoah.hustoj.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lamgnoah.hustoj.domain.AcmProblemStatus;
import com.lamgnoah.hustoj.domain.AcmProblemStatus.ContestProblemStatus;
import com.lamgnoah.hustoj.domain.AcmProblemStatus.ProblemStatus;
import com.lamgnoah.hustoj.domain.ContestProblemSubmitInfo;
import com.lamgnoah.hustoj.domain.OiProblemStatus;
import com.lamgnoah.hustoj.domain.UserContext;
import com.lamgnoah.hustoj.domain.enums.*;
import com.lamgnoah.hustoj.domain.pojos.JudgeResponse;
import com.lamgnoah.hustoj.domain.pojos.JudgeResult;
import com.lamgnoah.hustoj.dto.PageDTO;
import com.lamgnoah.hustoj.dto.SubmissionDTO;
import com.lamgnoah.hustoj.dto.TestcaseInfoDTO;
import com.lamgnoah.hustoj.entity.*;
import com.lamgnoah.hustoj.exception.AppException;
import com.lamgnoah.hustoj.judgeConfig.LanguageConfig;
import com.lamgnoah.hustoj.mapper.SubmissionMapper;
import com.lamgnoah.hustoj.query.SubmissionQuery;
import com.lamgnoah.hustoj.repository.*;
import com.lamgnoah.hustoj.service.SubmissionService;
import com.lamgnoah.hustoj.utils.CommonUtil;
import com.lamgnoah.hustoj.utils.HttpUtil;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.lamgnoah.hustoj.domain.enums.Result.COMPILE_ERROR;

@Service
@EnableRetry
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {

  private final ProblemRepository problemRepository;
  private final UserRepository userRepository;
  private final SubmissionRepository submissionRepository;
  private final SubmissionMapper submissionMapper;
  private final RankingUserRepository rankingUserRepository;
  private final ObjectMapper objectMapper;
  private final HttpUtil http;
  private final ContestRepository contestRepository;
  private final ContestProblemRepository contestProblemRepository;
  @Value("${judger.url}")
  private String judgeServerBaseURL;
  private String judgeURL;

  @PostConstruct
  public void init() {
    this.judgeURL = judgeServerBaseURL + "/judge";
  }


  @Override
  @Transactional
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
    SubmissionService submissionService = (SubmissionService) AopContext.currentProxy();
    submissionService.counter(dto);
    return dto;
  }

  @Override
//  @Transactional
  public SubmissionDTO createContestSubmission(SubmissionDTO submissionDTO)
      throws JsonProcessingException {
    User user = UserContext.getCurrentUser();
    Submission submission = submissionMapper.dtoToEntity(submissionDTO);
    submission.setAuthor(user);
    Contest contest = contestRepository.findById(submissionDTO.getContestId())
        .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_CONTEST));
    if (contest.getContestType().equals(ContestType.PUBLIC)
        && rankingUserRepository.findByContestAndUser(contest, user).isEmpty()) {
//       user not submit any problem to become contest competitor
      RankingUser rankingUser = RankingUser.builder().contest(contest).user(user).acceptCount(0)
          .time(0L)
          .score(0)
          .submitCount(0)

          .submissionInfo(new HashMap<>()).build();
      rankingUserRepository.save(rankingUser); // create ranking user
    }
    Optional<RankingUser> rankingUserOptional = rankingUserRepository.findByContestAndUser(contest,
        user);
    if (!user.isAdmin()) {
      requireContestUser(contest, rankingUserOptional);
      requireContestOnGoing(contest);
    }
    Problem problem = problemRepository.findById(submissionDTO.getProblemId())
        .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_PROBLEM));
    submission.setContest(contest);
    submission.setIsPractice(false);
    submission.setProblem(problem);
    JudgeResult judgeResult = judge(submission, problem);
    submission.setDuration(judgeResult.getRealTime());
    submission.setResult(judgeResult.getResult());
    submission.setResultDetail(objectMapper.writeValueAsString(judgeResult));
    submission.setMemory(judgeResult.getMemory());
    submission = submissionRepository.save(submission);
    SubmissionDTO dto = submissionMapper.entityToDTO(submission);
    SubmissionService submissionService = (SubmissionService) AopContext.currentProxy();
    submissionService.counter(dto);

    if (rankingUserOptional.isPresent()) {
      RankingUser rankingUser = rankingUserOptional.get();
      ContestProblem contestProblem = contestProblemRepository.findByContestAndProblem(contest,
          problem).orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_PROBLEM_IN_CONTEST));
      if (contest.getContestRuleType().equals(ContestRuleType.ACM)) {
        updateACMContestRank(rankingUser, submission, contestProblem);
      } else {
        updateOIContestRank(rankingUser, submission);
      }
    }
    return dto;
  }

  private void updateOIContestRank(RankingUser rankingUser, Submission submission)
      throws JsonProcessingException {
    Map<Long, ContestProblemSubmitInfo> submissionInfo = rankingUser.getSubmissionInfo();
    if (submissionInfo.get(submission.getProblem().getId()) == null) { // problem is not submit yet
      rankingUser.increaseSubmitCount();
      if (submission.getResult().equals(Result.ACCEPTED)) {
        rankingUser.increaseAcceptCount();
      }
      ContestProblemSubmitInfo info = ContestProblemSubmitInfo.builder()
          .score(0).build();
      JudgeResult result = objectMapper.readValue(submission.getResultDetail(),
          JudgeResult.class);
      info.setScore(result.getScore());
      rankingUser.addScore(result.getScore());
      submissionInfo.put(submission.getProblem().getId(), info);
    } else { // problem is submitted before
      rankingUser.increaseSubmitCount();
      if (submission.getResult().equals(Result.ACCEPTED)) {
        rankingUser.increaseAcceptCount();
      }
      ContestProblemSubmitInfo info = submissionInfo.get(submission.getProblem().getId());
      Integer lastScore = info.getScore(); // score previous submission
      Integer currentScore = objectMapper.readValue(submission.getResultDetail(),
          JudgeResult.class).getScore(); // score current submission
      info.setScore(currentScore);
      Integer totalScore = rankingUser.getScore();
      rankingUser.setScore(totalScore - lastScore + currentScore);
      submissionInfo.put(submission.getProblem().getId(), info); // update submission info in ranking user
    }
    rankingUser.setSubmissionInfo(submissionInfo);
    rankingUserRepository.save(rankingUser);
  }

  @Override
  public List<SubmissionDTO> findByContestProblem(Long contestId, Long problemId) {
    User user = UserContext.getCurrentUser();
    Problem problem =
        problemRepository
            .findById(problemId)
            .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_PROBLEM));
    Contest contest =
        contestRepository
            .findById(contestId)
            .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_CONTEST));
    List<Submission> submissionList =
        submissionRepository.findByContestAndProblemAndAuthor(
            contest, problem, user);
    return submissionMapper.toSubmissionDTOs(submissionList);
  }

  @Override
  public PageDTO<SubmissionDTO> findAllSubmissionByContest(Long contestId, Integer page,
      Integer size) {
    User user = UserContext.getCurrentUser();
    Contest contest =
        contestRepository
            .findById(contestId)
            .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_CONTEST));

    if (!CommonUtil.isAdmin(user)) {
      requireContestUser(contest, rankingUserRepository.findByContestAndUser(contest, user));
    }
    List<Submission> submissionList;
    Pageable pageable = PageRequest.of(page, size, Sort.Direction.ASC, "createDate");
    submissionList = submissionRepository.findByContest(contest, pageable);
    long total = submissionRepository.countByContest(contest);
    return new PageDTO<>(page, size, total, submissionMapper.toSubmissionDTOs(submissionList));
  }

  private void updateACMContestRank(RankingUser rankingUser, Submission submission,
      ContestProblem contestProblem) {
    Map<Long, ContestProblemSubmitInfo> submissionInfo = rankingUser.getSubmissionInfo();
    if (submissionInfo.get(submission.getProblem().getId()) == null) { // if problem is not submit yet
      ContestProblemSubmitInfo info = ContestProblemSubmitInfo.builder()
          .isAc(false)
          .acTime(0L)
          .errorNumber(0)
          .isFirstAc(false).build();
      rankingUser.increaseSubmitCount();
      if (Result.ACCEPTED.equals(submission.getResult())) { // submission is accepted
        rankingUser.increaseAcceptCount();
        info.setIsAc(true);
        long duration = Duration.between(contestProblem.getContest().getStartDate(),
            submission.getCreateDate()).toMillis();
        info.setAcTime(duration);
        rankingUser.addTime(info.getAcTime());
        if (contestProblem.getAcceptCount() == 1) {
          info.setIsFirstAc(true);
        }
      } else if (!Result.COMPILE_ERROR.equals(
          submission.getResult())) { // submission is wrong answer , time limit or memory limit
        info.setErrorNumber(info.getErrorNumber() + 1);
      }
      submissionInfo.put(submission.getProblem().getId(), info);
    } else {
      ContestProblemSubmitInfo info = submissionInfo.get(
          submission.getProblem().getId());
      if (Boolean.FALSE.equals(info.getIsAc())) { // if problem is not accepted previous
        rankingUser.increaseSubmitCount();
        if (Result.ACCEPTED.equals(submission.getResult())) { // submission is accepted
          rankingUser.increaseAcceptCount();
          info.setIsAc(true);
          long duration = Duration.between(contestProblem.getContest().getStartDate(),
              submission.getCreateDate()).toMillis();
          info.setAcTime(duration);
          rankingUser.addTime(
              info.getAcTime() + info.getErrorNumber() * 20 * 60 * 1000); // 20m is penalty time
          if (contestProblem.getAcceptCount() == 1) {
            info.setIsFirstAc(true);
          }
        } else if (!Result.COMPILE_ERROR.equals(
            submission.getResult())) { // submission is wrong answer , time limit or memory limit
          info.setErrorNumber(info.getErrorNumber() + 1);
        }
      }
      submissionInfo.put(submission.getProblem().getId(), info);
    }
    rankingUser.setSubmissionInfo(submissionInfo);
    rankingUserRepository.save(rankingUser);
  }

  @Override
  public SubmissionDTO findById(Long id) throws AppException {
    User user = UserContext.getCurrentUser();

    Submission submission = submissionRepository.findById(id)
        .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_SUBMISSION));
    Contest contest = submission.getContest();
    if (!submission.getAuthor().getId().equals(user.getId()) && !CommonUtil.isAdmin(user)
        && contest != null && Boolean.TRUE.equals(!submission.getIsPractice())) {
      requireContestUser(contest, rankingUserRepository.findByContestAndUser(contest, user));
    }
    SubmissionDTO dto = submissionMapper.entityToDTO(submission);
    if (submission.getResult().equals(Result.ACCEPTED)){
      Problem problem = submission.getProblem();
      List<Submission> acceptedSubmission = submissionRepository.findByProblemAndResult(problem,
          Result.ACCEPTED);
      List<Integer> memoryList = acceptedSubmission.stream().map(Submission::getMemory).collect(
          Collectors.toList());
      List<Integer> durationList = acceptedSubmission.stream().map(Submission::getDuration).collect(
          Collectors.toList());
      Double memoryPercentitle = calculatePercentageDifferent(memoryList, submission.getMemory());
      Double durationPercentitle = calculatePercentageDifferent(durationList, submission.getDuration());
      if (dto.getContestId() == null){
        dto.setMemoryPercentile(memoryPercentitle);
        dto.setDurationPercentile(durationPercentitle);
      }
    }
    return dto;
  }

  private Double calculatePercentageDifferent(List<Integer> numberList, Integer targetNumber) {
    int count = 0;
    for (Integer number : numberList) {
      if (number < targetNumber) {
        count++;
      }
    }
    double percentile = (count * 1.0 / numberList.size()) * 100;
    return 100 - percentile;
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
      problem = problemRepository.findById(problemId)
          .orElseThrow(() -> new AppException(ErrorCode.PROBLEM_NOTFOUND));
    }

    String username = submissionQuery.getUsername();
    User user = null;
    if (null != username) {
      user = userRepository.findUserByUsername(username)
          .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_USER));
    } else if (Boolean.TRUE.equals(submissionQuery.getIsPersonal())) {
      user = currentUser;
    }

    Problem finalProblem = problem;
    User finalUser = user;
    Specification<Submission> specification = (root, criteriaQuery, criteriaBuilder) -> {
      List<Predicate> predicateList = new ArrayList<>();

      Boolean isPersonal = submissionQuery.getIsPersonal();
      if (null != isPersonal && isPersonal) {
        predicateList.add(criteriaBuilder.equal(root.get("author"), currentUser));
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
    List<Submission> submissionList = submissionRepository.findAll(specification, pageable)
        .getContent();

    List<SubmissionDTO> submissionDTOList = submissionMapper.toSubmissionDTOs(submissionList);
    long count = submissionRepository.count(specification);
    return new PageDTO<>(page, size, count, submissionDTOList);
  }

  @Override
  public List<SubmissionDTO> findByPracticeProblem(Long problemId) {
    Problem problem = problemRepository.findById(problemId)
        .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_PROBLEM));
    User user = UserContext.getCurrentUser();
    List<Submission> submissionList = submissionRepository.findByProblemAndIsPracticeAndAuthor(
        problem, true, user);
    return submissionMapper.toSubmissionDTOs(submissionList);
  }

  @Override
  @Transactional
  public void counter(SubmissionDTO submissionDTO) throws JsonProcessingException {
    User user = userRepository.findById(submissionDTO.getAuthorId())
        .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_USER));
    Problem problem = problemRepository.findById(submissionDTO.getProblemId())
        .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_PROBLEM));

    statCounter(submissionDTO, user, problem);
  }

  @Retryable
  private void statCounter(SubmissionDTO submissionDTO, User user, Problem problem)
      throws JsonProcessingException {
    if (submissionDTO.getContestId() == null) {
      updateProblemStatus(submissionDTO, user, problem);
    } else {
      updateContestProblemStatus(submissionDTO, user, problem);
    }
    userRepository.save(user);
  }

  private void updateContestProblemStatus(SubmissionDTO submissionDTO, User user, Problem problem)
      throws JsonProcessingException {
    Contest contest = contestRepository.findById(1l)
        .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_CONTEST));
    ContestProblem contestProblem = contestProblemRepository.findByContestAndProblem(contest,
        problem).orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_PROBLEM_IN_CONTEST));
    if (contest.getContestRuleType().equals(ContestRuleType.ACM)) {
      String acmProblemStatusJson = user.getAcmProblemsStatus();
      AcmProblemStatus acmProblemStatus = objectMapper.readValue(acmProblemStatusJson,
          AcmProblemStatus.class);
      if (!acmProblemStatus.getContestProblem().containsKey(problem.getId())) { // if user not submit this contest problem before
        acmProblemStatus.getContestProblem().put(problem.getId(),
            AcmProblemStatus.ContestProblemStatus.builder().id(problem.getId())
                .status(submissionDTO.getResult()).build());
        contestProblem.setSubmitCount(contestProblem.getSubmitCount() + 1);
        if (Result.ACCEPTED.equals(Result.valueOf(submissionDTO.getResult()))) {
          contestProblem.setAcceptCount(contestProblem.getAcceptCount() + 1);
        }
        contestProblem.setAcceptRate(
            contestProblem.getAcceptCount() * 1.0 / contestProblem.getSubmitCount());
      } else { // if user submit this contest problem before
        ContestProblemStatus contestProblemStatus = acmProblemStatus.getContestProblem()
            .get(problem.getId());
        if (!contestProblemStatus.getStatus().equals(Result.ACCEPTED.name())) { // if previous submit is not accepted
          contestProblemStatus.setStatus(submissionDTO.getResult());
          acmProblemStatus.getContestProblem().put(problem.getId(), contestProblemStatus);
          contestProblem.setSubmitCount(contestProblem.getSubmitCount() + 1);
          if (Result.ACCEPTED.equals(Result.valueOf(submissionDTO.getResult()))) {
            contestProblem.setAcceptCount(contestProblem.getAcceptCount() + 1);
          }
          contestProblem.setAcceptRate(
              contestProblem.getAcceptCount() * 1.0 / contestProblem.getSubmitCount());
        }
      }
      user.setAcmProblemsStatus(objectMapper.writeValueAsString(acmProblemStatus));
    } else {
      String oiProblemStatusJson = user.getOiProblemsStatus();
      OiProblemStatus oiProblemStatus = objectMapper.readValue(oiProblemStatusJson,
          new TypeReference<>() {
          });
      String detail = submissionDTO.getResultDetail();
      JudgeResult judgeResult = objectMapper.readValue(detail, JudgeResult.class);
      if (!oiProblemStatus.getContestProblem().containsKey(problem.getId())) { // if user not submit this contest problem before
        oiProblemStatus.getContestProblem().put(problem.getId(),
            OiProblemStatus.ContestProblemStatus.builder().id(problem.getId())
                .status(submissionDTO.getResult()).score(judgeResult.getScore()).build());
        contestProblem.setSubmitCount(contestProblem.getSubmitCount() + 1);
        if (Result.ACCEPTED.equals(Result.valueOf(submissionDTO.getResult()))) {
          contestProblem.setAcceptCount(contestProblem.getAcceptCount() + 1);
        }
        contestProblem.setAcceptRate(contestProblem.getAcceptCount() * 1.0 / contestProblem.getSubmitCount());
      } else { // if user submit this contest problem before
        oiProblemStatus.getContestProblem().get(problem.getId()).setScore(judgeResult.getScore());
        oiProblemStatus.getContestProblem().get(problem.getId())
            .setStatus(submissionDTO.getResult());
        if (!oiProblemStatus.getContestProblem().get(problem.getId()).getStatus().equals(Result.ACCEPTED.name())){
          contestProblem.setSubmitCount(contestProblem.getSubmitCount() + 1);
          if (Result.ACCEPTED.equals(Result.valueOf(submissionDTO.getResult()))) {
            contestProblem.setAcceptCount(contestProblem.getAcceptCount() + 1);
          }
          contestProblem.setAcceptRate(
              contestProblem.getAcceptCount() * 1.0 / contestProblem.getSubmitCount());
        }
      }
      user.setOiProblemsStatus(objectMapper.writeValueAsString(oiProblemStatus));
    }
    contestProblemRepository.save(contestProblem);
  }

  private void updateProblemStatus(SubmissionDTO submissionDTO, User user, Problem problem)
      throws JsonProcessingException {
    problem.setSubmitCount(problem.getSubmitCount() + 1);
    if (submissionDTO.getResult().equals(Result.ACCEPTED.name())) {
      problem.setAcceptCount(problem.getAcceptCount() + 1);
    }
    problem.setAcceptRate(problem.getAcceptCount() * 1.0 / problem.getSubmitCount());
    user.setSubmitCount(user.getSubmitCount() + 1);
    if (problem.getRuleType().equals(ContestRuleType.ACM)) {
      String acmProblemStatusJson = user.getAcmProblemsStatus();
      AcmProblemStatus acmProblemStatus = objectMapper.readValue(acmProblemStatusJson,
          AcmProblemStatus.class);
      if (!acmProblemStatus.getProblems().containsKey(problem.getId())) { // if user not submit this problem before
        acmProblemStatus.getProblems().put(problem.getId(),
            AcmProblemStatus.ProblemStatus.builder().id(problem.getId())
                .status(submissionDTO.getResult()).build());
        if (submissionDTO.getResult().equals(Result.ACCEPTED.name())) {
          user.setAcCount(user.getAcCount() + 1);
        }
      } else {
        ProblemStatus problemStatus = acmProblemStatus.getProblems().get(problem.getId()); // if user submit this problem before
        if (!problemStatus.getStatus().equals(Result.ACCEPTED.name())) { // if previous submit is not accepted
          problemStatus.setStatus(submissionDTO.getResult());
          acmProblemStatus.getProblems().put(problem.getId(), problemStatus);
          if (submissionDTO.getResult().equals(Result.ACCEPTED.name())) {
            user.setAcCount(user.getAcCount() + 1);
          }
        }
      }
      user.setAcmProblemsStatus(objectMapper.writeValueAsString(acmProblemStatus));
    } else {
      String oiProblemStatusJson = user.getOiProblemsStatus();
      OiProblemStatus oiProblemStatus = objectMapper.readValue(oiProblemStatusJson,
          new TypeReference<>() {
          });
      if (!oiProblemStatus.getProblems().containsKey(problem.getId())) { //if user not submit this problem before
        String detail = submissionDTO.getResultDetail();
        JudgeResult judgeResult = objectMapper.readValue(detail, JudgeResult.class);
        oiProblemStatus.getProblems().put(problem.getId(),
            OiProblemStatus.ProblemStatus.builder().id(problem.getId())
                .status(submissionDTO.getResult()).score(judgeResult.getScore()).build());
        user.addScore(0, judgeResult.getScore());
        if (submissionDTO.getResult().equals(Result.ACCEPTED.name())) {
          user.setAcCount(user.getAcCount() + 1);
        }
      } else { // user submit this problem before
        if (!oiProblemStatus.getProblems().get(problem.getId()).getStatus().equals(Result.ACCEPTED.name())) { // user submission previous is not accepted
          Integer lastTimeScore = oiProblemStatus.getProblems().get(problem.getId()).getScore();
          Integer thisTimeScore = objectMapper.readValue(submissionDTO.getResultDetail(),
              JudgeResult.class).getScore();
          user.addScore(lastTimeScore, thisTimeScore);
          if (submissionDTO.getResult().equals(Result.ACCEPTED.name())) {
            user.setAcCount(user.getAcCount() + 1);
          }
        }
      }
      user.setOiProblemsStatus(objectMapper.writeValueAsString(oiProblemStatus));
    }
    user.setAcRate(user.getAcCount() * 1.0 / user.getSubmitCount());
    problemRepository.save(problem);
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
      if (err.equals("CompileError")) {
        result.setResult(COMPILE_ERROR);
      } else {
        result.setResult(Result.SYSTEM_ERROR);
      }
      result.setMessage(resBodyJson.get("data").textValue());
      if(problem.getRuleType().equals(ContestRuleType.OI)){
        result.setScore(0);
      }
      return result;
    }
    List<JudgeResponse> data = objectMapper.readValue(resBodyJson.get("data").toString(),
        new TypeReference<>() {
        });
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
            new TypeReference<>() {
            });
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
    if (!ContestStatus.PROCESSING.equals(contest.getStatus()) || (
        LocalDateTime.now().isBefore(contest.getStartDate()) || LocalDateTime.now()
            .isAfter(contest.getEndDate()))) {
      throw new AppException(ErrorCode.CONTEST_NOT_GOING);
    }
  }
}
