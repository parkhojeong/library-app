package com.group.libraryapp.service.user

import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.dto.user.request.UserCreateRequest
import com.group.libraryapp.dto.user.request.UserUpdateRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UserServiceTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val userService: UserService
){
    @AfterEach
    fun cleanUp() {
        userRepository.deleteAll()
    }

    @Test
    fun saveUserTest() {
        //given
        val request = UserCreateRequest("name", null)

        //when
        userService.saveUser(request)

        //then
        val users = userRepository.findAll()
        assertThat(users).hasSize(1)
        assertThat(users[0].name).isEqualTo(request.name)
        assertThat(users[0].age).isNull()
    }

    @Test
    fun getUsersTest() {
        //given
        userRepository.saveAll(listOf(
            User("A", 20),
            User("B", null)
        ))

        //when
        val users = userService.getUsers()

        //then
        assertThat(users).hasSize(2)
        assertThat(users).extracting("name").containsExactlyInAnyOrder("A", "B")
        assertThat(users).extracting("age").containsExactlyInAnyOrder(20, null)
    }

    @Test
    fun updateUserNameTest() {
        //given
        val user = userRepository.save(User("A", null))
        val request = UserUpdateRequest(user.id!!, "B")

        //when
        userService.updateUserName(request)

        //then
        val updatedUser = userRepository.findById(user.id).get()
        assertThat(updatedUser.name).isEqualTo(request.name)
    }

    @Test
    fun deleteUserTest() {
        //given
        val user = userRepository.save(User("A", null))

        //when
        userService.deleteUser("A")

        //then
        assertThat(userRepository.findAll()).isEmpty()
    }
}