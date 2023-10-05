package com.example;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZContext;

public class Monitor {
    private String tipoSensor;
    private String tema;
    private double minimo;
    private double maximo;
    
    public String getTipoSensor() {
        return tipoSensor;
    }
    public void setTipoSensor(String tipoSensor) {
        this.tipoSensor = tipoSensor;
    }
    public double getMinimo() {
        return minimo;
    }
    public void setMinimo(double minimo) {
        this.minimo = minimo;
    }
    public double getMaximo() {
        return maximo;
    }
    public void setMaximo(double maximo) {
        this.maximo = maximo;
    }
    public String getTema() {
        return tema;
    }
    public void setTema(String tema) {
        this.tema = tema;
    }
    public static void main(String[] args){
        System.out.println("Creando monitor");
        Monitor monitor= comprobarArgs(args);
        ZContext zContext= new ZContext();
        ZMQ.Socket socket= zContext.createSocket(SocketType.SUB);
        socket.connect("tcp://localhost:5556");
        socket.subscribe(monitor.getTema());
        try {
            while (!Thread.currentThread().isInterrupted()) {
                byte[] mensaje= socket.recv();
                String llegada= new String(mensaje, ZMQ.CHARSET);
                System.out.println("Mensaje recibido: "+ llegada);
                monitor.comprobarMedida(llegada);
            }
        } finally{
            socket.close();
            zContext.close();
        }
        
    }
    public static Monitor comprobarArgs(String[] args){
        if (args.length != 1) {
            System.err.println(
                    "Uso incorrecto. Debe proporcionar un argumentos: Tipo de sensor.");
            System.exit(1);
        }
        Monitor monitor= new Monitor();
        if (!ReadConfiguration.validarTipoDeSensor(args[0])) {
            System.err.println("Tipo de sensor no valido. Debe ser uno de los tipos validos.");
            System.exit(1);
        }
        monitor.setTipoSensor(args[0]);
        ReadConfiguration.leerArchivo("src\\main\\resources\\config.json", monitor);
        return monitor;
    }
    public double comprobarMedida(String mensaje){
        System.out.println("Partiendo Mensaje...");
        String[] split= mensaje.split(" ");
        if (split.length >= 3) {
            String tema = split[0];      
            String fecha = split[1];    
            String medidaStr = split[2]; 
        
            double medida = Double.parseDouble(medidaStr);
        
            System.out.println("Tema: " + tema);
            System.out.println("Fecha: " + fecha);
            System.out.println("Medida: " + medida);
            return medida;
        } else {
            System.err.println("Mensaje recibido no tiene el formato esperado.");
            return -1;
        }
    }
}