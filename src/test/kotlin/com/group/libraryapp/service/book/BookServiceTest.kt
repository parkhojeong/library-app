package com.group.libraryapp.service.book

import com.group.libraryapp.domain.book.Book
import com.group.libraryapp.domain.book.BookRepository
import com.group.libraryapp.domain.book.BookType
import com.group.libraryapp.domain.user.User
import com.group.libraryapp.domain.user.UserRepository
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistory
import com.group.libraryapp.domain.user.loanhistory.UserLoanHistoryRepository
import com.group.libraryapp.dto.book.request.BookLoanRequest
import com.group.libraryapp.dto.book.request.BookRequest
import com.group.libraryapp.dto.book.request.BookReturnRequest
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class BookServiceTest @Autowired constructor(
    private val bookService: BookService,
    private val bookRepository: BookRepository,
    private val userRepository: UserRepository,
    private val userLonaHistoryRepository: UserLoanHistoryRepository,
) {
    @AfterEach
    fun cleanUp() {
        bookRepository.deleteAll()
        userRepository.deleteAll()
    }


    @Test
    fun saveBookTest() {
        //given
        val request = BookRequest("book", BookType.COMPUTER)

        //when
        bookService.saveBook(request)

        //then
        val books = bookRepository.findAll()
        assertThat(books).hasSize(1)
        assertThat(books[0].name).isEqualTo(request.name)
        assertThat(books[0].type).isEqualTo(request.type)
    }

    @Test
    fun loanBookTest() {
        //given
        bookRepository.save(Book.fixture("book"))
        val savedUser = userRepository.save(User("user", null))
        val request = BookLoanRequest("user", "book")

        //when
        bookService.loanBook(request)

        //then
        val userLoanHistories = userLonaHistoryRepository.findAll()
        assertThat(userLoanHistories).hasSize(1)
        assertThat(userLoanHistories[0].bookName).isEqualTo(request.bookName)
        assertThat(userLoanHistories[0].user.id).isEqualTo(savedUser.id)
        assertThat(userLoanHistories[0].isReturn).isFalse()
    }

    @Test
    fun loanBookFailTest() {
        //given
        bookRepository.save(Book.fixture("book"))
        val savedUser = userRepository.save(User("user", null))
        userLonaHistoryRepository.save(UserLoanHistory(savedUser, "book", false))
        val request = BookLoanRequest("user", "book")

        //when & then
        val message = assertThrows<IllegalArgumentException> {
            bookService.loanBook(request)
        }.message
        assertThat(message).isEqualTo("진작 대출되어 있는 책입니다")

    }

    @Test
    fun returnBookTest() {
        //given
        bookRepository.save(Book.fixture("book"))
        val savedUser = userRepository.save(User("user", null))
        userLonaHistoryRepository.save(UserLoanHistory(savedUser, "book", false))
        val request = BookReturnRequest("user", "book")

        //when
        bookService.returnBook(request)

        //then
        val userLoanHistories = userLonaHistoryRepository.findAll()
        assertThat(userLoanHistories).hasSize(1)
        assertThat(userLoanHistories[0].isReturn).isTrue()
    }
}