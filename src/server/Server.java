package server;

import data.*;
import dragon.*;


import java.io.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.channels.DatagramChannel;

public class Server {
    private int port;
    private DatagramSocket socket;
    private InetAddress address;
    private DatabaseManager manager;
    private DataForServer command;


    public Server(int port, DatabaseManager manager) {
        this.port = port;
        this.manager = manager;
    }

    public void run(DragonCollection dragonCollection, DatabaseManager manager) {
        try {
            //while (true) {
                socket = new DatagramSocket(this.port);
                new ServerForReading(port, address, socket, manager, command, dragonCollection).start();
                socket = null;
            //}
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*private static DatagramSocket accept(int port) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(port);
            System.out.println("Client connected");
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
        return socket;
    }*/
}
