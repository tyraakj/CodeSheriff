# CodeSheriff 🕵️ - Complete Developer Guide

**AI-Powered Java Code Analysis Tool with Supabase Authentication**

A full-stack application that analyzes Java codebases using IBM watsonx.ai. Upload ZIP files containing Java source code and get intelligent insights from "Bob," an AI senior software engineer powered by Llama 3 70B.

---

## 📚 Table of Contents

1. [What CodeSheriff Does](#-what-codesheriff-does)
2. [System Architecture](#-system-architecture)
3. [Quick Start Guide](#-quick-start-guide)
4. [Backend Setup](#-backend-setup)
5. [Frontend Setup](#-frontend-setup)
6. [Supabase Authentication](#-supabase-authentication-setup)
7. [Security Considerations](#-security-considerations)
8. [API Documentation](#-api-documentation)
9. [Known Issues & Fixes](#-known-issues--fixes-applied)
10. [Troubleshooting](#-troubleshooting)
11. [Production Deployment](#-production-deployment)

---

## 🎯 What CodeSheriff Does

### User Flow
1. **Sign Up/Sign In** → Authenticate with Supabase
2. **Upload Java ZIP** → Drag and drop or select a ZIP file containing Java code
3. **Parse Structure** → JavaParser extracts all classes, methods, and metadata
4. **Select Method** → Browse the class tree and click on any method
5. **AI Analysis** → IBM watsonx.ai analyzes the method and provides insights
6. **View Results** → See what the method does, potential issues, and where to start reading

### Key Features
- ✅ **Secure Authentication** - Supabase-powered sign-up/sign-in
- ✅ **Drag & Drop Upload** - Easy ZIP file uploads (up to 100MB)
- ✅ **Smart Parsing** - Extracts classes, methods, annotations, signatures
- ✅ **AI-Powered Analysis** - IBM watsonx.ai Llama 3 70B model
- ✅ **Dark/Light Theme** - Toggle for comfortable viewing
- ✅ **No Database Required** - Stateless architecture
- ✅ **Real-time Insights** - Instant method analysis

---

## 🏗️ System Architecture

### Technology Stack

**Backend (Spring Boot 4.0.6)**
- Spring Web MVC - REST API endpoints
- Spring WebFlux - Reactive HTTP client for IBM API
- Spring Security - JWT authentication (optional)
- JavaParser 3.25.8 - Java code parsing
- Jackson - JSON processing
- Lombok - Boilerplate reduction

**Frontend (React 19 + Vite 8)**
- React 19.2.5 - UI framework
- Vite 8.0.10 - Build tool and dev server
- Supabase JS Client - Authentication
- Native Fetch API - HTTP requests
- CSS Custom Properties - Theming

**External Services**
- IBM watsonx.ai - AI code analysis
- Supabase - Authentication and user management

### Component Overview

```
CodeSheriff/
├── backend/                          # Spring Boot backend
│   ├── src/main/java/.../
│   │   ├── Controller/
│   │   │   ├── ZipController.java    # POST /api/upload - Upload ZIP
│   │   │   └── AnalyzeController.java # POST /analyze - Analyze method
│   │   ├── Service/
│   │   │   ├── JavaParserService.java # Parse Java files
│   │   │   └── BobService.java       # Call IBM watsonx.ai API
│   │   ├── Model/
│   │   │   ├── ClassInfo.java        # Java class representation
│   │   │   ├── MethodInfo.java       # Method representation
│   │   │   ├── BobAnalysis.java      # AI analysis result
│   │   │   ├── AnalyzeRequest.java   # Analysis request DTO
│   │   │   └── ApiErrorResponse.java # Error response DTO
│   │   └── Exception/
│   │       ├── AiIntegrationException.java
│   │       └── GlobalExceptionHandler.java
│   └── src/main/resources/
│       └── application.properties    # Configuration
│
└── frontend/codesheriff-ui/          # React frontend
    ├── src/
    │   ├── pages/
    │   │   ├── LandingPage.jsx       # Entry page
    │   │   ├── AuthPage.jsx          # Sign in/Sign up
    │   │   └── Dashboard.jsx         # Main workspace
    │   ├── components/
    │   │   ├── UploadZone.jsx        # File upload
    │   │   ├── ClassTree.jsx         # Class/method tree
    │   │   ├── MethodPanel.jsx       # Method details
    │   │   ├── BobInvestigation.jsx  # AI insights display
    │   │   └── ThemeToggle.jsx       # Dark/light mode
    │   ├── api/
    │   │   └── api.js                # Backend API calls
    │   ├── lib/
    │   │   └── supabase.js           # Supabase client
    │   └── theme/
    │       └── index.js              # Theme configuration
    └── .env                          # Environment variables
```

---

## 🚀 Quick Start Guide

### Prerequisites
- **Java 21+** and Maven 3.6+
- **Node.js 18+** and npm
- **IBM watsonx.ai account** with API credentials
- **Supabase account** (free tier works)

### 1. Clone and Setup

```bash
# Clone the repository
git clone <your-repo-url>
cd CodeSheriff

# Backend setup
cd backend
./mvnw clean install

# Frontend setup
cd ../frontend/codesheriff-ui
npm install
```

### 2. Configure Environment Variables

**Backend** (`backend/src/main/resources/application.properties`):
```properties
# IBM watsonx.ai credentials
ibm.api.key=${IBM_API_KEY}
ibm.api.url=${IBM_WATSONX_URL}

# File upload limits
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB
```

Set environment variables:
```bash
export IBM_API_KEY="your-ibm-api-key"
export IBM_WATSONX_URL="https://your-watsonx-url"
```

**Frontend** (`frontend/codesheriff-ui/.env`):
```env
VITE_SUPABASE_URL=https://your-project.supabase.co
VITE_SUPABASE_ANON_KEY=your-anon-key
VITE_API_BASE_URL=http://localhost:8080
```

### 3. Update Project ID

Edit `backend/src/main/java/.../Service/BobService.java` line 71:
```java
requestBody.put("project_id", "YOUR-ACTUAL-PROJECT-ID");
```

### 4. Run the Application

**Terminal 1 - Backend:**
```bash
cd backend
./mvnw spring-boot:run
```
Backend runs on `http://localhost:8080`

**Terminal 2 - Frontend:**
```bash
cd frontend/codesheriff-ui
npm run dev
```
Frontend runs on `http://localhost:5173`

### 5. Test the Application

1. Open `http://localhost:5173`
2. Click "SIGN IN" and create an account
3. Upload a Java ZIP file
4. Select a method from the tree
5. View Bob's AI analysis

---

## 🔧 Backend Setup

### Project Structure

The backend is a Spring Boot application with a clean layered architecture:

**Controllers** - Handle HTTP requests
- `ZipController` - Processes ZIP uploads
- `AnalyzeController` - Handles method analysis requests

**Services** - Business logic
- `JavaParserService` - Parses Java files using JavaParser library
- `BobService` - Communicates with IBM watsonx.ai API

**Models** - Data transfer objects
- `ClassInfo` - Represents a parsed Java class
- `MethodInfo` - Represents a parsed method
- `BobAnalysis` - AI analysis result
- `AnalyzeRequest` - Request payload for analysis
- `ApiErrorResponse` - Standardized error responses

**Exception Handling**
- `AiIntegrationException` - Custom exception for IBM API failures
- `GlobalExceptionHandler` - Centralized error handling

### Configuration Details

**application.properties:**
```properties
# IBM watsonx.ai API
ibm.api.key=${IBM_API_KEY}
ibm.api.url=${IBM_WATSONX_URL}

# File upload limits (adjust as needed)
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB
```

### Important Code Locations

**BobService.java** - IBM API Integration
- Line 27-33: `analyze()` method - Main entry point
- Line 35-63: `buildBobPrompt()` - Constructs AI prompt
- Line 65-92: `callIbmBob()` - Makes HTTP request to IBM
- Line 94-115: `parseBobResponse()` - Parses AI response

**JavaParserService.java** - Java Code Parsing
- Line 20-36: `parseAll()` - Parses multiple files
- Line 38-65: `parseOneFile()` - Parses single file
- Line 67-89: `extractMethodInfo()` - Extracts method details

**ZipController.java** - File Upload
- Line 27-38: `uploadZip()` - Handles ZIP upload
- Line 40-57: `extractJavaFilesFromZip()` - Extracts Java files

### Dependencies (pom.xml)

```xml
<!-- Core Spring Boot -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc</artifactId>
</dependency>

<!-- WebFlux for reactive HTTP client -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>

<!-- JavaParser for code analysis -->
<dependency>
    <groupId>com.github.javaparser</groupId>
    <artifactId>javaparser-core</artifactId>
    <version>3.25.8</version>
</dependency>

<!-- Jackson for JSON -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>

<!-- Lombok for cleaner code -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.30</version>
    <scope>provided</scope>
</dependency>
```

---

## 🎨 Frontend Setup

### Project Structure

**Pages:**
- `LandingPage.jsx` - Marketing page with call-to-action
- `AuthPage.jsx` - Sign in/Sign up with Supabase
- `Dashboard.jsx` - Main workspace with upload and analysis

**Components:**
- `UploadZone.jsx` - Drag-drop file upload
- `ClassTree.jsx` - Hierarchical class/method browser
- `MethodPanel.jsx` - Displays method code and analysis
- `BobInvestigation.jsx` - Shows AI insights in cards
- `ThemeToggle.jsx` - Dark/light mode switcher

**API Integration:**
- `api/api.js` - Backend API calls with JWT tokens
- `lib/supabase.js` - Supabase authentication client

### Key Files Explained

**App.jsx** - Main application component
- Manages authentication state
- Handles page routing
- Listens for auth state changes
- Provides user context to pages

**api.js** - Backend communication
```javascript
// Upload ZIP file
uploadAndParse(file)
// Returns: Array of ClassInfo objects

// Analyze method
analyzeMethod(className, methodName, methodBody, classContext)
// Returns: BobAnalysis object
```

**supabase.js** - Authentication helpers
```javascript
authService.signUp(email, password)
authService.signIn(email, password)
authService.signOut()
authService.getSession()
authService.getAccessToken() // For API calls
```

### Theme System

The app uses CSS custom properties for theming:

```javascript
// src/theme/index.js
export const DARK = {
  bg: "#0a0a0a",
  text: "#e0e0e0",
  accent: "#00d4ff",
  border: "#2a2a2a",
  // ...
};

export const LIGHT = {
  bg: "#ffffff",
  text: "#1a1a1a",
  accent: "#0066cc",
  border: "#e0e0e0",
  // ...
};
```

### Dependencies (package.json)

```json
{
  "dependencies": {
    "react": "^19.2.5",
    "react-dom": "^19.2.5",
    "@supabase/supabase-js": "^2.x.x"
  },
  "devDependencies": {
    "vite": "^8.0.10",
    "@vitejs/plugin-react": "^6.0.1"
  }
}
```

---

## 🔐 Supabase Authentication Setup

### Step 1: Create Supabase Project

1. Go to [https://app.supabase.com](https://app.supabase.com)
2. Click "New Project"
3. Fill in project details:
   - **Name**: CodeSheriff
   - **Database Password**: Generate strong password
   - **Region**: Choose closest to users
4. Wait 2-3 minutes for setup

### Step 2: Get Credentials

1. In Supabase dashboard → **Settings** → **API**
2. Copy:
   - **Project URL**: `https://xxxxx.supabase.co`
   - **anon public key**: Under "Project API keys"
   - **JWT Secret**: Under "JWT Settings" (for backend)

### Step 3: Configure Frontend

Create `frontend/codesheriff-ui/.env`:
```env
VITE_SUPABASE_URL=https://your-project-id.supabase.co
VITE_SUPABASE_ANON_KEY=your-anon-key-here
VITE_API_BASE_URL=http://localhost:8080
```

### Step 4: Enable Email Authentication

1. Supabase dashboard → **Authentication** → **Providers**
2. Enable **Email** provider
3. Configure:
   - ✅ Enable email confirmations (production)
   - ✅ Secure email change
   - ✅ Secure password change

### Step 5: Test Authentication

1. Start both backend and frontend
2. Navigate to `http://localhost:5173`
3. Click "SIGN IN"
4. Create account with email/password
5. Check email for confirmation (if enabled)
6. Sign in and access dashboard

### Step 6: Backend JWT Validation (Optional but Recommended)

For production, implement JWT validation in Spring Boot:

1. Add dependency to `pom.xml`:
```xml
<dependency>
    <groupId>com.auth0</groupId>
    <artifactId>java-jwt</artifactId>
    <version>4.4.0</version>
</dependency>
```

2. Create `SecurityConfig.java`:
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Value("${supabase.jwt.secret}")
    private String jwtSecret;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/upload", "/analyze").authenticated()
                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtAuthenticationFilter(), 
                UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtSecret);
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

3. Create `JwtAuthenticationFilter.java`:
```java
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final String jwtSecret;
    
    public JwtAuthenticationFilter(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) 
            throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            try {
                Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
                JWTVerifier verifier = JWT.require(algorithm).build();
                DecodedJWT jwt = verifier.verify(token);
                
                String userId = jwt.getSubject();
                String email = jwt.getClaim("email").asString();
                
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userId, null, Collections.emptyList()
                    );
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
            } catch (JWTVerificationException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
```

4. Update `application.properties`:
```properties
supabase.jwt.secret=${SUPABASE_JWT_SECRET}
```

5. Set environment variable:
```bash
export SUPABASE_JWT_SECRET="your-jwt-secret-from-supabase"
```

---

## 🔒 Security Considerations

### Critical Security Risks (Fixed/Addressed)

#### 1. ✅ Hardcoded Credentials - FIXED
**Before:** API keys in `application.properties`
**After:** Using environment variables
```properties
ibm.api.key=${IBM_API_KEY}
ibm.api.url=${IBM_WATSONX_URL}
```

#### 2. ✅ No Authentication - ADDRESSED
**Solution:** Supabase authentication integrated
- Frontend: JWT tokens in API requests
- Backend: Optional JWT validation filter

#### 3. ✅ JPA Dependencies Removed
**Before:** Unnecessary database dependencies
**After:** Fully stateless, no persistence layer

#### 4. ⚠️ Input Validation - NEEDS IMPLEMENTATION
**Risk:** No validation on ZIP contents
**Recommendation:**
```java
// Validate file type
if (!zipFile.getContentType().equals("application/zip")) {
    throw new InvalidFileTypeException("Only ZIP files allowed");
}

// Validate ZIP entries for path traversal
for (ZipEntry entry : entries) {
    if (entry.getName().contains("..")) {
        throw new SecurityException("Invalid file path");
    }
}
```

#### 5. ⚠️ Rate Limiting - NEEDS IMPLEMENTATION
**Risk:** API abuse, DoS attacks
**Recommendation:** Implement Bucket4j or Spring Cloud Gateway rate limiting

#### 6. ⚠️ WebClient Timeout - NEEDS CONFIGURATION
**Risk:** Hanging requests
**Recommendation:**
```java
private final WebClient webClient = WebClient.builder()
    .clientConnector(new ReactorClientHttpConnector(
        HttpClient.create()
            .responseTimeout(Duration.ofSeconds(30))
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
    ))
    .build();
```

### Security Checklist

- [x] Remove hardcoded credentials
- [x] Implement authentication (Supabase)
- [x] Remove unnecessary JPA dependencies
- [x] Add JWT token to API requests
- [ ] Add input validation for ZIP files
- [ ] Implement rate limiting
- [ ] Configure WebClient timeouts
- [ ] Add CSRF protection
- [ ] Implement audit logging
- [ ] Add error boundaries in React
- [ ] Use HTTPS in production

---

## 📡 API Documentation

### Backend Endpoints

#### 1. Upload Java ZIP
```http
POST /api/upload
Content-Type: multipart/form-data
Authorization: Bearer <jwt-token>

file: <java-zip-file>
```

**Success Response (200):**
```json
[
  {
    "className": "UserService",
    "filePath": "com/example/UserService.java",
    "annotations": ["Service", "Transactional"],
    "methods": [
      {
        "name": "createUser",
        "signature": "public User createUser(String name)",
        "returnType": "User",
        "visibility": "public",
        "lineStart": 15,
        "body": "{\n  User user = new User();\n  user.setName(name);\n  return userRepository.save(user);\n}",
        "params": "String name"
      }
    ]
  }
]
```

**Error Response (400):**
```json
{
  "timestamp": "2026-05-15T16:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "File is empty or invalid"
}
```

#### 2. Analyze Method
```http
POST /analyze
Content-Type: application/json
Authorization: Bearer <jwt-token>

{
  "className": "UserService",
  "methodName": "createUser",
  "methodBody": "{\n  User user = new User();\n  user.setName(name);\n  return userRepository.save(user);\n}",
  "allClassContext": "// other methods in class..."
}
```

**Success Response (200):**
```json
{
  "whatItDoes": "Creates a new user in the system with the provided name",
  "intentVsReality": "Method is clean and follows Single Responsibility Principle. No hidden complexity.",
  "whereToStart": "Line 3: userRepository.save(user) - This is where the user is persisted to the database",
  "hasTests": false,
  "lineCount": 4
}
```

**Error Response (502):**
```json
{
  "timestamp": "2026-05-15T16:00:00",
  "status": 502,
  "error": "AI Service Unavailable",
  "message": "Failed to communicate with IBM Bob. Please try again later."
}
```

### Frontend API Calls

**Upload ZIP:**
```javascript
import { uploadAndParse } from './api/api';

const file = document.getElementById('fileInput').files[0];
const classes = await uploadAndParse(file);
console.log(classes);
```

**Analyze Method:**
```javascript
import { analyzeMethod } from './api/api';

const analysis = await analyzeMethod(
  'UserService',
  'createUser',
  methodBody,
  classContext
);
console.log(analysis);
```

---

## 🐛 Known Issues & Fixes Applied

### Critical Bugs Fixed ✅

#### 1. Frontend API URL Mismatch
**Issue:** Upload endpoint was `/upload` instead of `/api/upload`
**Fixed in:** `frontend/codesheriff-ui/src/api/api.js` line 7
```javascript
// Before: fetch(`${BASE}/upload`)
// After:  fetch(`${BASE}/api/upload`)
```

#### 2. Inverted Success Check
**Issue:** Throwing error on successful response
**Fixed in:** `frontend/codesheriff-ui/src/api/api.js` lines 12 & 35
```javascript
// Before: if (res.ok) throw new Error(...)
// After:  if (!res.ok) throw new Error(...)
```

#### 3. Response Field Mismatch
**Issue:** Frontend expecting fields not in backend response
**Fixed in:** `frontend/codesheriff-ui/src/components/BobInvestigation.jsx`
```javascript
// Removed: whyConfusing, dependencies
// Added: lineCount, hasTests
```

### Backend Issues (Documented)

#### 1. Hardcoded Project ID
**Location:** `BobService.java` line 71
**Issue:** `project_id` is hardcoded
**Solution:** Move to configuration
```java
// Current
requestBody.put("project_id", "YOUR-ACTUAL-PROJECT-ID-HERE");

// Recommended
@Value("${ibm.project.id}")
private String projectId;
requestBody.put("project_id", projectId);
```

#### 2. Missing IBM API Parameters
**Location:** `BobService.java` line 66-69
**Issue:** Request may need `parameters` object
**Recommendation:**
```java
Map<String, Object> parameters = new HashMap<>();
parameters.put("max_new_tokens", 2000);
parameters.put("temperature", 0.7);
requestBody.put("parameters", parameters);
```

#### 3. No Timeout Configuration
**Location:** `BobService.java` line 24
**Issue:** WebClient can hang indefinitely
**Solution:** Add timeout configuration (see Security section)

#### 4. Silent Parse Failures
**Location:** `JavaParserService.java` line 32
**Issue:** Parse errors are logged but not reported to user
**Recommendation:** Collect errors and return in response

---

## 🔧 Troubleshooting

### Common Issues

#### "Upload failed: 404"
**Cause:** Backend not running or wrong URL
**Solution:**
1. Check backend is running: `http://localhost:8080`
2. Verify `.env` has correct `VITE_API_BASE_URL`
3. Check browser console for actual URL being called

#### "Analysis failed: 502"
**Cause:** IBM API communication failure
**Solution:**
1. Verify IBM API credentials in environment variables
2. Check IBM API quota/limits
3. Verify project ID is correct in `BobService.java`
4. Check backend logs for detailed error

#### "Invalid JWT" or "Unauthorized"
**Cause:** Authentication token issue
**Solution:**
1. Sign out and sign in again
2. Check Supabase credentials in `.env`
3. Verify JWT secret matches in backend (if using JWT validation)
4. Check browser console for token

#### CORS Errors
**Cause:** Backend not allowing frontend origin
**Solution:**
1. Verify `@CrossOrigin("http://localhost:5173")` in controllers
2. Check frontend is running on port 5173
3. Clear browser cache

#### "File too large"
**Cause:** ZIP exceeds 100MB limit
**Solution:**
1. Reduce ZIP size
2. Or increase limit in `application.properties`:
```properties
spring.servlet.multipart.max-file-size=200MB
spring.servlet.multipart.max-request-size=200MB
```

#### Parse Errors
**Cause:** Invalid Java syntax or unsupported features
**Solution:**
1. Ensure ZIP contains valid Java files
2. Check backend logs for specific parse errors
3. Try with simpler Java files first

### Debug Mode

**Backend:**
```properties
# application.properties
logging.level.com.backend.CodeSheriff=DEBUG
spring.jpa.show-sql=true
```

**Frontend:**
```javascript
// Enable detailed logging
console.log('Upload response:', await uploadAndParse(file));
console.log('Analysis response:', await analyzeMethod(...));
```

---

## 🚀 Production Deployment

### Backend Deployment

#### 1. Environment Variables
```bash
export IBM_API_KEY="production-key"
export IBM_WATSONX_URL="https://production-url"
export SUPABASE_JWT_SECRET="production-jwt-secret"
export ALLOWED_ORIGINS="https://your-frontend-domain.com"
```

#### 2. Build
```bash
cd backend
./mvnw clean package -DskipTests
```

#### 3. Run
```bash
java -jar target/CodeSheriff-0.0.1-SNAPSHOT.jar
```

#### 4. Deploy to Cloud
- **AWS Elastic Beanstalk**: Upload JAR
- **Heroku**: Use Heroku Maven plugin
- **Docker**: Create Dockerfile
```dockerfile
FROM openjdk:21-jdk-slim
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

### Frontend Deployment

#### 1. Update Environment
Create `.env.production`:
```env
VITE_SUPABASE_URL=https://your-project.supabase.co
VITE_SUPABASE_ANON_KEY=production-anon-key
VITE_API_BASE_URL=https://api.your-domain.com
```

#### 2. Build
```bash
cd frontend/codesheriff-ui
npm run build
```

#### 3. Deploy
- **Vercel**: `vercel --prod`
- **Netlify**: Drag `dist/` folder
- **AWS S3 + CloudFront**: Upload `dist/` contents

### Production Checklist

- [ ] All credentials in environment variables
- [ ] HTTPS enabled on both frontend and backend
- [ ] JWT validation implemented in backend
- [ ] Rate limiting configured
- [ ] Input validation added
- [ ] Error logging and monitoring setup
- [ ] CORS configured for production domain
- [ ] Database backups (if using persistence)
- [ ] CDN configured for frontend assets
- [ ] Health check endpoints added
- [ ] Load balancing configured (if needed)

---

## 📚 Additional Resources

### Documentation
- [Spring Boot Docs](https://spring.io/projects/spring-boot)
- [React Docs](https://react.dev/)
- [Vite Docs](https://vitejs.dev/)
- [Supabase Docs](https://supabase.com/docs)
- [IBM watsonx.ai Docs](https://www.ibm.com/products/watsonx-ai)
- [JavaParser Docs](https://javaparser.org/)

### Tutorials
- [Spring Security JWT](https://www.baeldung.com/spring-security-oauth-jwt)
- [React Authentication](https://supabase.com/docs/guides/auth/auth-helpers/react)
- [Vite Environment Variables](https://vitejs.dev/guide/env-and-mode.html)

### Tools
- [JWT.io](https://jwt.io/) - Decode and verify JWTs
- [Postman](https://www.postman.com/) - API testing
- [React DevTools](https://react.dev/learn/react-developer-tools)

---

## 🤝 Contributing

### For Junior Developers

1. **Start with the frontend** - It's easier to understand
2. **Read the code comments** - They explain what each part does
3. **Test locally first** - Always test changes before committing
4. **Ask questions** - No question is too simple
5. **Follow the patterns** - Look at existing code for examples

### Code Style

**Java:**
- Use Lombok annotations (`@Data`, `@Builder`)
- Follow Spring Boot conventions
- Add JavaDoc for public methods
- Use meaningful variable names

**JavaScript/React:**
- Use functional components with hooks
- Keep components small and focused
- Use descriptive prop names
- Add comments for complex logic

### Git Workflow

```bash
# Create feature branch
git checkout -b feature/your-feature-name

# Make changes and commit
git add .
git commit -m "feat: add your feature description"

# Push and create PR
git push origin feature/your-feature-name
```

---

## 📝 License

This project is part of the IBM CodeSheriff initiative.

---

## 🎉 Success!

You now have a complete understanding of CodeSheriff! 

**Next Steps:**
1. Set up your development environment
2. Configure Supabase authentication
3. Test with sample Java projects
4. Customize the AI prompts for your needs
5. Deploy to production

**Need Help?**
- Check the Troubleshooting section
- Review the API documentation
- Look at code comments
- Ask your team lead

---

**Built with ❤️ by developers, for developers**

*Last Updated: 2026-05-15*