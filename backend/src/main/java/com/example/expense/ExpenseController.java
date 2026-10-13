package com.example.expense;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class ExpenseController {
    private final JdbcTemplate jdbc;
    public ExpenseController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @GetMapping("/transactions")
    public List<Map<String,Object>> transactions() {
        return jdbc.queryForList("SELECT id,title,amount,type,category,txn_date AS date FROM transactions ORDER BY txn_date DESC,id DESC");
    }

    @PostMapping("/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> addTransaction(@RequestBody TxnRequest r) {
        if (r.title == null || r.title.isBlank() || r.category == null || r.category.isBlank()
            || r.amount == null || r.amount.signum() <= 0 || !List.of("INCOME","EXPENSE").contains(r.type)
            || r.date == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide title, positive amount, type, category and date.");
        org.springframework.jdbc.support.GeneratedKeyHolder keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
        jdbc.update(connection -> {
            var ps = connection.prepareStatement(
                "INSERT INTO transactions(title,amount,type,category,txn_date) VALUES (?,?,?,?,?)",
                java.sql.Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, r.title.trim());
            ps.setBigDecimal(2, r.amount);
            ps.setString(3, r.type);
            ps.setString(4, r.category.trim());
            ps.setDate(5, Date.valueOf(r.date));
            return ps;
        }, keyHolder);
        Number generatedId = keyHolder.getKey();
        return jdbc.queryForMap("SELECT id,title,amount,type,category,txn_date AS date FROM transactions WHERE id=?", generatedId.longValue());
    }

    @DeleteMapping("/transactions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTransaction(@PathVariable long id) {
        int changed = jdbc.update("DELETE FROM transactions WHERE id=?", id);
        if (changed == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found");
    }

    @GetMapping("/budgets")
    public List<Map<String,Object>> budgets() {
        return jdbc.queryForList("SELECT id,category,monthly_limit AS monthlyLimit FROM budgets ORDER BY category");
    }

    @PostMapping("/budgets")
    public Map<String,Object> saveBudget(@RequestBody BudgetRequest r) {
        if (r.category == null || r.category.isBlank() || r.monthlyLimit == null || r.monthlyLimit.signum() <= 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category and positive monthly limit are required.");
        jdbc.update("MERGE INTO budgets(category,monthly_limit) KEY(category) VALUES (?,?)", r.category.trim(), r.monthlyLimit);
        return jdbc.queryForMap("SELECT id,category,monthly_limit AS monthlyLimit FROM budgets WHERE category=?", r.category.trim());
    }

    @GetMapping("/goals")
    public List<Map<String,Object>> goals() {
        return jdbc.queryForList("SELECT id,name,target_amount AS targetAmount,saved_amount AS savedAmount FROM savings_goals ORDER BY id");
    }

    @PostMapping("/goals")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> addGoal(@RequestBody GoalRequest r) {
        if (r.name == null || r.name.isBlank() || r.targetAmount == null || r.targetAmount.signum() <= 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Goal name and positive target amount are required.");
        org.springframework.jdbc.support.GeneratedKeyHolder keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();
        jdbc.update(connection -> {
            var ps = connection.prepareStatement(
                "INSERT INTO savings_goals(name,target_amount,saved_amount) VALUES (?,?,?)",
                java.sql.Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, r.name.trim());
            ps.setBigDecimal(2, r.targetAmount);
            ps.setBigDecimal(3, BigDecimal.ZERO);
            return ps;
        }, keyHolder);
        Number generatedId = keyHolder.getKey();
        return jdbc.queryForMap("SELECT id,name,target_amount AS targetAmount,saved_amount AS savedAmount FROM savings_goals WHERE id=?", generatedId.longValue());
    }

    @PatchMapping("/goals/{id}/contribute")
    public Map<String,Object> contribute(@PathVariable long id, @RequestParam BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contribution must be positive.");
        int changed = jdbc.update("UPDATE savings_goals SET saved_amount=saved_amount+? WHERE id=?", amount, id);
        if (changed == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Savings goal not found");
        return jdbc.queryForMap("SELECT id,name,target_amount AS targetAmount,saved_amount AS savedAmount FROM savings_goals WHERE id=?", id);
    }

    @GetMapping("/dashboard")
    public Map<String,Object> dashboard() {
        String start = YearMonth.now().atDay(1).toString();
        String end = YearMonth.now().atEndOfMonth().toString();
        BigDecimal income = jdbc.queryForObject(
            "SELECT COALESCE(SUM(amount),0) FROM transactions WHERE type='INCOME' AND txn_date BETWEEN ? AND ?",
            BigDecimal.class, Date.valueOf(start), Date.valueOf(end));
        BigDecimal expenses = jdbc.queryForObject(
            "SELECT COALESCE(SUM(amount),0) FROM transactions WHERE type='EXPENSE' AND txn_date BETWEEN ? AND ?",
            BigDecimal.class, Date.valueOf(start), Date.valueOf(end));
        List<Map<String,Object>> categorySpend = jdbc.queryForList(
            "SELECT category, SUM(amount) AS spent FROM transactions WHERE type='EXPENSE' AND txn_date BETWEEN ? AND ? GROUP BY category ORDER BY spent DESC",
            Date.valueOf(start), Date.valueOf(end));
        List<Map<String,Object>> budgetRows = budgets();
        for (Map<String,Object> budget : budgetRows) {
            BigDecimal spent = categorySpend.stream()
                .filter(row -> row.get("CATEGORY").equals(budget.get("CATEGORY")))
                .map(row -> (BigDecimal) row.get("SPENT")).findFirst().orElse(BigDecimal.ZERO);
            budget.put("spent", spent);
            budget.put("overBudget", spent.compareTo((BigDecimal) budget.get("MONTHLYLIMIT")) > 0);
        }
        return Map.of("month", start.substring(0,7), "income", income, "expenses", expenses,
            "balance", income.subtract(expenses), "categorySpend", categorySpend, "budgets", budgetRows);
    }

    public static class TxnRequest {
        public String title;
        public BigDecimal amount;
        public String type;
        public String category;
        public LocalDate date;
    }
    public static class BudgetRequest {
        public String category;
        public BigDecimal monthlyLimit;
    }
    public static class GoalRequest {
        public String name;
        public BigDecimal targetAmount;
    }
}
