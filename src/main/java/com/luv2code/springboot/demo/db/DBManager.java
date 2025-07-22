package com.luv2code.springboot.demo.db;

import com.luv2code.springboot.demo.controller.ChatController;
import com.luv2code.springboot.demo.chat.ChatMessage;
import com.luv2code.springboot.demo.entity.User;
import com.luv2code.springboot.demo.controller.UserRestController;

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

            String insertUserSQL = "INSERT INTO user_directory.message (content, sender, recipient) VALUES (?, ?, ?)";
            try (PreparedStatement userStatement = connection.prepareStatement(insertUserSQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
                userStatement.setString(1, chatMessage.getContent());
                userStatement.setString(2, chatMessage.getSender());
                userStatement.setString(3, chatMessage.getRecipient());
                int affectedRows = userStatement.executeUpdate();
                ChatController.messages.add(new ChatMessage(chatMessage.getContent(), chatMessage.getSender(), chatMessage.getRecipient()));
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

                if(content!=null) {
                    ChatController.messages.add(new ChatMessage(content, sender, recipient));
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
}
