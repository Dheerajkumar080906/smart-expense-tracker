# Smart Expense Tracker & Financial Planner

A full-stack starter project using React (Vite), Spring Boot, Spring JDBC, and H2 database.

## Features
- Add, view, and delete income/expense transactions
- Set monthly category budget caps
- Dashboard totals and category spending
- Budget over-limit warnings
- Savings goals ("Piggy Bank") with progress tracking
- REST API with JDBC-based persistence
- H2 file database for local persistence

## Requirements
- Java 17+
- Maven 3.9+
- Node.js 18+

## Run the backend
```bash
cd backend
mvn spring-boot:run
```
Backend: http://localhost:8080  
H2 console: http://localhost:8080/h2-console  
JDBC URL: `jdbc:h2:file:./data/expense_tracker`  
Username: `sa`, password: (blank)

## Run the frontend
In another terminal:
```bash
cd frontend
npm install
npm run dev
```
Frontend: http://localhost:5173

## API endpoints
- `GET /api/dashboard`
- `GET /api/transactions`
- `POST /api/transactions`
- `DELETE /api/transactions/{id}`
- `GET /api/budgets`
- `POST /api/budgets`
- `GET /api/goals`
- `POST /api/goals`
- `PATCH /api/goals/{id}/contribute?amount=500`

## Notes
This is an educational starter project, not production-ready financial software. It has no authentication, and the default H2 database is intended for local development. Add authentication, validation, authorization, and database backups before real use.
