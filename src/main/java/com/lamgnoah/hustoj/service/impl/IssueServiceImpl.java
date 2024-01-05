package com.lamgnoah.hustoj.service.impl;

import com.lamgnoah.hustoj.domain.enums.ErrorCode;
import com.lamgnoah.hustoj.domain.enums.IssueStatus;
import com.lamgnoah.hustoj.dto.IssueDto;
import com.lamgnoah.hustoj.entity.Issue;
import com.lamgnoah.hustoj.entity.Problem;
import com.lamgnoah.hustoj.entity.User;
import com.lamgnoah.hustoj.exception.AppException;
import com.lamgnoah.hustoj.repository.IssueRepository;
import com.lamgnoah.hustoj.repository.ProblemRepository;
import com.lamgnoah.hustoj.repository.UserRepository;
import com.lamgnoah.hustoj.service.IssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IssueServiceImpl implements IssueService {
    private final IssueRepository issueRepository;
    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;

    @Override
    public Page<IssueDto> page(IssueStatus status, Pageable pageable) {
        Page<IssueDto> issuePage = issueRepository.findByStatus(status, pageable)
            .map(IssueDto::fromEntity);
        List<IssueDto> listIssue = issuePage.getContent();
        List<Long> userIds = listIssue.stream()
            .map(IssueDto::getAuthorId)
            .collect(Collectors.toUnmodifiableList());
        Map<Long, String> mapUsernameById = userRepository.findAllById(userIds)
            .stream()
            .collect(Collectors.toMap(User::getId, User::getUsername));
        List<Long> problemIds = listIssue.stream()
            .map(IssueDto::getProblemId)
            .collect(Collectors.toUnmodifiableList());
        Map<Long, String> mapProblemTitleById = problemRepository.findAllById(problemIds)
            .stream()
            .collect(Collectors.toMap(Problem::getId, Problem::getTitle));
        return issuePage.map(issueDto -> {
            issueDto.setAuthorName(mapUsernameById.get(issueDto.getAuthorId()));
            issueDto.setProblemTitle(mapProblemTitleById.get(issueDto.getProblemId()));
            return issueDto;
        });
    }

    @Override
    public IssueDto detail(Long id) {
        IssueDto issueDto = issueRepository.findById(id)
            .map(IssueDto::fromEntity)
            .orElseThrow(() -> new AppException(ErrorCode.ISSUE_NOTFOUND));
        String username = userRepository.findById(issueDto.getAuthorId())
            .map(User::getUsername)
            .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_USER));
        String problemTitle = problemRepository.findById(issueDto.getProblemId())
            .map(Problem::getTitle)
            .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_PROBLEM));
        issueDto.setAuthorName(username);
        issueDto.setProblemTitle(problemTitle);
        return issueDto;
    }

    @Override
    public IssueDto save(IssueDto issueDto) {
        Long issueId = issueDto.getId();
        Issue issue;
        if (issueId == null) {
            issue = new Issue();
            issue.setName(issueDto.getName());
            issue.setDescription(issueDto.getDescription());
            issue.setStatus(IssueStatus.NEW);
            issue.setAuthorId(issueDto.getAuthorId());
            issue.setProblemId(issueDto.getProblemId());
        } else {
            issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new AppException(ErrorCode.ISSUE_NOTFOUND));
            issue.setStatus(issueDto.getStatus());
        }
        Issue savedIssue = issueRepository.save(issue);
        return IssueDto.fromEntity(savedIssue);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        issueRepository.deleteById(id);
    }
}
