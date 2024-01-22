package com.lamgnoah.hustoj.repository;

import com.lamgnoah.hustoj.entity.Announcement;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AnnouncementRepository
    extends JpaRepository<Announcement, Long>, JpaSpecificationExecutor<Announcement> {

  Optional<Announcement> findByTitle(String title);
}
