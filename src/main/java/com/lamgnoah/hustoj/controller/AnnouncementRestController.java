package com.lamgnoah.hustoj.controller;

import com.lamgnoah.hustoj.dto.AnnouncementDTO;
import com.lamgnoah.hustoj.dto.PageDTO;
import com.lamgnoah.hustoj.exception.AppException;
import com.lamgnoah.hustoj.service.AnnouncementService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
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
@RequestMapping(value = "/api/v1/announcements")
@RequiredArgsConstructor
public class AnnouncementRestController {

  private final AnnouncementService announcementService;

  @GetMapping
  public PageDTO<AnnouncementDTO> getAnnouncements(
      @RequestParam(defaultValue = "0") Integer page,
      @RequestParam(defaultValue = "5") Integer size) {
    return announcementService.findAnnouncement(page, size);
  }

  @GetMapping(value = "/{id}")
  public AnnouncementDTO getAnnouncement(@PathVariable Long id) throws AppException {
    return announcementService.findAnnouncementById(id);
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
  public AnnouncementDTO createAnnouncement(@Validated @RequestBody AnnouncementDTO announcementDTO)
      throws AppException {
    return announcementService.create(announcementDTO);
  }
  @PutMapping(value = "/{id}")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
  public AnnouncementDTO updateAnnouncement(
      @Validated @RequestBody AnnouncementDTO announcementDTO,
      @PathVariable Long id)
      throws AppException {
      announcementDTO.setId(id);
      return announcementService.update(announcementDTO);
  }
  @DeleteMapping(value = "/{id}")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN')")
  public AnnouncementDTO delete(@PathVariable Long id) throws AppException {
    return announcementService.delete(id);
  }
}



