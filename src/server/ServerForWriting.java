package server;

import data.DataForClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ServerForWriting extends Thread {
    private int port;
    private InetAddress address;
    private DatagramSocket socket;
    private DataForClient message;

    ServerForWriting(int port, InetAddress address, DatagramSocket socket, DataForClient message) {
        this.port = port;
        this.address = address;
        this.socket = socket;
        this.message = message;
    }

    public void run() {
        try {
            sendMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
