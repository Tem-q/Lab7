package server;

import dragon.*;

public class ServerApp {
    public static void main(String[] args) {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("No driver to connect to the database");
        }
        String jdbcURL = "jdbc:postgresql://localhost:9999/studs";
        DatabaseManager manager = new DatabaseManager(jdbcURL, "username", "password");
        manager.connectionToDatabase();

        DragonCollection dragonCollection = new DragonCollection();
        manager.fillCollection(dragonCollection);
        Server server = new Server(5000, manager);

        server.run(dragonCollection, manager);
        dragonCollection.save();
    }
}
