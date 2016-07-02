package org.squonk.core.service.metrics

import groovy.sql.Sql
import groovy.util.logging.Log
import org.squonk.core.TokenCostHistoryDTO
import org.squonk.core.TokenUsageDTO
import org.squonk.core.util.SquonkServerConfig
import org.squonk.util.ExecutionStats

import javax.annotation.Nonnull
import javax.sql.DataSource

/**
 * Created by timbo on 28/06/16.
 */
@Log
class TokensPostgresClient {

    protected final DataSource dataSource = SquonkServerConfig.INSTANCE.getSquonkDataSource();


    /** Update the cost of a particular token. Writes it as new entry in the tokens_costs_history table and then references
     * that row from the tokens_costs table
     *
     * @param key
     * @param cost
     */
    void updateCost(String key, float cost) {
        Sql db = createSql()
        try {
            db.withTransaction {
                def keys = db.executeInsert(
                        "INSERT INTO users.metrics_tokens_costs_history (key, cost, created) VALUES (?, ?, NOW())",
                        [key, cost])
                Long id = findInsertedId(keys)
                db.executeInsert("""\
                |INSERT INTO users.metrics_tokens_costs AS t (key,version)
                |  VALUES (:k, :r)
                |  ON CONFLICT ON CONSTRAINT uq_tc_key DO UPDATE
                |    SET version=:r
                |      WHERE t.key=:k""".stripMargin(),
                        [k:key, r:id])
            }
        } finally {
            db.close()
        }
    }

    List<TokenCostHistoryDTO> getCostHistory(String key) {
        Sql db = createSql()
        try {
            def results = []
            db.eachRow("SELECT h.id, h.key, h.cost::real, h.created FROM users.metrics_tokens_costs_history h WHERE key=$key ORDER BY created DESC") {
                results << buildTokenCostHistoryDTO(it)
            }
            return results
        } finally {
            db.close()
        }
    }

    TokenCostHistoryDTO getCurrentCost(String key) {
        Sql db = createSql()
        try {
            def row = db.firstRow("SELECT h.id, h.key, h.cost::real, h.created FROM users.metrics_tokens_costs c JOIN users.metrics_tokens_costs_history h ON h.id=c.version WHERE c.key=$key")
            return row == null ? null : buildTokenCostHistoryDTO(row)
        } finally {
            db.close()
        }
    }



    List<TokenUsageDTO> getUserTokenUsage(String username) {
        Sql db = createSql()
        try {
            def results = []
            db.eachRow("""\
                        |SELECT u.username, m.job_uuid, m.key, m.units, m.tokens::real, m.created
                        |  FROM users.metrics_tokens_usage m
                        |  JOIN users.jobstatus j ON j.uuid = m.job_uuid
                        |  JOIN users.users u ON u.id = j.owner_id
                        |  WHERE u.username=? ORDER BY m.id DESC""".stripMargin(), [username]) {
                results << new TokenUsageDTO(it.username, it.job_uuid, it.key, it.units, it.tokens, it.created)
            }
            return results
        } finally {
            db.close()
        }
    }


    void saveExecutionStats(@Nonnull ExecutionStats stats) {
        String jobId = stats.jobId
        if (jobId == null) {
            log.warning("Job ID not defined - can't save stats: $stats")
            return;
        }
        log.info("Saving stats: $stats")
        Sql db = createSql()
        try {
            stats.data.each { k, v ->

                db.withBatch(50, """\
                |INSERT INTO users.metrics_tokens_usage (job_uuid, key, units, tokens, version, created)
                |  SELECT :uuid, :key, :units, ss.tokens, ss.version, NOW() FROM (
                |     SELECT c.version, (h.cost * :units) tokens FROM users.metrics_tokens_costs c
                |       JOIN users.metrics_tokens_costs_history h ON c.version=h.id
                |       WHERE c.key=:key
                |     UNION ALL VALUES (NULL::int, NULL::numeric)
                |     LIMIT 1
                |   ) AS ss""".stripMargin()) { ps ->

                    ps.addBatch([uuid: jobId, key: k, units: v])
                }
            }

        } finally {
            db.close()
        }
    }



    // ------------------- implementation methods -----------------------

    protected Sql createSql() {
        new Sql(dataSource.getConnection())
    }

    private Long findInsertedId(def keys) {
        if (keys != null && keys.size() == 1) {
            return keys[0][0]
        }
        return null
    }

    private buildTokenCostHistoryDTO(def row) {
        return new TokenCostHistoryDTO(row.id, row.key, row.cost, row.created)
    }

}
