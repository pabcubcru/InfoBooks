package com.pabcubcru.infobooks.service;

import com.pabcubcru.infobooks.models.User;
import com.pabcubcru.infobooks.services.UserService;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class UserServiceTests {

    @Autowired
    private UserService userService;

    @Test
    public void shouldFindUserById() throws Exception {
        User user = this.userService.findUserById("userTest-pablo123");
        Assertions.assertThat(user.getUsername()).isEqualTo("pablo123");
        Assertions.assertThat(user.getCity()).isEqualTo("Sevilla");
    }

    @Test
    public void shouldFindUserByUsername() throws Exception {
        User user = this.userService.findByUsername("juan1234");
        Assertions.assertThat(user.getId()).isEqualTo("userTest-juan1234");
        Assertions.assertThat(user.getEmail()).isEqualTo("juan@us.es");
    }

    @Test
    public void shouldExistsUserWithSameEmail() throws Exception {
        Boolean exists = this.userService.existUserWithSameEmail("pablo@us.es");
        Assertions.assertThat(exists).isEqualTo(true);
    }

    @Test
    public void shouldNotExistsUserWithSameEmail() throws Exception {
        Boolean exists = this.userService.existUserWithSameEmail("email@us.es");
        Assertions.assertThat(exists).isEqualTo(false);
    }

    @Test
    public void shouldExistsUserWithSameUsername() throws Exception {
        Boolean exists = this.userService.existUserWithSameUsername("pablo123");
        Assertions.assertThat(exists).isEqualTo(true);
    }

    @Test
    public void shouldNotExistsUserWithSameUsername() throws Exception {
        Boolean exists = this.userService.existUserWithSameUsername("userFake");
        Assertions.assertThat(exists).isEqualTo(false);
    }

    
    
}