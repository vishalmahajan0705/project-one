package com.project.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import static com.project.Constants.*;

/*
Send and receive messages. Message recipient overrides processMessage method.
 */
public abstract class AbstractMulticastNode {


    protected abstract void processMessage(String message);

    public void send(String message) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        InetAddress group = InetAddress.getByName(com.project.Constants.HOST);
        byte[] msg = message.getBytes();
        DatagramPacket packet = new DatagramPacket(msg, msg.length,
                group, PORT);
        socket.send(packet);
        socket.close();
   //     System.out.println(message);

    }



    public void receive() throws
            IOException{
        receive(HOST,PORT);
    }

    private void receive(String ip, int port) {

        Thread t = new Thread(() -> {
            try {
                byte[] buffer = new byte[1024*2];
                MulticastSocket socket = new MulticastSocket(PORT);
                InetAddress group = InetAddress.getByName(HOST);
                socket.joinGroup(group);
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer,
                            buffer.length);
                    socket.receive(packet);
                    String msg = new String(packet.getData(),
                            packet.getOffset(), packet.getLength());
                    processMessage(msg);
                    if ("Exit".equals(msg)) {
                        break;
                    }
                }

                socket.leaveGroup(group);
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
        t.start();
    }

}
