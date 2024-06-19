package com.bookstore.services;

import com.bookstore.entity.User;
import com.bookstore.repository.IRoleRepository;
import com.bookstore.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServices {
    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IRoleRepository roleRepository;

    public List<User> findAll(){
        return userRepository.findAll();
    }

    public void save(User user){
        userRepository.save(user);
        Long userId = userRepository.getUserIdByUsername(user.getUsername());
        Long roleId = roleRepository.getRoleIdByName("USER");
        if(roleId != 0 && userId != 0){
            userRepository.addRoleToUser(userId, roleId);
        }
    }

    public User findUserByEmail(String email){
        return userRepository.findAll().stream()
                .filter(p -> p.getEmail().equals(email))
                .findFirst()
                .orElse(null);
    }

    public User findUserByUserId(Long userId){
        return userRepository.findById(userId).orElse(null);
    }

    public void deleteUserByUserId(Long userId){
        userRepository.deleteById(userId);
    }

}
