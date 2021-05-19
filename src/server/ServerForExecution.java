package server;

import data.DataForClient;
import data.DataForServer;
import data.User;
import dragon.Dragon;
import dragon.DragonCollection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.Exchanger;
import java.util.concurrent.locks.ReentrantLock;

public class ServerForExecution extends Thread {
    private int port;
    private InetAddress address;
    private DatagramSocket socket;
    private DatabaseManager manager;
    private DataForServer command;
    private DataForClient message;
    private DragonCollection dragonCollection;
    private Exchanger<DataForServer> commandExchanger;
    private Exchanger<DataForClient> messageExchanger;
    ReentrantLock locker;


    ServerForExecution(int port, DatagramSocket socket, DatabaseManager manager, DragonCollection dragonCollection, Exchanger<DataForServer> commandExchanger, Exchanger<DataForClient> messageExchanger) {
        this.port = port;
        this.socket = socket;
        this.manager = manager;
        this.dragonCollection = dragonCollection;
        this.commandExchanger = commandExchanger;
        this.messageExchanger = messageExchanger;
    }

    public void run() {
        User user;
        Dragon dragon;
        //locker.lock();
        try {
            while (true) {
                command = commandExchanger.exchange(null);
                //locker.lock();
                switch (command.getCommandName()) {
                    case "help":
                        message = new DataForClient(dragonCollection.help());
                        break;
                    case "info":
                        message = new DataForClient(dragonCollection.info());
                        break;
                    case "show":
                        message = new DataForClient(dragonCollection.show());
                        break;
                    case "add":
                        dragon = (Dragon) command.getArgument();
                        message = new DataForClient(dragonCollection.add(dragon));
                        manager.add(dragon);
                        break;
                    case "update":
                        dragon = (Dragon) command.getArgument();
                        if (manager.update(dragon)) {
                            message = new DataForClient(dragonCollection.update(dragon));
                        } else {
                            message = new DataForClient("There is no dragon with this id in the collection or it isn't your dragon");
                        }
                        break;
                    case "remove_by_id":
                        int id = (int) command.getArgument();
                        if (manager.removeById(id)) {
                            message = new DataForClient(dragonCollection.removeById(id));
                        } else {
                            message = new DataForClient("There is no dragon with this id in the collection or it isn't your dragon");
                        }
                        break;
                    case "clear":
                        manager.clear();
                        message = new DataForClient(dragonCollection.clear());
                        manager.fillCollection(dragonCollection);
                        break;
                    case "head":
                        message = new DataForClient(dragonCollection.head());
                        break;
                    case "remove_head":
                        message = new DataForClient(dragonCollection.removeHead());
                        break;
                    case "add_if_max":
                        dragon = (Dragon) command.getArgument();
                        manager.add(dragon);
                        message = new DataForClient(dragonCollection.addIfMax(dragon));
                        break;
                    case "sum_of_age":
                        message = new DataForClient(dragonCollection.sumOfAge());
                        break;
                    case "filter_contains_name":
                        String name = (String) command.getArgument();
                        message = new DataForClient(dragonCollection.filterContainsName(name));
                        break;
                    case "filter_less_than_age":
                        long age = (long) command.getArgument();
                        message = new DataForClient(dragonCollection.filterLessThanAge(age));
                        break;
                    case "newUser":
                        user = (User) command.getArgument();
                        message = new DataForClient(manager.addUser(user));
                        break;
                }
                messageExchanger.exchange(message);
                //locker.unlock();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}