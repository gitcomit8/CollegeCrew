# AuthController API Documentation

## Authentication Endpoints

### Register User
**POST** `/api/auth/register`

Creates a new user account by extracting the email domain to find or create a college record.

**Request Body:**
```json
{
  "email": "student@university.edu",
  "password": "password123",
  "alias": "student_alias"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 1,
  "email": "student@university.edu", 
  "alias": "student_alias",
  "collegeId": 1
}
```

**Error Response (400 Bad Request):**
- Email already registered
- Invalid email format

---

### Login User  
**POST** `/api/auth/login`

Authenticates a user and returns a JWT token upon success.

**Request Body:**
```json
{
  "email": "student@university.edu",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userId": 1,
  "email": "student@university.edu",
  "alias": "student_alias", 
  "collegeId": 1
}
```

**Error Response (400 Bad Request):**
- Invalid email or password

## Implementation Details

- **Password Security**: Passwords are hashed using BCrypt
- **College Management**: Email domains automatically create college records (e.g., "university.edu" from "student@university.edu")
- **JWT Token**: Contains userId, email, alias, and collegeId claims
- **Security**: Endpoints are publicly accessible as configured in SecurityConfig