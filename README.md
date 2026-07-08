# AI-Powered Lead Qualification System (Enterprise Suite)

An intelligent B2B Sales Lead Qualification System built with Java 21, Spring Boot, MongoDB, and Google Gemini AI. The application evaluates incoming sales prospects, runs structured B2B prompt rules using Gemini, scores them on a 100-mark scale, categorizes them as Hot, Warm, or Cold, and presents them in a responsive, glassmorphic analytics dashboard.

---

## 1. Project Overview & Problem Statement

### Problem Statement
In B2B sales, teams are often overwhelmed with high volumes of inbound leads. Identifying high-value prospects (enterprise clients with matching budgets and timelines) manually is labor-intensive, error-prone, and slow. This delays response times, decreases conversion rates, and wastes sales executive hours on unqualified contacts.

### Solution
The **Lead Qualification Agent** automates B2B lead triage. By utilizing **Google Gemini AI**, the system instantly evaluates firmographics and requirement descriptions against professional qualification rules, assigning scores, categories, clear logic explanations, and recommendations. Sales executives can triage the pipeline instantly from an analytical KPI dashboard.

---

## 2. System Objectives
1. **Automated Triage**: Perform AI-driven lead scoring within seconds of submission.
2. **Standardized Rules**: Apply uniform scoring rules (Company Size, Budget Conversion to INR, Job Roles, Requirements Clarity, and Timelines).
3. **Data Integrity**: Persist leads in MongoDB Atlas with complete state-management tracking (`PENDING`, `SUCCESS`, `FAILED`).
4. **Rich Analytics**: Offer pipeline visualizations (Category distribution doughnut charts, monthly volumes, score trends) and data export triggers.
5. **Production Ready Deployment**: Fully dockerized environment supporting local runs via a single command.

---

## 3. Technology Stack

### Backend Architecture
- **Java 21** & **Spring Boot 3.x**
- **Spring Data MongoDB** (Database Persistence)
- **Spring RestTemplate** (Timeout-guarded HTTP request engine connecting to Gemini AI Studio)
- **Jakarta Validation** (Input sanitation)

### Frontend Dashboard
- **HTML5** & **Vanilla CSS3** (Custom glassmorphic dark theme)
- **Vanilla JavaScript** (ES6 Fetch, filters, and CSV generator)
- **Chart.js** (CDN loaded graphics engine)

### Deployment & DevOps
- **Docker** (Multi-stage build)
- **Docker Compose** (Multi-service manager: MongoDB, Backend App, Frontend Nginx Web Server)

---

## 4. System Architecture
```
  Sales Executive
        │
        ▼
   Frontend UI (Nginx :8000) ───[REST Calls]───► Spring Boot Backend (:8080)
                                                        │
                                            ┌───────────┴───────────┐
                                            ▼                       ▼
                                   MongoDB Atlas/Local      Google Gemini AI Studio
                                     (Port :27017)           (content generation)
```

---

## 5. Folder Structure
```
LeadQualificationAgent/
│
├── Dockerfile                          # Docker deployment descriptor
├── pom.xml                             # Maven build descriptor
├── src/                                # Spring Boot Source code
│   └── main/
│       ├── java/com/sales/leadqualifier/
│       │   ├── LeadQualificationAgentApplication.java
│       │   ├── config/             # CorsConfig, GeminiConfig
│       │   ├── controller/         # LeadController, DashboardController
│       │   ├── dto/                # DTO models (Request, Response, Dashboard Summary)
│       │   ├── exception/          # Global exception handlers
│       │   ├── model/              # Lead Document Entity
│       │   ├── repository/         # LeadRepository interface
│       │   ├── service/            # GeminiService, LeadQualificationService, DashboardService
│       │   └── util/               # Validation helpers
│       └── resources/
│           └── application.properties
│
├── frontend/
│   ├── index.html                      # Homepage Hero
│   ├── lead-form.html                  # Lead profile submission (Create/Edit)
│   ├── history.html                    # Pipeline logs and modal audits
│   ├── result.html                     # Scoring breakdown dial gauge reports
│   ├── dashboard.html                  # [New] Analytics charts and filters
│   ├── css/
│   │   ├── style.css                   # Global styles
│   │   └── dashboard.css               # [New] Grid cards, print layouts
│   └── js/
│       ├── home.js
│       ├── form.js                     # CRUD forms logic
│       ├── history.js                  # Table logs loader
│       ├── result.js                   # Speedometer gauges visualizer
│       └── dashboard.js                # [New] Chart.js builder, search filters, and exports
│
├── docker-compose.yml                  # Link service containers
└── README.md                           # Documentation (This file)
```

---

## 6. Database Design (MongoDB)
Data is saved in the `leads` collection under the following document schema:
```json
{
  "_id": "648c1a9b2b0f4d38c11e74f1",
  "leadName": "Alice Johnson",
  "companyName": "Acme Tech Corp",
  "industry": "Technology",
  "jobRole": "VP of Engineering",
  "companySize": 1200,
  "annualRevenue": 15000000.0,
  "budget": 35000.0,
  "timeline": "Immediate (Within 30 Days)",
  "requirement": "Require enterprise cloud-native orchestration support.",
  "email": "alice@acme.com",
  "phoneNumber": "+1-555-0199",
  "createdAt": "2026-07-04T12:00:00Z",
  "leadScore": 95,
  "category": "Hot Lead",
  "reason": "Enterprise size company with matching budget and VP decision maker.",
  "recommendation": "Assign to senior Account Executive immediately for onboarding call.",
  "analysisStatus": "SUCCESS",
  "analysisTimestamp": "2026-07-04T12:00:05Z"
}
```

---

## 7. REST API Documentation

### Lead Management Endpoints (`/api/leads`)

| Method | Endpoint | Description | Status Code |
|:---|:---|:---|:---|
| **POST** | `/api/leads/analyze` | Saves lead profile, triggers AI evaluation, returns immediate result | `200 OK` / `400 Bad Request` |
| **GET** | `/api/leads` | Lists all leads. Supports query: `?sort=newest` or `?sort=oldest` | `200 OK` |
| **GET** | `/api/leads/{id}` | Fetches a single lead record details by ID | `200 OK` / `404 Not Found` |
| **PUT** | `/api/leads/{id}` | Updates details of a lead and re-runs Gemini AI evaluation | `200 OK` / `400 Bad Request` |
| **DELETE** | `/api/leads/{id}` | Removes a lead document from the database | `200 OK` / `404 Not Found` |
| **GET** | `/api/leads/search` | Case-insensitive substring search by company name: `?company=Acme` | `200 OK` |
| **POST** | `/api/leads/{id}/retry` | Manually triggers Gemini AI analysis on a lead (useful for status `FAILED`) | `200 OK` / `404 Not Found` |

### Dashboard Analytics Endpoints (`/api/dashboard`)

| Method | Endpoint | Description | Response Model |
|:---|:---|:---|:---|
| **GET** | `/api/dashboard/summary` | Retrieves overall metrics (counts, averages, success rates) | `{"totalLeads":120, "hotLeads":40, ...}` |
| **GET** | `/api/dashboard/category-distribution` | Retrieves grouped category counts (Hot, Warm, Cold) | `{"Hot": 40, "Warm": 45, "Cold": 35}` |
| **GET** | `/api/dashboard/monthly-analysis` | Chronological lead counts grouped by month | `{"Jul 2026": 12, "Aug 2026": 24}` |
| **GET** | `/api/dashboard/monthly-score-trend` | Chronological average score trend by month | `{"Jul 2026": 82.5, "Aug 2026": 78.2}` |

---

## 8. Environmental Setup & Running Configurations

### Google Gemini API Setup
1. Visit [Google AI Studio](https://aistudio.google.com/).
2. Click **Create API Key**.
3. Save the generated key. Export it into your host system environment:
   - **Linux/macOS**: `export GEMINI_API_KEY="your_api_key"`
   - **Windows PowerShell**: `$env:GEMINI_API_KEY="your_api_key"`
   - **Windows CMD**: `set GEMINI_API_KEY="your_api_key"`

---

## 9. Running Locally

### Prerequisites
- Java 21 Installed
- Maven 3.8+ Installed
- Local MongoDB running on `mongodb://localhost:27017`

### Steps
1. Launch the Spring Boot service from the root:
   ```bash
   mvn spring-boot:run
   ```
2. Run a local static file server inside the frontend folder:
   ```bash
   cd frontend
   # Using Python:
   python -m http.server 8000
   # Or using Node:
   npx http-server -p 8000
   ```
3. Open `http://localhost:8000/index.html` in your browser.

---

## 10. Docker Setup (Recommended)

To launch the database, Spring Boot app, and Nginx server together in isolated containers, make sure Docker and Docker Compose are installed:

1. Define `GEMINI_API_KEY` in your environment.
2. In the root directory, spin up the entire cluster:
   ```bash
   docker-compose up --build
   ```
3. Access the interfaces:
   - **Frontend UI Dashboard**: `http://localhost:8000`
   - **Backend Service API**: `http://localhost:8080`
   - **MongoDB Service**: `http://localhost:27017`

To shut down:
```bash
docker-compose down -v
```

---

## 11. Screenshots Placeholder
- *Homepage Interface (index.html)*: Responsive hero page detailing pipeline elevation.
- *Lead Evaluation Form (lead-form.html)*: Standard parameters input panel with reactive form validations.
- *Speedometer scoring gauge (result.html)*: Dynamic HTML canvas gauges visualizing raw points out of 100.
- *Analytics Dashboard Layout (dashboard.html)*: Rich KPI panels, Doughnut graphs, Volume bars, and Trend lines.

---

## 12. Future Enhancements
1. **Salesforce/HubSpot CRM Sync**: Automatic sync when a lead is classified as a "Hot Lead".
2. **OAuth2 Integration**: Secure sales team access using Spring Security.
3. **Advanced LLM Models**: Support fallback routes between Gemini Flash and Gemini Pro.
