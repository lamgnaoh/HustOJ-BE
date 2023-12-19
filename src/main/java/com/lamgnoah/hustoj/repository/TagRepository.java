package com.lamgnoah.hustoj.repository;


import com.lamgnoah.hustoj.entity.Tag;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {

  Optional<Tag> findByName(String name);



}
