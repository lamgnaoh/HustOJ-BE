package com.lamgnoah.hustoj.service.impl;

import com.lamgnoah.hustoj.domain.UserContext;
import com.lamgnoah.hustoj.domain.enums.ErrorCode;
import com.lamgnoah.hustoj.dto.AnnouncementDTO;
import com.lamgnoah.hustoj.dto.PageDTO;
import com.lamgnoah.hustoj.entity.Announcement;
import com.lamgnoah.hustoj.entity.User;
import com.lamgnoah.hustoj.exception.AppException;
import com.lamgnoah.hustoj.mapper.AnnouncementMapper;
import com.lamgnoah.hustoj.repository.AnnouncementRepository;
import com.lamgnoah.hustoj.service.AnnouncementService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {
  private final AnnouncementRepository announcementRepository;
  private final AnnouncementMapper announcementMapper;

  @Override
  public PageDTO<AnnouncementDTO> findAnnouncement(int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "modifiedDate");
    List<Announcement> announcements = announcementRepository.findAll(pageable).getContent();
    List<AnnouncementDTO> announcementDTOList = new ArrayList<>();
    for (Announcement announcement : announcements) {
      announcementDTOList.add(announcementMapper.entityToDTO(announcement));
    }
    return new PageDTO<>(page , size , (long) announcementDTOList.size(),announcementDTOList);
  }

  @Override
  public AnnouncementDTO findAnnouncementById(Long id) throws AppException {
    Announcement announcement =
        announcementRepository
            .findById(id)
            .orElseThrow(
                () -> new AppException(ErrorCode.NO_SUCH_ANNOUNCEMENT));
    return announcementMapper.entityToDTO(announcement);
  }

  @Override
  public AnnouncementDTO create(AnnouncementDTO announcementDTO) throws AppException {
    User user = UserContext.getCurrentUser();
    Optional<Announcement> announcementOptional =
        announcementRepository.findByTitle(announcementDTO.getTitle());
    if (announcementOptional.isPresent()) {
      throw new AppException(ErrorCode.HAVE_SUCH_ANNOUNCEMENT);
    }
    Announcement announcement = announcementMapper.dtoToEntity(announcementDTO);
    announcement.setAuthor(user);
    return announcementMapper.entityToDTO(announcementRepository.save(announcement));
  }

  @Override
  public AnnouncementDTO update(AnnouncementDTO announcementDTO) throws AppException {
    Optional<Announcement> announcementOptional =
        announcementRepository.findById(announcementDTO.getId());
    if (announcementOptional.isEmpty()) {
      throw new AppException(ErrorCode.NO_SUCH_ANNOUNCEMENT);
    }
    Announcement announcement = announcementOptional.get();
    if (!announcement.getTitle().equals(announcementDTO.getTitle())){
      announcement.setTitle(announcementDTO.getTitle());
    }
    if (!announcement.getContent().equals(announcementDTO.getContent())){
      announcement.setContent(announcementDTO.getContent());
    }
    return announcementMapper.entityToDTO(announcementRepository.save(announcement));
  }

  @Override
  public AnnouncementDTO delete(Long id) throws AppException {
    Announcement announcement =
        announcementRepository
            .findById(id)
            .orElseThrow(
                () -> new AppException(ErrorCode.NO_SUCH_ANNOUNCEMENT));
    announcementRepository.delete(announcement);
    return announcementMapper.entityToDTO(announcement);
  }
}
