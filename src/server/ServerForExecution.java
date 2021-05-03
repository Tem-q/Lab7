package server;

import data.DataForClient;
import data.DataForServer;
import data.User;
import dragon.Dragon;
import dragon.DragonCollection;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.locks.ReentrantLock;

public class ServerForExecution extends Thread {
    private int port;
    private InetAddress address;
    private DatagramSocket socket;
    private DatabaseManager manager;
    private DataForServer command;
    private DataForClient message;
    private DragonCollection dragonCollection;
    ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
    ReentrantLock locker;


    ServerForExecution(int port, InetAddress address, DatagramSocket socket, DatabaseManager manager, DataForServer command, DragonCollection dragonCollection) {
        this.port = port;
        this.address = address;
        this.socket = socket;
        this.manager = manager;
        this.command = command;
        this.dragonCollection = dragonCollection;
    }

    public void run() {
        User user;
        Dragon dragon;
        //locker.lock();
        while (true) {
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
                    manager.add(dragon);
                    message = new DataForClient(dragonCollection.add(dragon));
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
            //locker.unlock();
            forkJoinPool.submit(new ServerForWriting(port, address, socket, message));
            break;
        }
    }
}