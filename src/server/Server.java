package server;

import data.*;
import dragon.*;


import java.io.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private int port;
    private DatagramSocket socket;
    private InetAddress address;
    private DatabaseManager manager;
    private DataForServer command;
    ExecutorService fixedPool = Executors.newFixedThreadPool(2);


    public Server(int port, DatabaseManager manager) {
        this.port = port;
        this.manager = manager;
    }

    public void run(DragonCollection dragonCollection, DatabaseManager manager) {
        try {
            //while (true) {
                socket = new DatagramSocket(this.port);
                Exchanger<DataForServer> exchanger = new Exchanger<>();
                Exchanger<DataForClient> messageExchanger = new Exchanger<>();
                new ServerForCommunication(port, socket, command, dragonCollection, exchanger, messageExchanger).start();
                fixedPool.submit(new ServerForExecution(port, socket, manager, dragonCollection, exchanger, messageExchanger));
            //}
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
