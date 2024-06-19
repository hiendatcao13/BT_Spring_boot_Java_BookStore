package com.bookstore.services;

import com.bookstore.entity.Role;
import com.bookstore.repository.IRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServices {
    @Autowired
    private IRoleRepository roleRepository;

    public List<Role> getAllRoles() { return roleRepository.findAll();}

    public Role getRoleById(Long id) { return roleRepository.findById(id).orElse(null);}

    public void addRole(Role role) { roleRepository.save(role);}

    public void deleteRole(Long id) { roleRepository.deleteById(id);}

    public void updateRole(Role role) { roleRepository.save(role);}

    public Long getRoleIdByName(String roleName) { return roleRepository.getRoleIdByName(roleName);}

    public List<Role> getRoleByIds(List<Long> roleIds) { return roleRepository.findAllById(roleIds);}

}
