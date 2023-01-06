package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HSQLDatabase implements Database {

    Connection connection;
    public static HSQLDatabase instance = null;

    public static HSQLDatabase getInstance() throws SQLException {
        // Singleton pattern implementation
        // Only one instance will be created and will be reused for each object


        HSQLDatabase result = instance;

        if (result == null) {

            synchronized (Database.class) {

                result = instance;
                if (result == null) {

                    result = new HSQLDatabase();
                }
            }
        }

        result.databaseConnection();
        result.queryCreateTables();

        return result;
    }


    public void databaseConnection() throws SQLException {

        try {

            Class.forName("org.hsqldb.jdbc.JDBCDriver");

        } catch (Exception e) {

            System.err.println("ERROR: failed to load HSQLDB JDBC driver.");
            e.printStackTrace();
            return;
        }


//        connection = DriverManager.getConnection("jdbc:hsqldb:file:C:\\Users\\Mubashir Ahmed\\Documents\\Database\\Database; hsqldb.lock_file=false; shutdown = true", "myDB", "PK313");
        connection = DriverManager.getConnection("jdbc:hsqldb:file:~/DB_TEST/; hsqldb.lock_file=false; shutdown = true", "myDB", "PK313");
        connection.setAutoCommit(true);

    }


    @Override
    public void closeConnection() {
        // Closes the connection to the database

        try {
            connection.close();
        } catch (SQLException e) {

            e.printStackTrace();

        }
    }


    @Override
    public ResultSet executeQuery(String query) throws SQLException {

        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery(query);
        return result;

    }


    @Override
    public int executeUpdate(String string) throws SQLException {

        Statement statement = connection.createStatement();
        int result = statement.executeUpdate(string);
        System.out.println(result);
        connection.commit();
        return result;
    }


    @Override
    public void queryCreateTables() throws SQLException {

        try {
            PreparedStatement ps1 = connection.prepareStatement("CREATE TABLE IF NOT EXISTS USERS (ID INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL, User_Name VARCHAR(25) NOT NULL, Email VARCHAR(50) NOT NULL, Password VARCHAR(15) NOT NULL, Profile_Picture VARCHAR(255));");
            PreparedStatement ps2 = connection.prepareStatement("CREATE TABLE IF NOT EXISTS CHATS (Message_ID INT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY NOT NULL, Sender_ID INT NOT NULL, Message VARCHAR(255) NOT NULL, Receiver_ID INT NOT NULL, Time TIMESTAMP NOT NULL, FOREIGN KEY (Sender_ID) references users(ID), FOREIGN KEY (RECEIVER_ID) REFERENCES users(ID));");

            ps1.executeUpdate();
            ps1.close();
            ps2.executeUpdate();
            ps2.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
//
//    @Override
//    public String queryFindByEmail(String email) {
//        String userId;
//
//        PreparedStatement ps = connection.prepareStatement("SELECT * FROM USERS WHERE EMAIL = ?");
//        ps.setString(1, email);
//
//        return userId;
//    }

    @Override
    public String queryFindByEmail(String email) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM USERS WHERE EMAIL = ?");
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getString(1); // return if user already exists in db
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Email doesn't exist in the database
    }


    @Override
    public boolean queryValidateID(Integer id) {

        boolean status = false;

        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM users WHERE id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {

                status = true;
                return status; // ID not found in the user's table

            }

        } catch (SQLException e) {

            e.printStackTrace();

        } finally {

            try {

                connection.close();

            } catch (SQLException e) {

                e.printStackTrace();
            }
        }

        return status; // Error in executing the query
    }


    @Override
    public String queryInsertUser(String userName, String email, String password) {
        String userId = null;
        if ((userId = queryFindByEmail(email)) != null)
            return userId; // Email already exists in the database, therefore the user cannot be added

        try {
            PreparedStatement ps;
            ps = connection.prepareStatement("INSERT INTO USERS(USER_NAME, EMAIL, PASSWORD) VALUES (?, ?, ?)");
            ps.setString(1, userName);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.executeUpdate();


            return queryFindByEmail(email); // FETCH PRIMARY KEY FROM EMAIL AND RETURN TO SERVER
        } catch (SQLException e) {
            e.printStackTrace();
            return null; // Error in adding new user
        }
    }


    public int verifyLoginCredentials(String email, String password) {

        try {

            PreparedStatement pstmt = connection.prepareStatement("SELECT COUNT(*) FROM USERS WHERE Email = ? AND Password = ?");
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            return count;

        } catch (SQLException e) {

            e.printStackTrace();
            return 0;

        } finally {

            try {

                connection.close();

            } catch (SQLException e) {

                e.printStackTrace();

            }
        }
    }


    public List<String[]> querySearchUsers(String searchString) {

        try {

            PreparedStatement ps = connection.prepareStatement("SELECT * FROM users WHERE User_Name LIKE ?");
            ps.setString(1, "%" + searchString + "%");
            ResultSet rs = ps.executeQuery();

            List<String[]> users = new ArrayList<>();

            while (rs.next()) {

                String userName = rs.getString("User_Name");
                String profilePicture = rs.getString("Profile_Picture");
                String[] user = {userName, profilePicture};
                users.add(user); // Returns the list of users

            }

            return users;

        } catch (SQLException e) {

            e.printStackTrace();
            return new ArrayList<>(); // Returns an empty list on error

        } finally {

            try {

                connection.close();

            } catch (SQLException e) {

                e.printStackTrace();

            }
        }
    }


    @Override
    public int queryChangeUsername(Integer id, String userName) {

        if (queryValidateID(id) == false) {

            return 0; // ID does not exist in the database, therefore the username cannot be changed

        }


        try {

            PreparedStatement ps = connection.prepareStatement("UPDATE USERS SET USER_NAME = ? WHERE ID = ?");
            ps.setString(1, userName);
            ps.setInt(2, id);
            int rowsUpdated = ps.executeUpdate();

            if (rowsUpdated > 0) {

                return 1; // Username successfully changed

            } else {

                return 0; // No rows were updated

            }

        } catch (SQLException e) {

            e.printStackTrace();
            return 0; // Error in executing the update statement

        } finally {

            try {

                connection.close();

            } catch (SQLException e) {

                e.printStackTrace();

            }
        }
    }


    @Override
    public int queryChangeEmail(Integer id, String email) {

        if (queryValidateID(id) == false) {

            return 0; // ID does not exist in the database, therefore the email cannot be changed

        }


        try {

            PreparedStatement ps = connection.prepareStatement("UPDATE USERS SET email = ? WHERE ID = ?");
            ps.setString(1, email);
            ps.setInt(2, id);
            int rowsUpdated = ps.executeUpdate();

            if (rowsUpdated > 0) {

                return 1; // Email successfully updated

            } else {

                return 0;

            }

        } catch (SQLException e) {

            e.printStackTrace();
            return 0; // Error executing update

        } finally {

            try {

                connection.close();

            } catch (SQLException e) {

                e.printStackTrace();
            }
        }
    }


    @Override
    public int queryChangePassword(Integer id, String password) {

        if (queryValidateID(id) == false) {

            return 0; // ID does not exist in the database, therefore the password cannot be changed

        }

        try {

            PreparedStatement ps = connection.prepareStatement("UPDATE USERS SET PASSWORD = ? WHERE ID = ?");
            ps.setString(1, password);
            ps.setInt(2, id);
            int rowsUpdated = ps.executeUpdate();

            if (rowsUpdated > 0) {

                return 1; // Password successfully changed

            } else {

                return 0; // No rows were updated. This means that password was not changed

            }

        } catch (SQLException e) {

            e.printStackTrace();
            return 0; // Error in executing update statement

        } finally {

            try {

                connection.close();

            } catch (SQLException e) {

                e.printStackTrace();
            }
        }
    }


    @Override
    public int queryDeleteUser(Integer id) {

        if (queryValidateID(id) == false) {

            return 0; // ID does not exist in the database, therefore the user cannot be deleted

        }

        try {

            PreparedStatement ps = connection.prepareStatement("DELETE FROM USERS WHERE ID = ?");
            ps.setInt(1, id);
            int rowsDeleted = ps.executeUpdate();

            if (rowsDeleted > 0) {

                return 1; // User successfully deleted from the database

            } else {

                return 0; // No rows were deleted. This means the deletion of user was unsuccessful

            }

        } catch (SQLException e) {

            e.printStackTrace();
            return 0; // Error in executing delete statement

        } finally {

            try {

                connection.close();

            } catch (SQLException e) {

                e.printStackTrace();
            }
        }
    }


    @Override
    public int queryAddMessage(Integer senderID, Integer receiverID, String message, Timestamp time) {

        try {

            PreparedStatement ps = connection.prepareStatement("INSERT INTO CHATS(SENDER_ID, MESSAGE, RECEIVER_ID, TIME) VALUES (?, ?, ?)");
            ps.setInt(1, senderID);
            ps.setInt(2, receiverID);
            ps.setString(3, message);
            ps.setTimestamp(4, time);
            int rowsInserted = ps.executeUpdate();

            if (rowsInserted > 0) {

                return 1; // Message was successfully added to the database

            } else {

                return 0; // No rows were inserted

            }

        } catch (SQLException e) {

            e.printStackTrace();
            return 0; // Error in executing insert statement

        } finally {

            try {

                connection.close();

            } catch (SQLException e) {

                e.printStackTrace();
            }
        }
    }


    @Override
    public List<String> queryGetMessages(Integer id) {

        List<String> messages = new ArrayList<>();

        try {

            PreparedStatement ps = connection.prepareStatement("SELECT * FROM chats WHERE sender_ID = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                messages.add(rs.getString("message"));

            }

            return messages;

        } catch (SQLException e) {

            e.printStackTrace();

            return messages; // Returns an empty list on error

        } finally {

            try {

                connection.close();

            } catch (SQLException e) {

                e.printStackTrace();

            }
        }
    }


    public int queryDeleteSelectedMessages(Integer senderId, Integer receiverId, List<Integer> messageId) {

        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM chats WHERE sender_ID = ? AND receiver_ID = ? AND id IN (?)");
            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            ps.setArray(3, connection.createArrayOf("INTEGER", messageId.toArray()));
            ps.executeUpdate();
            return 1;

        } catch (SQLException e) {

            e.printStackTrace();
            return 0;

        } finally {

            try {

                connection.close();

            } catch (SQLException e) {

                e.printStackTrace();

            }
        }
    }

}
	
		

	