package server;

import data.DataForClient;
import data.DataForServer;
import dragon.DragonCollection;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerForCommunication extends Thread{
    private int port;
    private InetAddress address;
    private DatagramSocket socket;
    private DataForServer command;
    private DataForClient message;
    private DragonCollection dragonCollection;
    private Exchanger<DataForServer> commandExchanger;
    private Exchanger<DataForClient> messageExchanger;


    ServerForCommunication(int port, DatagramSocket socket, DataForServer command, DragonCollection dragonCollection, Exchanger<DataForServer> commandExchanger, Exchanger<DataForClient> messageExchanger) {
        this.port = port;
        this.socket = socket;
        this.command = command;
        this.dragonCollection = dragonCollection;
        this.commandExchanger = commandExchanger;
        this.messageExchanger = messageExchanger;
    }

    public void run() {
        int i = 0;
        while (true) {
            try {
                command = getCommand();
                commandExchanger.exchange(command);
                System.out.println("The command '"+ command.getCommandName() +  "' was received");
                message = messageExchanger.exchange(null);
                sendMessage(message);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private DataForServer getCommand() throws IOException, ClassNotFoundException {
        byte[] getBuffer = new byte[socket.getReceiveBufferSize()];
        DatagramPacket getPacket = new DatagramPacket(getBuffer, getBuffer.length);
        socket.receive(getPacket);
        port = getPacket.getPort();
        address = getPacket.getAddress();
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

    private void sendMessage(DataForClient message) throws IOException {
        byte[] sendBuffer = serialize(message);
        DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, address, port);
        socket.send(sendPacket);
    }

    private byte[] serialize(DataForClient message) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(message);
        byte[] buffer = byteArrayOutputStream.toByteArray();
        objectOutputStream.flush();
        byteArrayOutputStream.flush();
        byteArrayOutputStream.close();
        objectOutputStream.close();
        return buffer;
    }
}
