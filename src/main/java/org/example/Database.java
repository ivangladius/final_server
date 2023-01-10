package org.example;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.sql.Timestamp;

public interface Database {

    public void databaseConnection() throws SQLException;

    public void closeConnection(); // Do we need this method to close connection to the database or is it already included in the Java.connection;

    public ResultSet executeQuery(String query) throws SQLException; // Not sure whether we need this method or not

    public int executeUpdate(String string) throws SQLException; // Not sure whether we need this method or not 

    public void queryCreateTables() throws SQLException;

    public String queryFindIDByEmail(String email);

    public String queryFindIDByUsername(String username);

    public boolean queryValidateID(Integer id);

    public String queryInsertUser(String userName, String email, String password);

    public int verifyLoginCredentials(String email, String password);

    public int checkIfUserExists(String username, String email);

    public List<String[]> querySearchUsers(String name);

    public String queryGetUsername(String email);

    String queryFindUsernameByID(Integer id);

    public int queryChangeUsername(Integer id, String userName);

    public int queryChangeEmail(Integer id, String email);

    public int queryChangePassword(Integer id, String password);

    public int queryDeleteUser(Integer id);

    public List<String> queryListFriends(Integer id);

    // we send . to chats table if friend was added, now we remove it if msg count > 2
//    public void cleanoutMessages();

    public int queryAddMessage(Integer senderID, Integer receiverID, String message, Timestamp Time);

    public List<String> queryGetMessages(Integer id, String partner);

    public int queryDeleteSelectedMessages(Integer senderId, Integer receiverId, List<Integer> messageId);
}
