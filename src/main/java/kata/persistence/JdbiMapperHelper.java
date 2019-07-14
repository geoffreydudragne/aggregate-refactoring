package kata.persistence;

import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.NoSuchMapperException;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

class JdbiMapperHelper {

    static <T> T mapTo(ResultSet rs, String columnLabel, Class<T> type, StatementContext ctx) throws SQLException {
        ColumnMapper<T> mapper = ctx.findColumnMapperFor(type).orElseThrow(() -> new NoSuchMapperException(type.toString()));
        return mapper.map(rs, columnLabel, ctx);
    }
}