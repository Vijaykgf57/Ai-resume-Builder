# AI-Based Smart Resume Analyzer & Job Matching System

## Tech Stack
- Java 17, Spring Boot 3.2.5
- Spring Security + JWT
- Spring Data JPA + PostgreSQL
- Apache Tika (text extraction from PDF/DOCX)
- TF-IDF Cosine Similarity (text matching)
- Synonym Dictionary (intelligent skill matching)

## Setup

### 1. Database
```sql
CREATE DATABASE resume_analyzer;
```

### 2. Configure
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.username=your_user
spring.datasource.password=your_password
```

### 3. Run
```bash
mvn spring-boot:run
```

---

## API Reference

### Auth
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login, get JWT token |

### Resume
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/resume/upload` | Upload resume (PDF/DOCX/TXT) |
| GET | `/api/resume` | Get my resumes + extracted skills |

### Jobs
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/jobs` | Create job with skills |
| GET | `/api/jobs` | List all jobs |

### Matching
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/matches/{resumeId}` | Run AI matching for a resume |
| GET | `/api/matches` | Get my match history |

---

## Example Flow

### 1. Register
```json
POST /api/auth/register
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "secret123"
}
```

### 2. Create a Job
```json
POST /api/jobs
Authorization: Bearer <token>

{
  "title": "Java Developer",
  "description": "We need a Java developer with Spring and REST API experience.",
  "skills": [
    { "skillName": "Java",     "weight": "MANDATORY" },
    { "skillName": "Spring",   "weight": "MANDATORY" },
    { "skillName": "REST API", "weight": "OPTIONAL"  }
  ]
}
```

### 3. Upload Resume
```
POST /api/resume/upload
Authorization: Bearer <token>
Content-Type: multipart/form-data

file: resume.pdf
```

### 4. Get Match Scores
```
POST /api/matches/1
Authorization: Bearer <token>
```

### Sample Response
```json
{
  "resumeId": 1,
  "resumeFileName": "john_resume.pdf",
  "matches": [
    {
      "jobId": 1,
      "jobTitle": "Java Developer",
      "skillScore": 85.71,
      "textSimilarityScore": 72.30,
      "finalScore": 81.69,
      "matchLabel": "Excellent"
    },
    {
      "jobId": 2,
      "jobTitle": "Backend Engineer",
      "skillScore": 57.14,
      "textSimilarityScore": 48.20,
      "finalScore": 54.46,
      "matchLabel": "Good"
    }
  ]
}
```

---

## AI Logic

### Synonym Matching
`Spring Boot` → resolves to `Spring` → matches job requiring `Spring`  
`REST` / `RESTful` / `Web Services` → all resolve to `REST API`

### Weighted Scoring
```
Score = (Σ weight of matched skills / Σ total weight) × 100

MANDATORY skill weight = 3
OPTIONAL  skill weight = 1
```

### Text Similarity (TF-IDF Cosine)
Compares full resume text against job description.  
Captures context beyond just skill keywords.

### Final Score
```
Final = (Skill Score × 0.70) + (Text Similarity × 0.30)
```

### Match Labels
| Score | Label |
|-------|-------|
| ≥ 80  | Excellent |
| ≥ 60  | Good |
| ≥ 40  | Fair |
| < 40  | Low |
