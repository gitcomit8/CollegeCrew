# CollegeCrew Application Testing Guide

This guide explains how to run the CollegeCrew application and test all implemented features after merging PR #7.

## Prerequisites

Before running the application, ensure you have:
- **Java 17** or higher installed
- **Maven 3.6+** installed
- **PostgreSQL** database running (see Database Setup below)

## Database Setup

The application requires a PostgreSQL database. Set up the database:

1. **Install PostgreSQL** (if not already installed)
2. **Create the database and user**:
   ```sql
   CREATE DATABASE collegecrew;
   CREATE USER collegecrew WITH PASSWORD 'password';
   GRANT ALL PRIVILEGES ON DATABASE collegecrew TO collegecrew;
   ```

### Alternative: Use H2 for Testing

To quickly test without PostgreSQL, temporarily modify `src/main/resources/application.properties`:

```properties
# Comment out PostgreSQL config and add H2:
# spring.datasource.url=jdbc:postgresql://localhost:5432/collegecrew
# spring.datasource.username=collegecrew  
# spring.datasource.password=password
# spring.datasource.driver-class-name=org.postgresql.Driver

# H2 Database (for testing)
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

## Running the Application

### 1. Build and Start the Application

```bash
# Navigate to the project directory
cd /path/to/CollegeCrew

# Clean and compile
mvn clean compile

# Run the application
mvn spring-boot:run
```

The application will start on **http://localhost:8080**

### 2. Verify Application is Running

Test the health endpoint:
```bash
curl http://localhost:8080/api/health
```

**Expected Response:** `CollegeCrew Backend is running!`

## Testing Implemented Features

### Current Feature Set

After merging PR #7, the following features are available:

1. **Health Check Endpoint** - Basic application status
2. **User Registration** - Create new user accounts with automatic college assignment
3. **User Authentication** - Login with JWT token generation
4. **College Management** - Automatic college creation based on email domains
5. **JWT Security** - Token-based authentication system

### Feature Testing Guide

#### 1. Health Check Test

```bash
curl -X GET http://localhost:8080/api/health
```

**Expected:** `CollegeCrew Backend is running!`

#### 2. User Registration Test

Register a new user:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@university.edu",
    "password": "password123", 
    "alias": "johndoe"
  }'
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsImFsaWFzIjoiam9obmRvZSIsImNvbGxlZ2VJZCI6MSwic3ViIjoiam9obkB1bml2ZXJzaXR5LmVkdSIsImlhdCI6MTYwMDAwMDAwMCwiZXhwIjoxNjAwMDg2NDAwfQ...",
  "userId": 1,
  "email": "john@university.edu",
  "alias": "johndoe", 
  "collegeId": 1
}
```

**What this tests:**
- Email domain extraction ("university.edu")
- Automatic college creation
- Password hashing
- User creation
- JWT token generation

#### 3. Test Duplicate Registration

Try registering the same email again:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@university.edu",
    "password": "differentpass",
    "alias": "differentalias"
  }'
```

**Expected:** HTTP 400 Bad Request (email already exists)

#### 4. Register User from Different College

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@mit.edu",
    "password": "securepass", 
    "alias": "alice_mit"
  }'
```

**Expected:** Success with different `collegeId` (demonstrating multi-college support)

#### 5. User Login Test

Login with the registered user:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@university.edu",
    "password": "password123"
  }'
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 1,
  "email": "john@university.edu",
  "alias": "johndoe",
  "collegeId": 1
}
```

#### 6. Test Invalid Login

Try login with wrong password:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@university.edu",
    "password": "wrongpassword"
  }'
```

**Expected:** HTTP 400 Bad Request

#### 7. Test Non-existent User Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "nonexistent@university.edu",
    "password": "password123"
  }'
```

**Expected:** HTTP 400 Bad Request

## Advanced Testing

### JWT Token Verification

You can decode the JWT token at [jwt.io](https://jwt.io) to verify it contains:
- `userId`: User's database ID
- `alias`: User's alias
- `collegeId`: Associated college ID
- `sub`: User's email (subject)
- `iat`: Issued at timestamp
- `exp`: Expiration timestamp

### Testing with Different Tools

#### Using Postman
1. Import the endpoints as a collection
2. Set `Content-Type: application/json` headers
3. Use the JSON request bodies from examples above

#### Using Browser (for GET endpoints)
- Visit: http://localhost:8080/api/health

#### Using HTTPie (if installed)
```bash
# Register
http POST localhost:8080/api/auth/register email=test@college.edu password=pass123 alias=testuser

# Login  
http POST localhost:8080/api/auth/login email=test@college.edu password=pass123
```

## Running Tests

Run the automated test suite:

```bash
# Run all tests
mvn test

# Run only AuthController tests
mvn test -Dtest=AuthControllerTest

# Run with verbose output
mvn test -Dtest=AuthControllerTest -X
```

**Expected:** All tests should pass (30 total tests, including 7 new authentication tests)

## Troubleshooting

### Common Issues

1. **Port already in use**: Change `server.port` in `application.properties`
2. **Database connection issues**: Verify PostgreSQL is running and credentials are correct
3. **JWT secret too short**: Default secret should work, but ensure it's at least 32 characters
4. **Build failures**: Run `mvn clean compile` first

### Logs

Check application logs for detailed error information:
```bash
# With more verbose logging
mvn spring-boot:run -Dspring.profiles.active=debug
```

## What's Implemented vs. Future Features

### ✅ Currently Implemented (PR #7)
- User registration with email/password/alias
- Automatic college creation from email domains
- User authentication with JWT tokens
- Password hashing with BCrypt
- Basic error handling
- Comprehensive test suite

### 🔄 Future Features (Not Yet Implemented)
- Job posting and management
- Bidding system
- Transaction processing
- User profile management
- File uploads
- Real-time notifications
- Admin panel

This completes the current feature set after merging PR #7. The authentication system provides the foundation for all future user-related features in the CollegeCrew application.