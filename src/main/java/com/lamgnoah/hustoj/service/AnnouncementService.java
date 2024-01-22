package com.lamgnoah.hustoj.service;

import com.lamgnoah.hustoj.dto.AnnouncementDTO;
import com.lamgnoah.hustoj.dto.PageDTO;
import com.lamgnoah.hustoj.exception.AppException;

public interface AnnouncementService {

  PageDTO<AnnouncementDTO> findAnnouncement(int page, int size);

  AnnouncementDTO findAnnouncementById(Long id) throws AppException;

  AnnouncementDTO create(AnnouncementDTO announcementDTO) throws AppException;

  AnnouncementDTO update(AnnouncementDTO announcementDTO) throws AppException;

  AnnouncementDTO delete(Long id) throws AppException;
}
