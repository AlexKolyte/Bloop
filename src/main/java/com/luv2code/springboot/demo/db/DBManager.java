package com.luv2code.springboot.demo.db;

import com.luv2code.springboot.demo.controller.ChatController;
import com.luv2code.springboot.demo.chat.ChatMessage;
import com.luv2code.springboot.demo.entity.User;
import com.luv2code.springboot.demo.controller.UserRestController;
import com.luv2code.springboot.demo.entity.UserPosition;

import java.sql.*;
import java.util.ArrayList;

public class DBManager {

    private static String DATABASE_URL = "jdbc:mysql://localhost:3306/user_directory";
    private final static String username = "springstudent";
    private final static String password = "springstudent";

    public static int createUser(String username, String password, String email) {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, DBManager.username, DBManager.password)) {
            connection.setAutoCommit(false);

            String insertUserSQL = "INSERT INTO user_directory.user (username, password, email) VALUES (?, ?, ?)";
            try (PreparedStatement userStatement = connection.prepareStatement(insertUserSQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
                userStatement.setString(1, username);
                userStatement.setString(2, password);
                userStatement.setString(3, email);

                int affectedRows = userStatement.executeUpdate();

                if (affectedRows == 0) {
                    throw new SQLException("Creating order failed, no rows affected.");
                }

                int orderId;
                try (ResultSet generatedKeys = userStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        orderId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Creating order failed, no ID obtained.");
                    }
                }
                connection.commit();
                return orderId;
            } catch (SQLException e) {
                connection.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }


    public static void updateUserAndCoordinates(String new_username, String lat, String lon, String old_username)
    {
        String updateEmailSQL =
                "UPDATE user_directory.`position` " +
                        "SET username = ?, latitude = ?, longitude= ? " +
                        "WHERE username = ?;";

        try (Connection connection = DriverManager.getConnection(
                DATABASE_URL, DBManager.username, DBManager.password)) {

            connection.setAutoCommit(false);

            try (PreparedStatement stmt = connection.prepareStatement(updateEmailSQL)) {
                stmt.setString(1, new_username);
                stmt.setString(2, lat);
                stmt.setString(3, lon);
                stmt.setString(4, old_username);

                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException(
                            "Updating user coordinates and username failed, no rows affected.");
                }

                // 3) commit if successful
                connection.commit();

            } catch (SQLException e) {
                // rollback on error
                connection.rollback();
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateUser(String new_username, String new_password, String old_username)
    {
        String updateEmailSQL =
                "UPDATE user_directory.`user` " +
                        "SET username = ?, password = ? " +
                        "WHERE username = ?;";

        try (Connection connection = DriverManager.getConnection(
                DATABASE_URL, DBManager.username, DBManager.password)) {

            connection.setAutoCommit(false);

            try (PreparedStatement stmt = connection.prepareStatement(updateEmailSQL)) {
                stmt.setString(1, new_username);
                stmt.setString(2, new_password);
                stmt.setString(3, old_username);

                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException(
                            "Updating user username and password failed, no rows affected.");
                }

                // 3) commit if successful
                connection.commit();

            } catch (SQLException e) {
                // rollback on error
                connection.rollback();
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateUsername(String new_username, String old_username)
    {
        String updateEmailSQL =
                "UPDATE user_directory.`user` " +
                        "SET username = ?" +
                        "WHERE username = ?;";

        try (Connection connection = DriverManager.getConnection(
                DATABASE_URL, DBManager.username, DBManager.password)) {

            connection.setAutoCommit(false);

            try (PreparedStatement stmt = connection.prepareStatement(updateEmailSQL)) {
                stmt.setString(1, new_username);
                stmt.setString(2, old_username);

                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException(
                            "Updating user username failed, no rows affected.");
                }

                // 3) commit if successful
                connection.commit();

            } catch (SQLException e) {
                // rollback on error
                connection.rollback();
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateUserEmail(String username, String newEmail) {
        String updateEmailSQL =
                "UPDATE user_directory.`user` " +
                        "SET email = ? " +
                        "WHERE username = ?;";

        try (Connection connection = DriverManager.getConnection(
                DATABASE_URL, DBManager.username, DBManager.password)) {

            connection.setAutoCommit(false);

            try (PreparedStatement stmt = connection.prepareStatement(updateEmailSQL)) {
                // 1) bind the new email
                stmt.setString(1, newEmail);
                // 2) bind the username of the record to update
                stmt.setString(2, username);

                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException(
                            "Updating user email failed, no rows affected.");
                }

                // 3) commit if successful
                connection.commit();
                System.out.printf(
                        "Updated email for user '%s' to '%s'%n", username, newEmail);

            } catch (SQLException e) {
                // rollback on error
                connection.rollback();
                e.printStackTrace();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void saveLastCoordinate()
    {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DriverManager.getConnection(DATABASE_URL, username, password);

            String query = "SELECT * FROM user_directory.position\n" +
                    "ORDER BY id DESC\n" +
                    "LIMIT 1;";
            statement = connection.prepareStatement(query);

            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String username = resultSet.getString("username");
                String latitude = resultSet.getString("latitude");
                String longitude = resultSet.getString("longitude");

                UserRestController.userPositions.add(new UserPosition(username, latitude, longitude));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static int saveLastUser()
    {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DriverManager.getConnection(DATABASE_URL, username, password);

            String query = "SELECT * FROM user\n" +
                    "ORDER BY id DESC\n" +
                    "LIMIT 1;";
            statement = connection.prepareStatement(query);

            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String username = resultSet.getString("username");
                String password = resultSet.getString("password");
                String email = resultSet.getString("email");

                UserRestController.users.add(new User(username, password, email));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public static ArrayList<User> getAllUsers(){
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DriverManager.getConnection(DATABASE_URL, username, password);
            String query = "SELECT * FROM user";
            statement = connection.prepareStatement(query);

            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String username = resultSet.getString("username");
                String password = resultSet.getString("password");
                String email = resultSet.getString("email");
                UserRestController.users.add(new User(username, password, email));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return UserRestController.users;
    }


    public static void saveMessage(ChatMessage chatMessage)
    {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, DBManager.username, DBManager.password)) {
            connection.setAutoCommit(false);

            String insertUserSQL = "INSERT INTO user_directory.message (content, sender, recipient, user_id) VALUES (?, ?, ?, ?)";
            try (PreparedStatement userStatement = connection.prepareStatement(insertUserSQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
                userStatement.setString(1, chatMessage.getContent());
                userStatement.setString(2, chatMessage.getSender());
                userStatement.setString(3, chatMessage.getRecipient());
                userStatement.setInt(4, chatMessage.getUser_id());
                int affectedRows = userStatement.executeUpdate();
                ChatController.messages.add(new ChatMessage(chatMessage.getContent(), chatMessage.getSender(), chatMessage.getRecipient(), chatMessage.getUser_id()));
                if (affectedRows == 0) {
                    throw new SQLException("Creating order failed, no rows affected.");
                }

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static int getUserId(String username)
    {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DriverManager.getConnection(DATABASE_URL, username, password);

            String query = "SELECT id FROM user where username=?";
            statement = connection.prepareStatement(query);
            statement.setString(1, username);

            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                return id;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public static void saveUserPosition(String latitude, String longitude, String username)
    {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, DBManager.username, DBManager.password)) {
            connection.setAutoCommit(false);

            String insertUserSQL = "INSERT INTO user_directory.position (username, latitude, longitude) VALUES (?, ?, ?)";
//            String insertUserSQL = "\"UPDATE user_directory.position \" +\n" +
//                    "                        \"SET latitude = ? \" +\n" +
//                    "                        \" SET longitude = ? WHERE username = ?;\"";
            try (PreparedStatement userStatement = connection.prepareStatement(insertUserSQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
                userStatement.setString(1, username);
                userStatement.setString(2, latitude);
                userStatement.setString(3, longitude);

                int affectedRows = userStatement.executeUpdate();

                if (affectedRows == 0) {
                    throw new SQLException("Creating order failed, no rows affected.");
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void updateUserPosition(String latitude, String longitude, String username)
    {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, DBManager.username, DBManager.password)) {
            connection.setAutoCommit(false);

//            String insertUserSQL = "INSERT INTO user_directory.position (username, latitude, longitude) VALUES (?, ?, ?)";
            String insertUserSQL = "UPDATE user_directory.position\n" +
                    "SET\n" +
                    "  latitude  = ?,\n" +
                    "  longitude = ?\n" +
                    "WHERE\n" +
                    "  username  = ?;\n";
            try (PreparedStatement userStatement = connection.prepareStatement(insertUserSQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
                userStatement.setString(1, latitude);
                userStatement.setString(2, longitude);
                userStatement.setString(3, username);

                int affectedRows = userStatement.executeUpdate();

                if (affectedRows == 0) {
                    throw new SQLException("Creating order failed, no rows affected.");
                }
                connection.commit();
                fillUserPositions();
            } catch (SQLException e) {
                connection.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static UserPosition fillUserPositions()
    {

        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        UserRestController.userPositions.clear();
        try {
            connection = DriverManager.getConnection(DATABASE_URL, username, password);

            String query = "SELECT * FROM user_directory.position";
            statement = connection.prepareStatement(query);

            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String username = resultSet.getString("username");
                String latitude = resultSet.getString("latitude");
                String longitude = resultSet.getString("longitude");

                if(username!=null) {
                    UserRestController.userPositions.add(new UserPosition(username, latitude, longitude));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public static boolean isUser(String username)
    {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DriverManager.getConnection(DATABASE_URL, DBManager.username, DBManager.password);

            String query = "SELECT id FROM user WHERE username = ?";
            statement = connection.prepareStatement(query);
            statement.setString(1, username);
            resultSet = statement.executeQuery();

            if(resultSet.next())
            {
                return true;
            }
            else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static ChatMessage getAllMessages()
    {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DriverManager.getConnection(DATABASE_URL, username, password);

            String query = "SELECT * FROM message";
            statement = connection.prepareStatement(query);

            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String content = resultSet.getString("content");
                String sender = resultSet.getString("sender");
                String recipient = resultSet.getString("recipient");
                int userId = resultSet.getInt(("user_id"));

                if(content!=null) {
                    ChatController.messages.add(new ChatMessage(content, sender, recipient, userId));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    public static boolean deleteUser(String username) {
        String sql = "DELETE FROM user_directory.`user` WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL, DBManager.username, DBManager.password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static int countUsers() {
        String sql = "SELECT COUNT(*) AS total FROM user_directory.`user`";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL, DBManager.username, DBManager.password);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getUserEmail(String username) {
        String sql = "SELECT email FROM user_directory.`user` WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL, DBManager.username, DBManager.password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("email");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean positionExists(String username) {
        String sql = "SELECT 1 FROM user_directory.position WHERE username = ? LIMIT 1";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL, DBManager.username, DBManager.password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void batchCreateUsers(java.util.List<User> users) {
        String sql = "INSERT INTO user_directory.`user` (username, password, email) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL, username, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);
            for (User u : users) {
                stmt.setString(1, u.getUsername());
                stmt.setString(2, u.getPassword());
                stmt.setString(3, u.getEmail());
                stmt.addBatch();
            }
            stmt.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean updateEmailAndPosition(String username, String newEmail,
                                                 String newLat, String newLon) {
        String emailSql    = "UPDATE user_directory.`user` SET email = ? WHERE username = ?";
        String positionSql = "UPDATE user_directory.position SET latitude = ?, longitude = ? WHERE username = ?";
        Connection conn    = null;
        PreparedStatement emailStmt = null;
        PreparedStatement posStmt   = null;
        try {
            conn = DriverManager.getConnection(DATABASE_URL, username, password);
            conn.setAutoCommit(false);

            emailStmt = conn.prepareStatement(emailSql);
            emailStmt.setString(1, newEmail);
            emailStmt.setString(2, username);
            emailStmt.executeUpdate();

            posStmt = conn.prepareStatement(positionSql);
            posStmt.setString(1, newLat);
            posStmt.setString(2, newLon);
            posStmt.setString(3, username);
            posStmt.executeUpdate();

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (emailStmt != null) emailStmt.close();
                if (posStmt   != null) posStmt.close();
                if (conn      != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean pingDatabase() {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL, username, password)) {
            return conn.isValid(2);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}


