package com.trip.routemate.database;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class SchemaCommentMigrationTest {

    private static final Set<String> EXPECTED_TABLES = Set.of(
            "TB_USER_MSTR", "TB_COUNTRY", "TB_REGION", "TB_DESTINATION",
            "TB_TRAVEL_PLAN", "TB_TRAVEL_DAY", "TB_TRAVEL_DAY_REGION",
            "TB_TRAVEL_SCHEDULE", "TB_TRAVEL_TRANSPORT", "TB_TRAVEL_PACKING_ITEM",
            "TB_ADMIN_DEPT", "TB_ADMIN_ROLE", "TB_ADMIN_PERMISSION", "TB_ADMIN_MENU",
            "TB_ADMIN_ROLE_PERMISSION", "TB_ADMIN_ROLE_MENU", "TB_ADMIN_USER_ROLE",
            "TB_DEST_RECOMMEND", "TB_TRAVEL_PRODUCT", "TB_TRAVEL_PRODUCT_OPTION"
    );

    @Test
    void mysqlAndOracleComments_coverTheSameTablesAndColumns() throws Exception {
        var mysql = resource("db/migration/mysql/V18__schema_comments.sql");
        var oracle = resource("db/migration/oracle/V18__schema_comments.sql");

        var mysqlTargets = mysqlTargets(mysql);
        var oracleTargets = oracleTargets(oracle);

        assertThat(mysqlTargets.tables()).isEqualTo(EXPECTED_TABLES);
        assertThat(oracleTargets.tables()).isEqualTo(EXPECTED_TABLES);
        assertThat(mysqlTargets.columns()).isEqualTo(oracleTargets.columns()).hasSize(158);
    }

    @Test
    void mysqlComments_preserveImportantColumnAttributes() throws Exception {
        var mysql = resource("db/migration/mysql/V18__schema_comments.sql");

        assertThat(mysql)
                .contains("USER_ID BIGINT NOT NULL AUTO_INCREMENT COMMENT")
                .contains("MDFY_DT DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT")
                .contains("DEST_DESC LONGTEXT NULL COMMENT")
                .contains("PRICE DECIMAL(12, 2) NOT NULL DEFAULT 0.00 COMMENT")
                .contains("CONFIRMATION_TYPE VARCHAR(20) NOT NULL DEFAULT 'INSTANT' COMMENT");
    }

    @Test
    void productOrderMigrations_includeDatabaseDescriptionsForBothDatabases() throws Exception {
        var mysql = resource("db/migration/mysql/V20__product_order.sql");
        var oracle = resource("db/migration/oracle/V20__product_order.sql");

        assertThat(mysql)
                .contains("COMMENT='회원이 홈페이지에서 옵션상품을 선택해 접수한 주문과 결제 상태를 보존하는 테이블'")
                .contains("ORDER_NO VARCHAR(40) NOT NULL COMMENT '고객에게 안내하는 고유 주문번호'")
                .contains("PAYMENT_STATUS VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT");
        assertThat(oracle)
                .contains("COMMENT ON TABLE TB_PRODUCT_ORDER IS '회원이 홈페이지에서 옵션상품을 선택해 접수한 주문과 결제 상태를 보존하는 테이블'")
                .contains("COMMENT ON COLUMN TB_PRODUCT_ORDER.ORDER_NO IS '고객에게 안내하는 고유 주문번호'")
                .contains("COMMENT ON COLUMN TB_PRODUCT_ORDER.PAYMENT_STATUS IS '결제 상태로 PENDING, PAID, FAILED, REFUNDED 등을 저장'");
    }

    private Targets mysqlTargets(String sql) {
        var tables = new HashSet<String>();
        var columns = new HashSet<String>();
        String currentTable = null;
        var alterTable = Pattern.compile("^ALTER TABLE (TB_[A-Z0-9_]+)(?: COMMENT.*)?;?$");
        var modifyColumn = Pattern.compile("^\\s*MODIFY COLUMN ([A-Z0-9_]+) .*$");

        for (var line : sql.lines().toList()) {
            var tableMatcher = alterTable.matcher(line);
            if (tableMatcher.matches()) {
                currentTable = tableMatcher.group(1);
                if (line.contains(" COMMENT = ")) tables.add(currentTable);
                continue;
            }
            var columnMatcher = modifyColumn.matcher(line);
            if (columnMatcher.matches() && currentTable != null) {
                columns.add(currentTable + "." + columnMatcher.group(1));
            }
        }
        return new Targets(tables, columns);
    }

    private Targets oracleTargets(String sql) {
        var tables = new HashSet<String>();
        var columns = new HashSet<String>();
        var tablePattern = Pattern.compile("COMMENT ON TABLE (TB_[A-Z0-9_]+) IS .*");
        var columnPattern = Pattern.compile("COMMENT ON COLUMN (TB_[A-Z0-9_]+\\.[A-Z0-9_]+) IS .*");

        for (var line : sql.lines().toList()) {
            var tableMatcher = tablePattern.matcher(line);
            if (tableMatcher.matches()) tables.add(tableMatcher.group(1));
            var columnMatcher = columnPattern.matcher(line);
            if (columnMatcher.matches()) columns.add(columnMatcher.group(1));
        }
        return new Targets(tables, columns);
    }

    private String resource(String path) throws Exception {
        return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
    }

    private record Targets(Set<String> tables, Set<String> columns) {
    }
}
