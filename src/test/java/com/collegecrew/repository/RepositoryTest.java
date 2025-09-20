package com.collegecrew.repository;

import com.collegecrew.entity.College;
import com.collegecrew.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class RepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CollegeRepository collegeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void testCollegeRepositoryFindByName() {
        // Given
        College college = College.builder()
                .name("Test University")
                .build();
        entityManager.persistAndFlush(college);

        // When
        Optional<College> found = collegeRepository.findByName("Test University");

        // Then
        assertTrue(found.isPresent());
        assertEquals("Test University", found.get().getName());
    }

    @Test
    void testUserRepositoryFindByEmail() {
        // Given
        College college = College.builder()
                .name("Test University")
                .build();
        entityManager.persistAndFlush(college);

        User user = User.builder()
                .email("test@test.com")
                .passwordHash("hashedpassword")
                .alias("testuser")
                .college(college)
                .build();
        entityManager.persistAndFlush(user);

        // When
        Optional<User> found = userRepository.findByEmail("test@test.com");

        // Then
        assertTrue(found.isPresent());
        assertEquals("test@test.com", found.get().getEmail());
        assertEquals("testuser", found.get().getAlias());
    }

    @Test
    void testAllRepositoriesAutowired() {
        // Verify all repositories are properly autowired
        assertNotNull(collegeRepository);
        assertNotNull(userRepository);
        assertNotNull(jobRepository);
        assertNotNull(bidRepository);
        assertNotNull(transactionRepository);
    }
}