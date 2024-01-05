package com.lamgnoah.hustoj.service;

import com.lamgnoah.hustoj.domain.enums.IssueStatus;
import com.lamgnoah.hustoj.dto.IssueDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IssueService {
    Page<IssueDto> page(IssueStatus status, Pageable pageable);

    IssueDto detail(Long id);

    IssueDto save(IssueDto issueDto);

    void delete(Long id);
}
