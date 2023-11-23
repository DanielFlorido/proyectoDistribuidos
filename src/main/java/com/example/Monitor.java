package com.example;
import java.io.FileWriter;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZContext;

import com.opencsv.CSVWriter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Enumeration;
public class Monitor {
    private String tipoSensor;
    private String tema;
    private double minimo;
    private double maximo;
    private Connection connection;
    private static String urlSistema="25.5.211.175";
    public Monitor() {
        try {
            String url = "jdbc:mysql://25.5.211.175:3306/distri"; // Replace with your database URL
            String username = "user"; // Replace with your database username
            String password = "123456"; // Replace with your database password

            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
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
        socket.connect("tcp://"+ urlSistema +":5556");
        socket.subscribe(monitor.getTema());
        ZMQ.Socket socketCalidad= zContext.createSocket(SocketType.PUSH);
        socketCalidad.connect("tcp://"+urlSistema +":5557");
        ZMQ.Socket socketLatido = zContext.createSocket(SocketType.REP);
        socketLatido.connect("tcp://"+urlSistema+":5558");
        monitor.crearConexion(socketLatido);
        try {
            while (!Thread.currentThread().isInterrupted()) {
                byte[] mensaje= socket.recv();
                String llegada= new String(mensaje, ZMQ.CHARSET);
                System.out.println("Mensaje recibido: "+ llegada);
                monitor.comprobarMedida(llegada, socketCalidad);
            }
        } finally{
            socket.close();
            socketCalidad.close();
            socketLatido.close();
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
    public double comprobarMedida(String mensaje, Socket socketCalidad){
        System.out.println("Partiendo Mensaje...");
        String mensaje2= mensaje;
        String[] split= mensaje.split(" ");
        if (split.length >= 3) {
            String tema = split[0];      
            String fecha = split[1];   
            String hora = split[2];
            String medidaStr = split[3]; 
        
            double medida = Double.parseDouble(medidaStr);
        
            System.out.println("Tema: " + tema);
            System.out.println("Fecha: " + fecha);
            System.out.println("Medida: " + medida);
            System.out.println("Hora: "+ hora);
            if(medida>0){
                guardarEnDB(tema, fecha, hora, medida);
            }else{
                socketCalidad.send(mensaje2);
                System.out.println("Valor Erroneo");
            }
            return medida;
        } else {
            System.err.println("Mensaje recibido no tiene el formato esperado.");
            return -1;
        }
    }
    private void guardarEnDB(String tema, String fecha, String hora, Double medida) {
        // Prepare SQL statement to insert data into the 'medidas' table
        String sql = "INSERT INTO medidas (tipo_sensor, fecha, hora, medida) VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            // Set the values for the prepared statement
            statement.setString(1, tema);
            statement.setString(2, fecha);
            statement.setString(3, hora);
            statement.setDouble(4, medida);

            // Execute the prepared statement to insert the data
            statement.executeUpdate();
            System.out.println("Datos guardados en la base de datos.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void guardar(String tema, String fecha, String hora, Double medida){
        try (CSVWriter writer = new CSVWriter(new FileWriter("src\\main\\resources\\datos.csv", true))) {
            // Crear un array de strings con los datos
            String[] datos = {tema, fecha, hora, Double.toString(medida)};

            //writer.writeNext(new String[]{"Tema", "Fecha","Hora", "Medida"});

            writer.writeNext(datos);

            System.out.println("Datos guardados en " + "datos.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void enviarLatido(Socket socketLatido){
        socketLatido.send("Estoy vivo!");
    }
    public String crearConexion(Socket socketLatido){
        socketLatido.send("conectarme".getBytes(ZMQ.CHARSET), 0);
        byte[] response = socketLatido.recv(0);
        String serverIpAddress = new String(response, ZMQ.CHARSET);
        System.out.println("Dirección IP del servidor recibida: " + serverIpAddress);
        return serverIpAddress;
    }
    
}

