package org.bootstrapbugz.api.admin.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.bootstrapbugz.api.admin.request.AdminRequest;
import org.bootstrapbugz.api.admin.request.ChangeRoleRequest;
import org.bootstrapbugz.api.admin.service.AdminService;
import org.bootstrapbugz.api.auth.service.JwtService;
import org.bootstrapbugz.api.user.mapper.UserMapper;
import org.bootstrapbugz.api.user.model.Role;
import org.bootstrapbugz.api.user.repository.UserRepository;
import org.bootstrapbugz.api.user.response.UserResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminServiceImpl implements AdminService {
  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final UserMapper userMapper;

  public AdminServiceImpl(
      UserRepository userRepository, JwtService jwtService, UserMapper userMapper) {
    this.userRepository = userRepository;
    this.jwtService = jwtService;
    this.userMapper = userMapper;
  }

  @Override
  public List<UserResponse> findAllUsers() {
    return userRepository.findAllWithRoles().stream()
        .map(userMapper::userToUserResponse)
        .collect(Collectors.toList());
  }

  @Override
  public void changeRole(ChangeRoleRequest changeRoleRequest) {
    final var users = userRepository.findAllByUsernameIn(changeRoleRequest.getUsernames());
    users.forEach(
        user -> {
          final var roles =
              changeRoleRequest.getRoleNames().stream().map(Role::new).collect(Collectors.toSet());
          user.setRoles(roles);
          jwtService.invalidateAllTokens(user.getUsername());
          jwtService.deleteAllRefreshTokensByUser(user.getUsername());
        });
    userRepository.saveAll(users);
  }

  @Override
  public void lock(AdminRequest adminRequest) {
    final var users = userRepository.findAllByUsernameIn(adminRequest.getUsernames());
    users.forEach(
        user -> {
          user.setNonLocked(false);
          jwtService.invalidateAllTokens(user.getUsername());
          jwtService.deleteAllRefreshTokensByUser(user.getUsername());
        });
    userRepository.saveAll(users);
  }

  @Override
  public void unlock(AdminRequest adminRequest) {
    final var users = userRepository.findAllByUsernameIn(adminRequest.getUsernames());
    users.forEach(user -> user.setNonLocked(true));
    userRepository.saveAll(users);
  }

  @Override
  public void activate(AdminRequest adminRequest) {
    final var users = userRepository.findAllByUsernameIn(adminRequest.getUsernames());
    users.forEach(user -> user.setActivated(true));
    userRepository.saveAll(users);
  }

  @Override
  public void deactivate(AdminRequest adminRequest) {
    final var users = userRepository.findAllByUsernameIn(adminRequest.getUsernames());
    users.forEach(
        user -> {
          user.setActivated(false);
          jwtService.invalidateAllTokens(user.getUsername());
          jwtService.deleteAllRefreshTokensByUser(user.getUsername());
        });
    userRepository.saveAll(users);
  }

  @Override
  @Transactional
  public void delete(AdminRequest adminRequest) {
    final var users = userRepository.findAllByUsernameIn(adminRequest.getUsernames());
    users.forEach(
        user -> {
          jwtService.invalidateAllTokens(user.getUsername());
          jwtService.deleteAllRefreshTokensByUser(user.getUsername());
          userRepository.delete(user);
        });
  }
}
