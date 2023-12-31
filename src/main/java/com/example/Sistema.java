package com.example;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class Sistema {
    public static void main(String[] args){
        String urlSistema= "25.5.211.175";
        ZContext zContext= new ZContext();
        ZMQ.Socket socketPublicar = zContext.createSocket(SocketType.PUB);
        socketPublicar.bind("tcp://"+urlSistema+":5556");
        ZMQ.Socket socketRecibir = zContext.createSocket(SocketType.PULL);
        socketRecibir.bind("tcp://"+ urlSistema +":5555");
        
        try {
            while (!Thread.currentThread().isInterrupted()) {
                System.out.println("Esperando mensaje...");
                byte[] mensaje =socketRecibir.recv();
                String mensajeString= new String(mensaje, ZMQ.CHARSET);
                System.out.println(mensajeString);
                socketPublicar.send(mensajeString);
            }
        } finally {
            socketPublicar.close();
            socketRecibir.close();
            zContext.close();
        }
    }
}
