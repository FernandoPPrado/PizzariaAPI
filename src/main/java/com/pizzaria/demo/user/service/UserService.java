package com.pizzaria.demo.user.service;

import com.pizzaria.demo.user.dto.UserRequestDTO;
import com.pizzaria.demo.user.dto.UserResponseDTO;
import com.pizzaria.demo.user.model.Role;
import com.pizzaria.demo.user.model.User;
import com.pizzaria.demo.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.boot.context.event.SpringApplicationEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("USUARIO NAO ENCONTRADO"));
    }


    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
        return UserResponseDTO.
                fromEntity(userRepository.
                        save(new User(userRequestDTO.name(), userRequestDTO.email(), passwordEncoder.encode(userRequestDTO.password()), Role.ROLE_USER)));
    }


    public UserResponseDTO findByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("USUARIO NAO ENCONTRADO"));
        return UserResponseDTO.fromEntity(user);
    }

    public UserResponseDTO updateUser(Integer id, UserRequestDTO userResponseDTO) {

        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("USUARIO NAO ENCONTRADO"));
        user.setName(userResponseDTO.name());
        user.setEmail(userResponseDTO.email());
        User saved = userRepository.save(user);
        return entityToResponse(saved);

    }

    public UserResponseDTO findById(Integer id) {
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("USUARIO NAO ENCONTRADO"));
        return UserResponseDTO.fromEntity(user);
    }

    public List<UserResponseDTO> findAll() {
        return userRepository.findAll().stream().map(UserResponseDTO::fromEntity).toList();
    }

    public void deleteUserById(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("Usuário não encontrado com id: " + id);
        }
        userRepository.deleteById(id);
    }


    private User requestToEntity(UserRequestDTO userReq) {
        return new User(userReq.name(), userReq.email(), userReq.password(), Role.ROLE_USER);
    }

    private UserResponseDTO entityToResponse(User userReq) {
        return new UserResponseDTO(userReq.getId(), userReq.getName(), userReq.getEmail(), userReq.getRole());
    }

}
