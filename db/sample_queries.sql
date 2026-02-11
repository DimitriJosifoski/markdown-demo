--1. Production Line Ranking (Most issues this week) Answers: "Which production lines had the most issues this week?"

SELECT 
    pl.line_name, 
    COUNT(p.id) AS total_defects
FROM production_logs p
JOIN production_lines pl ON p.production_line_id = pl.id
WHERE p.issue_flag = TRUE 
  AND p.production_date >= CURRENT_DATE - INTERVAL '7 days'
GROUP BY pl.line_name
ORDER BY total_defects DESC;

--2. Shipping Risk Report (Problematic batches that shipped) Answers: "Has a problematic batch already shipped?"

SELECT 
    l.lot_identifier, 
    c.customer_name, 
    s.ship_date, 
    dt.defect_name AS issue_found
FROM lots l
JOIN production_logs p ON l.id = p.lot_id
JOIN shipping_logs s ON l.id = s.lot_id
JOIN defect_types dt ON p.defect_type_id = dt.id
JOIN customers c ON s.customer_id = c.id
WHERE p.issue_flag = TRUE 
  AND s.ship_status = 'Shipped';

--3. Defect Trend Analysis Answers: "Is a certain defect type getting worse over time?"

SELECT 
    dt.defect_name, 
    DATE_TRUNC('week', p.production_date) AS production_week, 
    COUNT(*) AS occurrence_count
FROM production_logs p
JOIN defect_types dt ON p.defect_type_id = dt.id
GROUP BY dt.defect_name, production_week
ORDER BY production_week DESC, occurrence_count DESC;
