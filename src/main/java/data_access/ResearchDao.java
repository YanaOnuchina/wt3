package data_access;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.ResultSetExtractor;
import service.ResearchInfo;
import service.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResearchDao {
    JdbcTemplate template;

    public void setTemplate(JdbcTemplate template){
        this.template = template;
    }

    public List<String> getAllResearches(){
        String sql = "select research_name from researches";
        return template.query(sql, new ResultSetExtractor<List<String>>(){
            public List<String> extractData(ResultSet rs) throws SQLException, DataAccessException {
                List<String> researches = new ArrayList<String>();
                while(rs.next()){
                    researches.add(rs.getString(1));
                }
                return researches;
            }
        });
    }

    public List<String> getActiveForUser(int user_id){
        String sql = "SELECT research_name, id_research FROM researches WHERE status = 'running'";
        return template.query(sql, new ResultSetExtractor<List<String>>(){
            public List<String> extractData(ResultSet rs) throws SQLException, DataAccessException {
                List<String> researches = new ArrayList<String>();
                while(rs.next()){
                    if (!checkResearchAnswered(rs.getInt(2), user_id)) {
                        researches.add(rs.getString(1));
                    }
                }
                return researches;
            }
        });
    }

    public boolean checkResearchAnswered(int id_research, int id_user) {
        String sql = "SELECT id_answer FROM answers WHERE research = '" + id_research + "' AND user = '" + id_user  + "'";
        return Boolean.TRUE.equals(template.query(sql, new ResultSetExtractor<Boolean>() {
            public Boolean extractData(ResultSet rs) throws SQLException, DataAccessException {
                return rs.next();
            }
        }));
    }

    public void addResearch(String name){
        int maxCounter = getMaxCounter("id_research", "researches");
        String sql = "INSERT INTO researches (id_research, research_name, status) VALUES (" +
                (maxCounter + 1) + ", '" + name + "', 'paused')";
        template.update(sql);
    }

    public int getMaxCounter(String column_name, String table_name){
        String sql = "SELECT MAX(" + column_name + ") FROM " + table_name;
        Integer result = template.queryForObject(sql, Integer.class);;
        return result == null ? 0 : result;
    }

    public int countQuestions(int user_id){
        String sql = "SELECT COUNT(*) FROM answers WHERE user = '" + user_id + "'";
        Integer result = template.queryForObject(sql, Integer.class);
        return result == null ? 0 : result;
    }

    public int countResearches(int user_id){
        String sql = "SELECT COUNT(DISTINCT research) FROM answers WHERE user = '" + user_id + "'";
        Integer result = template.queryForObject(sql, Integer.class);
        return result == null ? 0 : result;
    }

    public HashMap<String, Integer> countTopics(int user_id){
        String query = "SELECT COUNT(DISTINCT answers.question) FROM answers JOIN questions ON " +
                "answers.question = questions.id_question WHERE questions.topic = ? AND user = ?";
        HashMap<String, Integer> topics = getTopics();
        HashMap<String, Integer> result = new HashMap<>();
        for (Map.Entry<String, Integer> entry : topics.entrySet()) {
            result.put(entry.getKey(),
            template.execute(query, new PreparedStatementCallback<Integer>() {
                @Override
                public Integer doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                    ps.setInt(1, entry.getValue());
                    ps.setInt(2, user_id);
                    ResultSet rs = ps.executeQuery();
                    rs.next();
                    return rs.getInt(1);
                }
            })
            );
        }
        return result;
    }

    public HashMap<String, Integer> getTopics(){
        String sql = "SELECT id_topic, topic_name FROM topics";
        return template.query(sql, new ResultSetExtractor<HashMap<String, Integer>>(){
            public HashMap<String, Integer> extractData(ResultSet rs) throws SQLException, DataAccessException {
                HashMap<String, Integer> topics = new HashMap<>();
                while(rs.next()){
                    topics.put(rs.getString(2), rs.getInt(1));
                }
                return topics;
            }
        });
    }

    public ResearchInfo getResearchInfo(String research_name){
        String query = "SELECT id_research, status FROM researches WHERE research_name = ?";
        ResearchInfo result;
        result = template.execute(query, new PreparedStatementCallback<ResearchInfo>() {
            @Override
            public ResearchInfo doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                ps.setString(1, research_name);
                ResultSet rs = ps.executeQuery();
                rs.next();
                ResearchInfo researchInfo = new ResearchInfo();
                researchInfo.setResearch_id(rs.getInt(1));
                researchInfo.setStatus(rs.getString(2));
                return researchInfo;
            }
        });
        if (result != null) {
            result.setResearch_name(research_name);
            HashMap<String, Integer> questions = getQuestionsByResearchId(result.getResearch_id());
            HashMap<String, String> questionsTopic = new HashMap<>();
            for (Map.Entry<String, Integer> entry : questions.entrySet()){
                questionsTopic.put(entry.getKey(), getTopicNameById(entry.getValue()));
            }
            result.setQuestions(questionsTopic);
        }
        return result;
    }

    public String getTopicNameById (int id){
        String sql = "SELECT topic_name FROM topics WHERE id_topic = '" + id + "'";
        String result = template.queryForObject(sql, String.class);
        return result == null ? "" : result;
    }

    public HashMap<String, Integer> getQuestionsByResearchId(int id){
        String sql = "SELECT question, topic FROM questions WHERE research = '" + id + "'";
        return template.query(sql, new ResultSetExtractor<HashMap<String, Integer>>() {
            @Override
            public HashMap<String, Integer> extractData(ResultSet rs) throws SQLException, DataAccessException {
                HashMap<String, Integer> questions = new HashMap<>();
                while (rs.next()) {
                    questions.put(rs.getString(1), rs.getInt(2));
                }
                return questions;
            }
        });
    }

    public void addQuestion(String research, String topic, String question){
        int idResearch = getIdByResearchName(research);
        int idTopic = getIdByTopicName(topic);
        int idQuestion = getMaxCounter("id_question", "questions") + 1;
        String query = "INSERT INTO questions (id_question, question, topic, research) VALUES (?, ?, ?, ?)";
        template.execute(query, new PreparedStatementCallback<Integer>() {
            @Override
            public Integer doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                ps.setInt(1, idQuestion);
                ps.setString(2, question);
                ps.setInt(3, idTopic);
                ps.setInt(4, idResearch);
                return ps.executeUpdate();
            }
        });
    }

    public int getIdByTopicName(String topic){
        String sql = "SELECT id_topic FROM topics WHERE topic_name = ?";
        Integer result = template.execute(sql, new PreparedStatementCallback<Integer>() {
            @Override
            public Integer doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                ps.setString(1, topic);
                ResultSet rs = ps.executeQuery();
                return rs.next() ? rs.getInt(1) : 0;
            }
        });
        return result == null ? 0 : result;
    }

    public int getIdByResearchName(String research){
        String sql = "SELECT id_research FROM researches WHERE research_name = ?";
        Integer result = template.execute(sql, new PreparedStatementCallback<Integer>() {
            @Override
            public Integer doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                ps.setString(1, research);
                ResultSet rs = ps.executeQuery();
                return rs.next() ? rs.getInt(1) : 0;
            }
        });
        return result == null ? 0 : result;
    }

    public int getIdByQuestionName(String question){
        String sql = "SELECT id_question FROM questions WHERE question = ?";
        Integer result = template.execute(sql, new PreparedStatementCallback<Integer>() {
            @Override
            public Integer doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                ps.setString(1, question);
                ResultSet rs = ps.executeQuery();
                return rs.next() ? rs.getInt(1) : 0;
            }
        });
        return result == null ? 0 : result;
    }

    public void setResearchStatus(String research, String status){
        int research_id = getIdByResearchName(research);
        String sql = "UPDATE researches SET status = ? WHERE id_research = ?";
        template.execute(sql, new PreparedStatementCallback<Integer>() {
            @Override
            public Integer doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                ps.setString(1, status);
                ps.setInt(2, research_id);
                return ps.executeUpdate();
            }
        });
    }

    @Autowired
    UserDao userDao;

    public HashMap<String, String> getAnswers(String question){
        int id_question = getIdByQuestionName(question);
        String query = "SELECT answer, user FROM answers WHERE question = ?";
        HashMap<String, String> result = template.execute(query, new PreparedStatementCallback<HashMap<String, String>>() {
            @Override
            public HashMap<String, String> doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                ps.setInt(1, id_question);
                ResultSet rs = ps.executeQuery();
                HashMap<String, String> answers = new HashMap<>();
                while (rs.next()){
                    answers.put(rs.getString(1), userDao.getLoginById(rs.getInt(2)));
                }
                return answers;
            }
        });
        return result;
    }

    public void addAnswer(String research_name, String question_name, String answer, int user_id){
        int id_answer = getMaxCounter("id_answer", "answers") + 1;
        int id_research = getIdByResearchName(research_name);
        int id_question = getIdByQuestionName(question_name);
        String sql = "INSERT INTO answers (id_answer, question, user, answer, research) VALUES (?, ?, ?, ?, ?)";
        template.execute(sql, new PreparedStatementCallback<Integer>() {
            @Override
            public Integer doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
                ps.setInt(1, id_answer);
                ps.setInt(2, id_question);
                ps.setInt(3, user_id);
                ps.setString(4, answer);
                ps.setInt(5, id_research);
                return ps.executeUpdate();
            }
        });
    }

}
