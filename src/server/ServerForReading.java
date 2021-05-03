package server;

import data.DataForServer;
import dragon.DragonCollection;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerForReading extends Thread{
    private int port;
    private InetAddress address;
    private DatagramSocket socket;
    private DatabaseManager manager;
    private DataForServer command;
    private DragonCollection dragonCollection;
    ExecutorService fixedPool = Executors.newFixedThreadPool(2);

    ServerForReading(int port, InetAddress address, DatagramSocket socket, DatabaseManager manager, DataForServer command, DragonCollection dragonCollection) {
        this.port = port;
        this.address = address;
        this.socket = socket;
        this.manager = manager;
        this.command = command;
        this.dragonCollection = dragonCollection;
    }

    public void run() {
        while (true) {
            try {
                command = getCommand();
                System.out.println("The command '"+ command.getCommandName() +  "' was received");
                fixedPool.submit(new ServerForExecution(port, address, socket, manager, command, dragonCollection));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private DataForServer getCommand() throws IOException, ClassNotFoundException {
        byte[] getBuffer = new byte[socket.getReceiveBufferSize()];
        DatagramPacket getPacket = new DatagramPacket(getBuffer, getBuffer.length);
        socket.receive(getPacket);
        address = getPacket.getAddress();
        port = getPacket.getPort();
        return deserialize(getPacket, getBuffer);
    }

    private DataForServer deserialize(DatagramPacket getPacket, byte[] buffer) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(getPacket.getData());
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        DataForServer command = (DataForServer) objectInputStream.readObject();
        byteArrayInputStream.close();
        objectInputStream.close();
        return command;
    }
}
