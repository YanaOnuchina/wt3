package data_access;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.ResultSetExtractor;
import service.User;
import service.UserDataChecker;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class UserDao {
    JdbcTemplate template;

    public void setTemplate(JdbcTemplate template){
        this.template = template;
    }

    public User getUserByEmail(String email){
        String sql = "select * from users where login = '" + email + "'";
        return template.query(sql, new ResultSetExtractor<User>(){
            public User extractData(ResultSet rs) throws SQLException, DataAccessException {
                User user = new User();
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                    user.setEmail(rs.getString(2));
                    user.setPassword(rs.getString(3));
                    user.setRole(rs.getString(4));
                    user.setStatus(rs.getString(5));
                }
                return user;
            }
        });
    }

    public void updateStatus(int id, String status){
        String sql = "update users set status = '" + status + "' where id_user = '" + id + "'";
        template.update(sql);
    }

    public void addUser(String email, String password){
        int maxCounter = getMaxCounter("id_user", "users");
        String role = UserDataChecker.isAdmin(email) ? "admin" : "user";
        String sql = "INSERT INTO users (id_user, login, password, role, status) VALUES (" + (maxCounter + 1) + ", '" +
                email + "', '" + password + "', '" + role + "', 'inactive')";
        template.update(sql);
    }

    public int getMaxCounter(String column_name, String table_name){
        String sql = "SELECT MAX(" + column_name + ") FROM " + table_name;
            Integer result = template.queryForObject(sql, Integer.class);
            return result == null ? 0 : result;
    }

    public String getLoginById(int id){
        String sql = "SELECT login FROM users WHERE id_user = '" + id + "'";
        return template.query(sql, new ResultSetExtractor<String>() {
            @Override
            public String extractData(ResultSet rs) throws SQLException, DataAccessException {
                return rs.next() ? rs.getString(1) : "";
            }
        });
    }

    public void updateUserInfo(int user_id, String email, String password) {
        String sql = "UPDATE users SET login = ?, password = ? WHERE id_user = ?";
        template.execute(sql, new PreparedStatementCallback<Integer>() {
            @Override
            public Integer doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                ps.setString(1, email);
                ps.setString(2, password);
                ps.setInt(3, user_id);
                return ps.executeUpdate();
            }
        });
    }
}
