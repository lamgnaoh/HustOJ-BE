package com.lamgnoah.hustoj.service;

import com.lamgnoah.hustoj.dto.IssueDto;
import com.lamgnoah.hustoj.dto.PageDTO;
import org.springframework.data.domain.Pageable;

public interface IssueService {
    PageDTO<IssueDto> page(Long problemId, Pageable pageable);

    IssueDto detail(Long id);

    IssueDto save(IssueDto issueDto);

    void delete(Long id);
}
