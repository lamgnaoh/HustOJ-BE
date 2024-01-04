package com.lamgnoah.hustoj.service.impl;

import com.lamgnoah.hustoj.dto.IssueDto;
import com.lamgnoah.hustoj.dto.PageDTO;
import com.lamgnoah.hustoj.repository.IssueRepository;
import com.lamgnoah.hustoj.service.IssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IssueServiceImpl implements IssueService {
    private final IssueRepository issueRepository;

    @Override
    public PageDTO<IssueDto> page(Long problemId, Pageable pageable) {
        return null;
    }

    @Override
    public IssueDto detail(Long id) {
        return null;
    }

    @Override
    public IssueDto save(IssueDto issueDto) {
        return null;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        issueRepository.deleteById(id);
    }
}
