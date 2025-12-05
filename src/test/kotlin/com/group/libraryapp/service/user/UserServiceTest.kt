package com.group.libraryapp.service.user

import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistory
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistoryRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanStatus
import com.group.libraryapp.dto.user.request.UserCreateRequest
import com.group.libraryapp.dto.user.request.UserUpdateRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UserServiceTest @Autowired constructor(
    private val userRepository: UserRepository,
    private val userService: UserService,
    private val userLoanHistoryRepository: UserLoanHistoryRepository,
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

    @Test
    @DisplayName("대출 기록이 없는 유저도 응답에 포함")
    fun getUserLoanHistoriesTest1() {
        //given
        userRepository.save(User("A", null))

        //when
        val userLoanHistories = userService.getUserLoanHistories()

        //then
        assertThat(userLoanHistories).hasSize(1)
        assertThat(userLoanHistories[0].name).isEqualTo("A")
        assertThat(userLoanHistories[0].books).isEmpty()
    }

    @Test
    @DisplayName("대출 기록이 2개 이상인 경우 정상 동작")
    fun getUserLoanHistoriesTest2() {
        //given
        val user = userRepository.save(User("A", null))
        userLoanHistoryRepository.saveAll(listOf(
            UserLoanHistory.fixture(user, "book1", UserLoanStatus.LOANED),
            UserLoanHistory.fixture(user, "book2", UserLoanStatus.LOANED),
            UserLoanHistory.fixture(user, "book3", UserLoanStatus.RETURNED),
        ))

        //when
        val userLoanHistories = userService.getUserLoanHistories()

        //then
        assertThat(userLoanHistories).hasSize(1)
        assertThat(userLoanHistories[0].name).isEqualTo("A")
        assertThat(userLoanHistories[0].books).hasSize(3)
        assertThat(userLoanHistories[0].books).extracting("name").containsExactlyInAnyOrder(
            "book1", "book2", "book3"
        )
        assertThat(userLoanHistories[0].books).extracting("isReturn")
            .containsExactlyInAnyOrder(false, false, true)
    }
}