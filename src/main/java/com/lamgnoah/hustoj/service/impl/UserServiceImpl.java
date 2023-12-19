package com.lamgnoah.hustoj.service.impl;

import com.lamgnoah.hustoj.domain.enums.AuthorityName;
import com.lamgnoah.hustoj.domain.enums.ErrorCode;
import com.lamgnoah.hustoj.domain.enums.ProblemPermission;
import com.lamgnoah.hustoj.dto.PageDTO;
import com.lamgnoah.hustoj.dto.UserDTO;
import com.lamgnoah.hustoj.entity.Authority;
import com.lamgnoah.hustoj.entity.User;
import com.lamgnoah.hustoj.exception.AppException;
import com.lamgnoah.hustoj.query.UserQuery;
import com.lamgnoah.hustoj.repository.UserRepository;
import com.lamgnoah.hustoj.repository.AuthorityRepository;
import com.lamgnoah.hustoj.security.JwtUser;
import com.lamgnoah.hustoj.security.JwtUserFactory;
import com.lamgnoah.hustoj.service.UserService;
import com.lamgnoah.hustoj.utils.CommonUtil;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final AuthorityRepository authorityRepository;

  @Transactional
  @Override
  public User create(UserDTO userDTO) {
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

    return userRepository.save(user);
  }

  @Override
  public PageDTO<JwtUser> getAllUsers(Integer page, Integer size, UserQuery userQuery) {
    Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "acCount");

    Specification<User> us =
        ((root, criteriaQuery, criteriaBuilder) -> {
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
    long count = userRepository.count(us);
    return new PageDTO<>(page, userList.size(), count, JwtUserFactory.createList(userList));
  }
}
