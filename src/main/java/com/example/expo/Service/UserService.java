
        package com.example.expo.Service;

import com.example.expo.Api.ApiException;
import com.example.expo.Model.User;
import com.example.expo.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User addUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }


    public List<User> getAllUsers() {
        return userRepository.findAll();
    }



    public void getUserById(Integer id) {
        User user= userRepository.findUserById(id);
        if(user==null){
            throw new ApiException("User not found");
        }
    }

    // Update User
    public void updateUser(Integer id, User user) {

        User oldUser = userRepository.findUserById(id);
        if(oldUser==null){
            throw new ApiException("User not found");
        }
        oldUser.setUsername(user.getUsername());
        oldUser.setEmail(user.getEmail());
        oldUser.setPhone(user.getPhone());
        if (user.getPassword() != null && !user.getPassword().isBlank()) oldUser.setPassword(passwordEncoder.encode(user.getPassword()));
        oldUser.setRole(user.getRole());
        oldUser.setStatus(user.getStatus());
        userRepository.save(oldUser);
    }

    // Delete User
    public void deleteUser(Integer id) {
        User oldUser = userRepository.findUserById(id);
                if(oldUser==null){
                    throw new ApiException("User not found");
                }
        userRepository.delete(oldUser);
    }
}
