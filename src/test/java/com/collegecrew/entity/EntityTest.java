package com.collegecrew.entity;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EntityTest {

    @Test
    void testCollegeEntityCreation() {
        College college = College.builder()
                .name("Test University")
                .build();
        
        assertNotNull(college);
        assertEquals("Test University", college.getName());
    }

    @Test
    void testUserEntityCreation() {
        College college = College.builder()
                .name("Test University")
                .build();
        
        User user = User.builder()
                .email("test@test.com")
                .passwordHash("hashedpassword")
                .alias("testuser")
                .college(college)
                .build();
        
        assertNotNull(user);
        assertEquals("test@test.com", user.getEmail());
        assertEquals("testuser", user.getAlias());
        assertEquals(college, user.getCollege());
    }

    @Test
    void testJobEntityCreation() {
        College college = College.builder()
                .name("Test University")
                .build();
        
        User poster = User.builder()
                .email("poster@test.com")
                .passwordHash("hashedpassword")
                .alias("poster")
                .college(college)
                .build();
        
        Job job = Job.builder()
                .title("Test Job")
                .description("Test job description")
                .budget(new BigDecimal("100.00"))
                .poster(poster)
                .college(college)
                .build();
        
        assertNotNull(job);
        assertEquals("Test Job", job.getTitle());
        assertEquals(new BigDecimal("100.00"), job.getBudget());
        assertEquals("OPEN", job.getStatus());
        assertEquals(poster, job.getPoster());
        assertEquals(college, job.getCollege());
    }

    @Test
    void testBidEntityCreation() {
        College college = College.builder()
                .name("Test University")
                .build();
        
        User poster = User.builder()
                .email("poster@test.com")
                .passwordHash("hashedpassword")
                .alias("poster")
                .college(college)
                .build();
        
        User bidder = User.builder()
                .email("bidder@test.com")
                .passwordHash("hashedpassword")
                .alias("bidder")
                .college(college)
                .build();
        
        Job job = Job.builder()
                .title("Test Job")
                .description("Test job description")
                .budget(new BigDecimal("100.00"))
                .poster(poster)
                .college(college)
                .build();
        
        Bid bid = Bid.builder()
                .job(job)
                .bidder(bidder)
                .amount(new BigDecimal("80.00"))
                .proposal("I can do this job")
                .build();
        
        assertNotNull(bid);
        assertEquals(new BigDecimal("80.00"), bid.getAmount());
        assertEquals("PENDING", bid.getStatus());
        assertEquals(job, bid.getJob());
        assertEquals(bidder, bid.getBidder());
        assertNotNull(bid.getCreatedAt());
    }

    @Test
    void testTransactionEntityCreation() {
        College college = College.builder()
                .name("Test University")
                .build();
        
        User payer = User.builder()
                .email("payer@test.com")
                .passwordHash("hashedpassword")
                .alias("payer")
                .college(college)
                .build();
        
        User payee = User.builder()
                .email("payee@test.com")
                .passwordHash("hashedpassword")
                .alias("payee")
                .college(college)
                .build();
        
        Job job = Job.builder()
                .title("Test Job")
                .description("Test job description")
                .budget(new BigDecimal("100.00"))
                .poster(payer)
                .college(college)
                .build();
        
        Transaction transaction = Transaction.builder()
                .job(job)
                .payer(payer)
                .payee(payee)
                .amount(new BigDecimal("100.00"))
                .status("COMPLETED")
                .transactionType("PAYMENT")
                .build();
        
        assertNotNull(transaction);
        assertEquals(new BigDecimal("100.00"), transaction.getAmount());
        assertEquals("COMPLETED", transaction.getStatus());
        assertEquals("PAYMENT", transaction.getTransactionType());
        assertEquals(job, transaction.getJob());
        assertEquals(payer, transaction.getPayer());
        assertEquals(payee, transaction.getPayee());
        assertNotNull(transaction.getCreatedAt());
    }
}