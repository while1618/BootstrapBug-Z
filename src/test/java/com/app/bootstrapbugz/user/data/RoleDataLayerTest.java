package com.app.bootstrapbugz.user.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.app.bootstrapbugz.user.model.Role;
import com.app.bootstrapbugz.user.model.RoleName;
import com.app.bootstrapbugz.user.repository.RoleRepository;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class RoleDataLayerTest {
  @Autowired private RoleRepository roleRepository;

  @Test
  void findAllRolesByNameIn() {
    Set<RoleName> names = Set.of(RoleName.USER, RoleName.ADMIN);
    List<Role> roles = roleRepository.findAllByNameIn(names);
    assertThat(roles).hasSize(2);
  }

  @Test
  void findRoleByName() {
    Role role = roleRepository.findByName(RoleName.USER).orElseThrow();
    assertEquals(1L, role.getId());
  }
}
