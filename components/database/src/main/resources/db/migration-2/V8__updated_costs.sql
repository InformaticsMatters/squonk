INSERT INTO users.metrics_tokens_costs_history (key, cost) VALUES
('CXN.LogD', 0.1),
('CXN.LogS', 0.1),
('CXN.pKa', 0.1);

DELETE FROM users.metrics_tokens_costs;

INSERT INTO users.metrics_tokens_costs(key, version)
  SELECT key, max(id) FROM users.metrics_tokens_costs_history
    GROUP BY key;