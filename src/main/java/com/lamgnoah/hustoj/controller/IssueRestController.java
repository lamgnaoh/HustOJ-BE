package com.lamgnoah.hustoj.controller;

import com.lamgnoah.hustoj.dto.IssueDto;
import com.lamgnoah.hustoj.dto.PageDTO;
import com.lamgnoah.hustoj.security.JwtUser;
import com.lamgnoah.hustoj.service.IssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/issue")
public class IssueRestController {

    private final IssueService issueService;

    @GetMapping("/page")
    public PageDTO<IssueDto> getPage(@RequestParam(defaultValue = "10") int size,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam Long problemId) {
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "created_at");
        return issueService.page(problemId, pageable);
    }


    @GetMapping("/detail/{id}")
    public ResponseEntity<IssueDto> detail(@PathVariable Long id) {
        return ResponseEntity.ok(issueService.detail(id));
    }

    @PostMapping("/save")
    public ResponseEntity<IssueDto> save(@AuthenticationPrincipal JwtUser user,
                                         @RequestBody IssueDto issueDto) {
        issueDto.setAuthorId(user.getId());
        return ResponseEntity.ok(issueService.save(issueDto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        issueService.delete(id);
        return ResponseEntity.ok().build();
    }
}
