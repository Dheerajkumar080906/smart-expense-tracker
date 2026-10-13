# Smart Expense Tracker & Financial Planner

A modern full-stack personal finance application built with **React (Vite)**, **Spring Boot 3**, **Spring JDBC**, and support for **H2** or **PostgreSQL**.

---

## 🚀 Quick Start (Local Development)

### Prerequisites
- **Java 17+**
- **Node.js 18+** & npm
- Maven 3.9+ (or use the included `./mvnw`)

### 1. Run the Backend
```bash
cd backend
./mvnw spring-boot:run
```
*(On Windows PowerShell/CMD: `mvnw.cmd spring-boot:run` or `mvn spring-boot:run`)*

- Backend API: `http://localhost:8080/api`
- Health Check: `http://localhost:8080/api/health`
- Spring Actuator: `http://localhost:8080/actuator/health`

### 2. Run the Frontend
```bash
cd frontend
npm install
npm run dev
```
- Frontend UI: `http://localhost:5173`

---

## 🐳 Docker Deployment (Zero-Config)

Run the entire application (Backend + Frontend) in isolated containers:

```bash
docker compose up --build
```

- **Frontend**: http://localhost
- **Backend API**: http://localhost:8080/api

---

## 🌐 Cloud Deployment Options (Beginner Friendly)

### Option A: Render (Free & Automated via `render.yaml`)
1. Push your repository to GitHub.
2. Sign in to [Render](https://render.com).
3. Click **New +** -> **Blueprint**.
4. Select your repository. Render reads [`render.yaml`](./render.yaml) and automatically configures:
   - **Backend**: Spring Boot Docker Web Service (with auto-assigned `$PORT`).
   - **Frontend**: Static Site with automatic SPA rewrite and configured `VITE_API_URL`.

### Option B: Split Deployment (Vercel + Render / Railway)
- **Frontend on Vercel / Netlify**:
  - Root directory: `frontend`
  - Build command: `npm run build`
  - Output directory: `dist`
  - Environment Variable: `VITE_API_URL=https://your-backend.onrender.com/api`
- **Backend on Render / Railway**:
  - Root directory: `backend`
  - Build command: `./mvnw clean package -DskipTests`
  - Start command: `java -jar target/smart-expense-tracker-1.0.0.jar`
  - Environment Variable: `CORS_ALLOWED_ORIGINS=https://your-frontend.vercel.app`

---

## ⚙️ Environment Variables

### Backend (`application.properties`)
| Variable | Default | Purpose |
|---|---|---|
| `PORT` | `8080` | Port for the embedded Tomcat server |
| `SPRING_DATASOURCE_URL` | `jdbc:h2:file:./data/expense_tracker...` | Database JDBC URL (supports H2 or PostgreSQL) |
| `SPRING_DATASOURCE_USERNAME` | `sa` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | *(empty)* | Database password |
| `CORS_ALLOWED_ORIGINS` | `*` | Allowed CORS origins (e.g. `https://my-app.vercel.app`) |
| `SPRING_H2_CONSOLE_ENABLED` | `false` | Enable/disable `/h2-console` |

### Frontend
| Variable | Default | Purpose |
|---|---|---|
| `VITE_API_URL` | `http://localhost:8080/api` (dev) / `/api` (prod) | Base URL of the backend REST API |

---

## 🔌 API Endpoints
- `GET /api/health` - Health check status
- `GET /api/dashboard` - Monthly summary & budget calculations
- `GET /api/transactions` - List all transactions
- `POST /api/transactions` - Add transaction
- `DELETE /api/transactions/{id}` - Delete transaction
- `GET /api/budgets` - List category budgets
- `POST /api/budgets` - Set/update category budget limit
- `GET /api/goals` - List savings goals
- `POST /api/goals` - Create savings goal
- `PATCH /api/goals/{id}/contribute?amount=500` - Add savings contribution
