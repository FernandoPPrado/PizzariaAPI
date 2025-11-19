package com.pizzaria.demo.user.service;

import com.pizzaria.demo.user.dto.UserRequestDTO;
import com.pizzaria.demo.user.dto.UserResponseDTO;
import com.pizzaria.demo.user.model.Role;
import com.pizzaria.demo.user.model.User;
import com.pizzaria.demo.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.SpringApplicationEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
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
        log.debug("Carregando usuario com email = {}", email);


        User user = userRepository.findByEmailAndEnabledTrue(email).orElseThrow(() -> {
            log.warn("Usuário não encontrado ou desativado email={}", email);
            return new EntityNotFoundException("USUARIO NAO ENCONTRADO");
        });

        log.debug("Usuário encontrado id={} email={}", user.getId(), user.getEmail());

        return user;
    }


    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {

        log.info("Iniciando criação de usuário com email {}", userRequestDTO.email());

        try {
            log.debug("Mapeando DTO para user e salvando entidade, email = {}", userRequestDTO.email());
            UserResponseDTO userResponseDTO = UserResponseDTO.fromEntity(userRepository.save(new User(userRequestDTO.name(), userRequestDTO.email(), passwordEncoder.encode(userRequestDTO.password()), Role.ROLE_USER)));
            log.info("Usuario com id = {} e email = {} salvo corretamente", userResponseDTO.id(), userResponseDTO.email());
            return userResponseDTO;
        } catch (Exception e) {
            log.error("Erro inesperado ao criar usuario", e);
            throw e;
        }


    }


    public UserResponseDTO findByEmail(String email) {
        log.info("Iniciando busca de usuario com email = {}", email);

        try {
            log.debug("Buscando usuario com email = {}", email);
            User user = userRepository.findByEmailAndEnabledTrue(email).orElseThrow(() -> {
                log.warn("Usuário não encontrado ou desativado email={}", email);
                return new EntityNotFoundException("USUARIO NAO ENCONTRADO");
            });
            log.debug("Mapeando entidade para DTO, id = {}", user.getId());
            UserResponseDTO userResp = UserResponseDTO.fromEntity(user);
            log.info("Usuario com id = {} localizado pelo email = {}", user.getId(), user.getEmail());
            return userResp;
        } catch (Exception e) {
            log.error("Erro inesperado ao buscar usuario", e);
            throw e;
        }

    }

    public UserResponseDTO updateUser(Integer id, UserRequestDTO userResponseDTO) {


        log.info("Iniciando atualizaçao de usuario de id = {}", id);
        User user = userRepository.findByIdAndEnabledTrue(id).orElseThrow(() -> {
            log.warn("Usuário não encontrado ou desativado id = {}", id);
            return new EntityNotFoundException("USUARIO NAO ENCONTRADO");
        });


        log.debug("Atualizando dados id = {} antigos \nNome: {}\nEmail: {}", user.getId(), user.getName(), user.getEmail());

        user.setName(userResponseDTO.name());
        user.setEmail(userResponseDTO.email());

        try {
            log.debug("Salvando usuario com dados atualizados");
            User saved = userRepository.save(user);
            log.info("""
                    Usuário atualizado com sucesso id={}:
                    - Novo Nome: {}
                    - Novo Email: {}
                    """, saved.getId(), saved.getName(), saved.getEmail());
            return entityToResponse(saved);
        } catch (Exception e) {
            log.error("Erro inesperado ao salvar usuario", e);
            throw e;
        }


    }

    public UserResponseDTO findById(Integer id) {
        log.info("Iniciando busca de usuario com id = {}", id);

        try {
            log.debug("Buscando usuario com id = {}", id);
            User user = userRepository.findByIdAndEnabledTrue(id).orElseThrow(() -> {
                log.warn("Usuário não encontrado ou desativado ao buscar por id = {}", id);
                return new EntityNotFoundException("USUARIO NAO ENCONTRADO");

            });
            log.debug("Mapeando entidade para DTO, id = {}", user.getId());

            UserResponseDTO uerResp = UserResponseDTO.fromEntity(user);
            log.info("Usuario localizado com sucesso id = {} email = {}", user.getId(), user.getEmail());

            return uerResp;

        } catch (Exception e) {
            log.error("Erro inesperado ao buscar usuario com id = {}", id, e);
            throw e;
        }

    }

    public List<UserResponseDTO> findAll() {

        log.info("Iniciando listagem de usuários ativos");
        log.debug("Consultando repositório para buscar todos usuários enabled=true e mapeando entidade para DTO");
        List<UserResponseDTO> userList = userRepository.findAllByEnabledTrue().stream().map(UserResponseDTO::fromEntity).toList();
        log.info("Quantidade de usuários ativos encontrados = {}", userList.size());
        return userList;

    }

    public void deleteUserById(Integer id) {
        log.info("Iniciando desativação (soft delete) do usuário id={}", id);
        try {

            User user = userRepository.findByIdAndEnabledTrue(id).orElseThrow(() -> {
                log.warn("Usuário não encontrado ou já desativado id={}", id);
                return new EntityNotFoundException("Usuário não encontrado com id: " + id);
            });

            log.debug("""
                    Dados do usuário antes da desativação id={}:
                    - Nome: {}
                    - Email: {}
                    """, user.getId(), user.getName(), user.getEmail());

            user.setEnabled(false);
            userRepository.save(user);

            log.info("Usuário desativado com sucesso id={} email={}", user.getId(), user.getEmail());

        } catch (Exception e) {
            log.error("Erro inesperado ao desativar usuário id={}", id, e);
            throw e;
        }


    }


    private User requestToEntity(UserRequestDTO userReq) {
        return new User(userReq.name(), userReq.email(), userReq.password(), Role.ROLE_USER);
    }

    private UserResponseDTO entityToResponse(User userReq) {
        return new UserResponseDTO(userReq.getId(), userReq.getName(), userReq.getEmail(), userReq.getRole());
    }

}
