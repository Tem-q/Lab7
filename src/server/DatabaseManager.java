package server;

import data.User;
import dragon.*;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDate;

public class DatabaseManager {
    private String URL;
    private String username;
    private String password;
    private Connection connection;
    private User user;

    public DatabaseManager(String URL, String username, String password) {
        this.URL = URL;
        this.username = username;
        this.password = password;
    }

    public void connectionToDatabase() {
        try {
            connection = DriverManager.getConnection(URL, username, password);
            System.out.println("Connection to the database is done");
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database. Shutting down...");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void fillCollection(DragonCollection dragonCollection) {
        String request = "SELECT*FROM dragons";
        try {
            PreparedStatement statement = connection.prepareStatement(request);
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                Dragon dragon = getDragon(result);
                dragonCollection.addFromDatabase(dragon);
            }
            statement.executeQuery();
            statement.close();
            System.out.println("The collection was successfully filled from the database");
        } catch (SQLException throwables) {
            System.out.println("Something went wrong with filling collection from database");
            System.exit(-1);
        }
    }

    public Dragon getDragon(ResultSet result) throws SQLException {
        int id = result.getInt("id");
        String name = result.getString("name");
        long x_coor = result.getLong("x_coordinate");
        double y_coor = result.getDouble("y_coordinate");
        LocalDate creationDate = result.getDate("date").toLocalDate();
        long age = result.getLong("age");
        String description = result.getString("description");
        int weight = result.getInt("weight");
        DragonType type = DragonType.valueOf(result.getString("type"));
        String killerName = result.getString("killer_name");
        float killerHeight = result.getFloat("killer_height");
        long killerWeight = result.getLong("killer_weight");
        float x = result.getFloat("x");
        int y = result.getInt("y");
        float z = result.getFloat("z");
        String locationName = result.getString("location_name");
        Coordinates coordinates = new Coordinates(x_coor, y_coor);
        Location location = new Location(x, y, z, locationName);
        Person killer = new Person(killerName, killerHeight, killerWeight, location);
        Dragon dragon = new Dragon(id, name, coordinates, creationDate, age, description, weight, type, killer);
        return dragon;
    }

    public void add(Dragon dragon) {
        String request = "INSERT INTO dragons VALUES (nextval('id'), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement statement = connection.prepareStatement(request);
            statement.setString(1, dragon.getName());
            statement.setLong(2, dragon.getCoordinates().getX());
            statement.setDouble(3, dragon.getCoordinates().getY());
            statement.setDate(4, Date.valueOf(dragon.getCreationDate()));
            statement.setLong(5, dragon.getAge());
            statement.setString(6, dragon.getDescription());
            statement.setInt(7, dragon.getWeight());
            statement.setString(8, String.valueOf(dragon.getType()));
            statement.setString(9, dragon.getKiller().getName());
            statement.setFloat(10, dragon.getKiller().getHeight());
            statement.setLong(11, dragon.getKiller().getWeight());
            statement.setFloat(12, dragon.getKiller().getLocation().getX());
            statement.setInt(13, dragon.getKiller().getLocation().getY());
            statement.setFloat(14,  dragon.getKiller().getLocation().getZ());
            statement.setString(15,  dragon.getKiller().getLocation().getName());
            statement.setString(16, user.getUsername());
            statement.executeUpdate();
            statement.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public boolean update(Dragon dragon) {
        int result = 0;
        String request = "UPDATE dragons SET name = ?, x_coordinate = ?, y_coordinate = ?, age = ?, description = ?, weight = ?, type = ?, killer_name = ?, killer_height = ?, killer_weight = ?, x = ?, y = ?, z = ?, location_name = ? WHERE id = ? AND owner = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(request);
            statement.setString(1, dragon.getName());
            statement.setLong(2, dragon.getCoordinates().getX());
            statement.setDouble(3, dragon.getCoordinates().getY());
            statement.setLong(4, dragon.getAge());
            statement.setString(5, dragon.getDescription());
            statement.setInt(6, dragon.getWeight());
            statement.setString(7, String.valueOf(dragon.getType()));
            statement.setString(8, dragon.getKiller().getName());
            statement.setFloat(9, dragon.getKiller().getHeight());
            statement.setLong(10, dragon.getKiller().getWeight());
            statement.setFloat(11, dragon.getKiller().getLocation().getX());
            statement.setInt(12, dragon.getKiller().getLocation().getY());
            statement.setFloat(13,  dragon.getKiller().getLocation().getZ());
            statement.setString(14,  dragon.getKiller().getLocation().getName());
            statement.setInt(15, dragon.getId());
            statement.setString(16, user.getUsername());
            result = statement.executeUpdate();
            statement.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return result > 0;
    }

    public boolean removeById(int id) {
        String request = "DELETE FROM dragons WHERE id = ? AND owner = ?";
        int result = 0;
        try {
            PreparedStatement statement = connection.prepareStatement(request);
            statement.setInt(1, id);
            statement.setString(2, user.getUsername());
            result = statement.executeUpdate();
            statement.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return result > 0;
    }

    public void clear() {
        String request = "DELETE FROM dragons WHERE owner = ?";
        try {
            PreparedStatement statement = connection.prepareStatement(request);
            statement.setString(1, user.getUsername());
            statement.executeUpdate();
            statement.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public void removeHead() {
        String request = "DELETE FROM dragons WHERE CTID IN(SELECT CTID FROM dragons WHERE owner = ? LIMIT 1)";
        try {
            PreparedStatement statement = connection.prepareStatement(request);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public String addUser(User newUser) {
        String request = "SELECT*FROM users";
        user = newUser;
        boolean checkUserForExistence = false;
        try {
            PreparedStatement statement = connection.prepareStatement(request);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                if (resultSet.getString(1).equals(newUser.getUsername())) {
                    checkUserForExistence = true;
                    break;
                }
            }
            if (checkUserForExistence) {
                return "User signed in successfully";
            } else {
                String requestNewUser = "INSERT INTO users VALUES (?, ?)";
                PreparedStatement statementNewUser = connection.prepareStatement(requestNewUser);
                statementNewUser.setString(1, newUser.getUsername());
                String passwordSHA224 = toSHS224(newUser.getPassword());
                statementNewUser.setString(2, passwordSHA224);
                statementNewUser.executeUpdate();
                statementNewUser.close();
                return "New user is successfully added";
            }
        } catch (SQLException throwables) {
            return "Something went wrong with authorization";
        }
    }

    public String toSHS224(String password) {
        try{
            MessageDigest md  = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(password.getBytes(StandardCharsets.UTF_8));
            BigInteger numRepresentation = new BigInteger(1,digest);
            String hashedString  = numRepresentation.toString(16);
            while (hashedString.length()<32){
                hashedString = "0" + hashedString;
            }
            return hashedString;
        } catch (NoSuchAlgorithmException e) {
            System.out.println("No such algorithm");
        }
        return null;
    }
}
