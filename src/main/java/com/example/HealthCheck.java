package com.example;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class HealthCheck {
    private Long tiempoMax=  50l;
    private static String urlSistema="25.5.211.175";
    public static void main(String[] args) {
        ZContext zContext= new ZContext();
        ZMQ.Socket socketLatido = zContext.createSocket(SocketType.REP);
        socketLatido.bind("tcp://25.5.211.175:5570");
        try{
            while (!Thread.currentThread().isInterrupted()) {
                byte[] request = socketLatido.recv(0);
                String mensaje = new String(request, ZMQ.CHARSET);
                System.out.println("Mensaje recibido: " + mensaje);

                // Verificamos el tipo de mensaje
                if (mensaje.equals("conectarme")) {
                    // Obtén la dirección IP del cliente
                    String clientIpAddress = socketLatido.getLastEndpoint().substring(6);  // Eliminamos el prefijo "tcp://"
                    System.out.println("Dirección IP del cliente: " + clientIpAddress);

                    // Enviamos la dirección IP al cliente
                    socketLatido.send(clientIpAddress.getBytes(ZMQ.CHARSET), 0);
                } else if (mensaje.startsWith("Vivo ")) {
                    // Extraemos la dirección IP del mensaje
                    String[] partes = mensaje.split(" ");
                    if (partes.length == 3) {
                        String ipRecibida = partes[2];
                        System.out.println("Vivo + IP recibida: " + ipRecibida);
                        // Aquí puedes realizar cualquier acción adicional con la IP recibida
                    } else {
                        System.out.println("Formato incorrecto para mensaje 'Vivo'");
                    }
                } else {
                    System.out.println("Mensaje no reconocido");
                }
            }
        }finally{
            socketLatido.close();
            zContext.close();
        }
    }
    public void clientConection(){
        
    }
}
