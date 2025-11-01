package com.pizzaria.demo.user.userService;

import com.pizzaria.demo.user.dto.UserRequestDTO;
import com.pizzaria.demo.user.dto.UserResponseDTO;
import com.pizzaria.demo.user.model.Role;
import com.pizzaria.demo.user.model.User;
import com.pizzaria.demo.user.repository.UserRepository;
import com.pizzaria.demo.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class UserServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Nested
    @DisplayName("Cenários Sucesso")
    class CenariosSucesso {

        @Test
        @DisplayName("CriarUserdeveRetornarUsuarioQuandoIdExiste")
        void criarUserdeveRetornarUsuarioQuandoIdExiste() {
            //Arrange
            UserResponseDTO usuarioCriado = userService.createUser(new UserRequestDTO("Teste", "Teste@Gmail.com", "123456"));
            //Act
            UserResponseDTO encontrado = userService.findById(usuarioCriado.id());
            //Assert
            assertEquals(usuarioCriado.id(), encontrado.id());
            assertEquals("Teste", encontrado.name());

        }

        @Test
        @DisplayName("UpdateUserDeveRetornarSucessoSeUsuarioExistir")
        public void UpdateUserDeveRetornarSucessoSeUsuarioExistir() {

            UserRequestDTO usuarioTeste = new UserRequestDTO("Teste", "Teste@Gmail.com", "123456");
            UserRequestDTO usuatioDTOchange = new UserRequestDTO("Alterado", "Alterado@Gmail.com", "Alterado123");

            UserResponseDTO savedUser = userService.createUser(usuarioTeste);

            UserResponseDTO usuarioAlterado = userService.updateUser(savedUser.id(), usuatioDTOchange);

            assertEquals(usuarioAlterado.name(), usuatioDTOchange.name());
        }

        @Test
        @DisplayName("DeleteUserDeveRemoverUsuarioSeExistir")
        void deleteUserDeveRemoverUsuarioSeExistir() {
            // Arrange
            UserResponseDTO usuarioCriado = userService.createUser(
                    new UserRequestDTO("Teste", "teste@teste.com", "123456")
            );
            // Act
            userService.deleteUserById(usuarioCriado.id());
            // Assert → agora o findById deve lançar exceção
            assertThrows(EntityNotFoundException.class,
                    () -> userService.findById(usuarioCriado.id()));
        }


    }

    @Nested
    @DisplayName("Cenários de Erro")
    class CenariosErro {

        @Test
        @DisplayName("criarUserdeveLancarErroQuandoUsuarioNaoExistir")
        public void criarUserdeveLancarErroQuandoUsuarioNaoExistir() {
            assertThrows(Exception.class, () -> userService.findById(-1));
        }

        @Test
        @DisplayName("UpdateUserDeveRetornarErroSeUsuarioNaoExistir")
        public void updateUserDeveRetornarErroSeUsuarioNaoExistir() {
            UserRequestDTO user = new UserRequestDTO("Teste", "Teste@Gmail.com", "123456");
            assertThrows(EntityNotFoundException.class, () -> userService.updateUser(-1, user));

        }

        @Test
        @DisplayName("DeleteDeveRetornarErroSeUsuraioNaoExistir")
        public void deleteDeveRetornarErroSeUsuraioNaoExistir() {
            assertThrows(Exception.class, () -> userService.deleteUserById(-1));
        }


    }

}
