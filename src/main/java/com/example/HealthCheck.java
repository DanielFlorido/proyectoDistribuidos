package com.example;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class HealthCheck {
    private static String urlSistema = "25.5.211.175";
    private Map<String, Timer> monitorTimers = new HashMap<>();

    public static void main(String[] args) {
        HealthCheck healthCheck = new HealthCheck();
        healthCheck.startHealthCheck();
    }

    public void startHealthCheck() {
        ZContext zContext = new ZContext();
        ZMQ.Socket socketLatido = zContext.createSocket(SocketType.REP);
        socketLatido.bind("tcp://" + urlSistema + ":5558");
        System.out.println("Creando Healthcare");

        try {
            while (!Thread.currentThread().isInterrupted()) {
                byte[] request = socketLatido.recv(0);
                String mensaje = new String(request, ZMQ.CHARSET);
                System.out.println("Mensaje recibido: " + mensaje);

                // Verificamos el tipo de mensaje
                if (mensaje.startsWith("conectarme")) {
                    // Obtén la dirección IP del cliente
                    String clientIpAddress = socketLatido.getLastEndpoint().substring(6);  // Eliminamos el prefijo "tcp://"
                    System.out.println("Dirección IP del cliente: " + clientIpAddress);
                    
                    // Enviamos la dirección IP al cliente
                    socketLatido.send(clientIpAddress.getBytes(ZMQ.CHARSET), 0);
                    String tipoSensor = obtenerTipoSensor(mensaje);
                    // Iniciamos un temporizador para este monitor
                    iniciarTemporizador(clientIpAddress, tipoSensor);
                } else if (mensaje.startsWith("Vivo")) {
                    // Procesar mensaje 'Vivo'
                    procesarMensajeVivo(mensaje, socketLatido);
                } else {
                    System.out.println("Mensaje no reconocido");
                }
            }
        } finally {
            socketLatido.close();
            zContext.close();
        }
    }
    private String obtenerTipoSensor(String mensaje) {    
        String[] partes = mensaje.split(" ");
        if (partes.length == 2) {
            return partes[1];
        } else {
            return "TipoDesconocido"; // O algún valor por defecto o indicador de error
        }
    }
    private void procesarMensajeVivo(String mensaje, Socket socketLatido) {
        // Extraemos la dirección IP del mensaje
        String[] partes = mensaje.split(" ");
        if (partes.length == 2) {
            String comando = partes[0];
            String ipRecibida = partes[1];
            String msjString = comando + " IP recibida: " + ipRecibida;
            System.out.println(msjString);
            socketLatido.send(msjString.getBytes(ZMQ.CHARSET));
            // Aquí puedes realizar cualquier acción adicional con la IP recibida
        } else {
            System.out.println("Formato incorrecto para mensaje 'Vivo'");
        }
    }

    private void iniciarTemporizador(String ip, String tipoSensor) {
        Timer timer = new Timer(true); // true indica que el temporizador es de fondo
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Acciones a realizar cuando el temporizador expire
                System.out.println("Temporizador para " + ip + " y tipo sensor "+tipoSensor+" ha expirado.");
                // Puedes agregar aquí cualquier acción que desees realizar
                crearNuevoMonitor(tipoSensor);
            }
        }, 20000); // 10000 milisegundos (30 segundos)
        
        // Almacenamos el temporizador asociado a la dirección IP del monitor
        monitorTimers.put(ip, timer);
    }
    private void crearNuevoMonitor(String tipoSensor) {
         try {
            // Ruta del comando PowerShell
            String comandoPowerShell = "C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe";

            // Comando que deseas ejecutar en la nueva ventana de PowerShell
            String comando = "Start-Process 'C:\\Program Files\\Eclipse Adoptium\\jdk-21.0.1.12-hotspot\\bin\\java.exe' " +
                 "-ArgumentList \"@C:\\Users\\ESTUDI~1\\AppData\\Local\\Temp\\3\\cp_d380cbv3dlngijkvzjio2zhjh.argfile\", " +
                 "\"com.example.Monitor\", \""+tipoSensor+"\" " +
                 "-WorkingDirectory 'C:\\Users\\estudiante\\Documents\\proyectoDistribuidos'";

            // Crear el proceso
            ProcessBuilder builder = new ProcessBuilder(comandoPowerShell, "-Command", comando);
            builder.redirectErrorStream(true); // Redirigir la salida de error estándar al flujo de entrada estándar
            Process proceso = builder.start();

            // Leer la salida estándar y de error
            try (InputStream inputStream = proceso.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String linea;
                while ((linea = reader.readLine()) != null) {
                    System.out.println(linea);
                }
            }

            // Esperar a que el proceso termine
            int resultado = proceso.waitFor();

            // Imprimir el resultado
            System.out.println("El proceso terminó con resultado: " + resultado);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
         
    
}
