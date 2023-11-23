package com.example;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class SistemaCalidad {
    public static void main(String[] args) {
        String urlSistema= "25.5.211.175";
        ZContext zContext= new ZContext();
        ZMQ.Socket socketRecibir = zContext.createSocket(SocketType.PULL);
        socketRecibir.bind("tcp://"+urlSistema+":5557");

        try{
            while (!Thread.currentThread().isInterrupted()) {
                System.out.println("Esperando Mensajes...");
                byte[] mensaje = socketRecibir.recv();
                String mensakeString = new String(mensaje, ZMQ.CHARSET);
                System.out.println("Valor erronero por parte de:\n"+mensakeString);
            }
        }finally{
            socketRecibir.close();
            zContext.close();
        }
    }
}
