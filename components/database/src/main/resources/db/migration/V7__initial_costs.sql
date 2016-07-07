INSERT INTO users.metrics_tokens_costs_history (key, cost) VALUES
('Squonk.CpuMinutes', 0.25),
('CXN.LogP', 0.02),
('CXN.LogS', 0.05),
('CXN.MolarRefractivity', 0.02),
('CXN.ReactionEnumeration', 0.05),
('RDKit.StructureSearch.Exact', 0.0005),
('RDKit.StructureSearch.SSS', 0.005),
('RDKit.StructureSearch.Similarity', 0.005);

INSERT INTO users.metrics_tokens_costs (key, version)
   SELECT key, id FROM users.metrics_tokens_costs_history;