package com.lamgnoah.hustoj.service.impl;

import com.lamgnoah.hustoj.domain.enums.AuthorityName;
import com.lamgnoah.hustoj.domain.enums.ErrorCode;
import com.lamgnoah.hustoj.domain.enums.ProblemPermission;
import com.lamgnoah.hustoj.dto.ChangePasswordDTO;
import com.lamgnoah.hustoj.dto.PageDTO;
import com.lamgnoah.hustoj.dto.UserDTO;
import com.lamgnoah.hustoj.entity.Authority;
import com.lamgnoah.hustoj.entity.User;
import com.lamgnoah.hustoj.exception.AppException;
import com.lamgnoah.hustoj.mapper.UserMapper;
import com.lamgnoah.hustoj.query.UserQuery;
import com.lamgnoah.hustoj.repository.*;
import com.lamgnoah.hustoj.service.UserService;
import com.lamgnoah.hustoj.utils.CommonUtil;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final AuthorityRepository authorityRepository;
  private final UserMapper userMapper;
  private final ProblemRepository problemRepository;
  private final ContestProblemRepository contestProblemRepository;
  private final SubmissionRepository submissionRepository;
  private final RankingUserRepository rankingUserRepository;
  private final ContestRepository contestRepository;
  private final TokenRepository tokenRepository;

  @Transactional
  @Override
  public UserDTO create(UserDTO userDTO) {
    String username = userDTO.getUsername();
    if (userRepository.existsByUsername(username)) {
      throw new AppException(ErrorCode.DUPLICATED_USERNAME);
    }
    String email = userDTO.getEmail();
    if (userRepository.existsByEmail(email)) {
      throw new AppException(ErrorCode.DUPLICATED_EMAIL);
    }
    User user = new User();

    user.setPassword(userDTO.getPassword());
    user.setUsername(userDTO.getUsername());
    user.setEmail(userDTO.getEmail());

    List<Authority> authorities = new ArrayList<>();
    for (Authority authority : userDTO.getAuthorities()) {
      authorities.add(authorityRepository.findByName(authority.getName()));
    }
    user.setFirstname(userDTO.getFirstname());
    user.setLastname(userDTO.getLastname());
    user.setName(userDTO.getFirstname() + " " + userDTO.getLastname());
    user.setProblemPermission(ProblemPermission.valueOf(userDTO.getProblemPermission()));
    user.setAuthorities(authorities);

    return userMapper.entityToDTO(userRepository.save(user));
  }

  @Override
  public PageDTO<UserDTO> getAllUsers(Integer page, Integer size, UserQuery userQuery) {
    Pageable pageable = PageRequest.of(page, size, Direction.ASC, "createDate");

    Specification<User> us = ((root, criteriaQuery, criteriaBuilder) -> {
      List<Predicate> predicateList = new ArrayList<>();

      Long id = userQuery.getId();
      if (null != id) {
        predicateList.add(criteriaBuilder.equal(root.get("id"), id));
      }

      String username = userQuery.getUsername();
      if (!CommonUtil.isNull(username)) {
        predicateList.add(criteriaBuilder.like(root.get("username"), "%" + username + "%"));
      }

      String name = userQuery.getName();
      if (!CommonUtil.isNull(name)) {
        predicateList.add(criteriaBuilder.like(root.get("name"), "%" + name + "%"));
      }

      List<String> roleList = userQuery.getRole();
      if (null != roleList && !roleList.isEmpty()) {
        List<Predicate> subPredicateList = new ArrayList<>();
        for (String role : roleList) {
          Authority authority = authorityRepository.findByName(AuthorityName.valueOf(role));
          if (authority == null) {
            return null;
          }
          subPredicateList.add(criteriaBuilder.isMember(authority, root.get("authorities")));
        }
        Predicate[] subPredicates = new Predicate[subPredicateList.size()];
        predicateList.add(criteriaBuilder.or(subPredicateList.toArray(subPredicates)));
      }

      Predicate[] predicates = new Predicate[predicateList.size()];
      return criteriaBuilder.and(predicateList.toArray(predicates));
    });

    if (null == us) {
      return new PageDTO<>(page, 0, 0L, new ArrayList<>());
    }

    List<User> userList = userRepository.findAll(us, pageable).getContent();
    List<UserDTO> userDTOS = userList.stream().map(userMapper::entityToDTO)
        .collect(Collectors.toList());
    long count = userRepository.count(us);
    return new PageDTO<>(page, userList.size(), count, userDTOS);
  }

  @Override
  public UserDTO getUser(Long id) {
    Optional<User> userOptional = userRepository.findById(id);
    if (userOptional.isEmpty()) {
      throw new AppException(ErrorCode.NO_SUCH_USER);
    }
    return userMapper.entityToDTO(userOptional.get());
  }

  @Override
  @Transactional
  public UserDTO update(UserDTO userDTO) {
    User user = userRepository.findById(userDTO.getId())
        .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_USER));
    String username = userDTO.getUsername();
    if (null != username) {
      if (userRepository.existsByUsernameAndIdIsNot(username, userDTO.getId())) {
        throw new AppException(ErrorCode.DUPLICATED_USERNAME);
      }
      user.setUsername(username);
    }
    String email = userDTO.getEmail();
    if (null != email) {
      if (userRepository.existsByEmailAndIdIsNot(email, userDTO.getId())) {
        throw new AppException(ErrorCode.DUPLICATED_EMAIL);
      }
      user.setEmail(email);
    }
    if (null != userDTO.getEnabled()) {
      user.setEnabled(userDTO.getEnabled());
    }

    if (null != userDTO.getProblemPermission() && null != userDTO.getAuthorities()) {
      for (Authority authority : userDTO.getAuthorities()) {
        if (authority.getName().equals(AuthorityName.ROLE_ADMIN)) {
          user.setProblemPermission(ProblemPermission.valueOf(userDTO.getProblemPermission()));
        } else if (authority.getName().equals(AuthorityName.ROLE_SUPER_ADMIN)) {
          user.setProblemPermission(ProblemPermission.ALL);
        } else {
          user.setProblemPermission(ProblemPermission.NONE);
        }
      }
    }

    if (null != userDTO.getAuthorities()) {
      List<Authority> authorities = new ArrayList<>();
      for (Authority authority : userDTO.getAuthorities()) {
        authorities.add(authorityRepository.findByName(authority.getName()));
      }
      user.setAuthorities(authorities);
    }
    userRepository.save(user);
    return userMapper.entityToDTO(user);
  }

  @Override
  @Transactional
  public void delete(Long id) {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_USER));
//    delete entity relate to user
    contestProblemRepository.deleteAllByProblemAuthor(user);
    contestRepository.deleteAllByAuthor(user);
    problemRepository.deleteAllByAuthor(user);
    submissionRepository.deleteAllByAuthor(user);
    rankingUserRepository.deleteAllByUser(user);
    tokenRepository.deleteAllByUser(user);
    userRepository.delete(user);
  }

  @Override
  public Boolean changePassword(Long userId, ChangePasswordDTO changePasswordDTO) {
    User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.NO_SUCH_USER));
    if (!BCrypt.checkpw(changePasswordDTO.getOldPassword(), user.getPassword())) {
      return false;
    }
    user.setPassword(changePasswordDTO.getNewPassword());
    userRepository.save(user);
    return true;
  }
}
