package com.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;;

public class Sensor {
    private String tipoSensor;
    private double tiempoEnvio;
    private String archivoConfig;
    private double probValoresCorrectos;
    private double probValoresFueraRango;
    private double probValoresIncorrectos;
    private double valorMinimo;
    private double valorMaximo;
    private String tema;

    public String getTipoSensor() {
        return tipoSensor;
    }

    public void setTipoSensor(String tipoSensor) {
        this.tipoSensor = tipoSensor;
    }

    public double getTiempoEnvio() {
        return tiempoEnvio;
    }

    public void setTiempoEnvio(double tiempoEnvio) {
        this.tiempoEnvio = tiempoEnvio;
    }

    public String getArchivoConfig() {
        return archivoConfig;
    }

    public void setArchivoConfig(String archivoConfig) {
        this.archivoConfig = archivoConfig;
    }

    public double getProbValoresCorrectos() {
        return probValoresCorrectos;
    }

    public void setProbValoresCorrectos(double probValoresCorrectos) {
        this.probValoresCorrectos = probValoresCorrectos;
    }

    public double getProbValoresFueraRango() {
        return probValoresFueraRango;
    }

    public void setProbValoresFueraRango(double probValoresFueraRango) {
        this.probValoresFueraRango = probValoresFueraRango;
    }

    public double getProbValoresIncorrectos() {
        return probValoresIncorrectos;
    }

    public void setProbValoresIncorrectos(double probValoresIncorrectos) {
        this.probValoresIncorrectos = probValoresIncorrectos;
    }

    public double getValorMinimo() {
        return valorMinimo;
    }

    public void setValorMinimo(double valorMinimo) {
        this.valorMinimo = valorMinimo;
    }

    public double getValorMaximo() {
        return valorMaximo;
    }

    public void setValorMaximo(double valorMaximo) {
        this.valorMaximo = valorMaximo;
    }

    public Sensor() {
    }

    public Sensor(String tipoSensor, double tiempoEnvio, String archivoConfig) {
        this.tipoSensor = tipoSensor;
        this.tiempoEnvio = tiempoEnvio;
        this.archivoConfig = archivoConfig;
    }

    public double generarMedida() {
        Random random = new Random();
        double medida = 0;
        double probabilidad = random.nextDouble();
        if (probabilidad < probValoresIncorrectos) {
            medida = -random.nextDouble();
        } else if (probabilidad < probValoresIncorrectos + probValoresCorrectos) {
            medida = valorMinimo + random.nextDouble() * (valorMaximo - valorMinimo);
        } else {
            medida = random.nextDouble() > 0.5 ? valorMaximo + random.nextDouble() * 10
                    : valorMinimo - random.nextDouble() * 10;
        }
        return medida;
    }

    public static void main(String[] args) {
        Sensor sensor = validarArgumentos(args);
        if (sensor != null) {
            String urlSistema="25.5.211.175";
            System.out.println("Tipo de Sensor: " + sensor.getTipoSensor());
            System.out.println("Tiempo de Envio: " + sensor.getTiempoEnvio());
            System.out.println("Archivo de Configuracion: " + sensor.getArchivoConfig());
            System.out.println("minimo: " + sensor.getValorMinimo());
            System.out.println("Maximo: " + sensor.getValorMaximo());
            System.out.println("Tema: "+ sensor.getTema());
            ZContext zContext = new ZContext();
            ZMQ.Socket socket = zContext.createSocket(SocketType.PUSH);
            socket.connect("tcp://"+urlSistema+":5555");
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    LocalDateTime horaActual = LocalDateTime.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    String horaFormateada = horaActual.format(formatter);
                    String mensaje=sensor.getTema()+ " "+horaFormateada+" "+sensor.generarMedida();
                    socket.send(mensaje);
                    System.out.println("Medida Enviada: "+ mensaje );
                    Thread.sleep((long)sensor.getTiempoEnvio()*1000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally{
                socket.close();
                zContext.close();
            }
        }
    }

    private static Sensor validarArgumentos(String[] args) {
        if (args.length != 3) {
            System.err.println(
                    "Uso incorrecto. Debe proporcionar tres argumentos: Tipo de sensor, tiempo de envio y direccion del archivo de configuracion.");
            System.exit(1);
        }
        String tipoDeSensor = args[0];
        String tiempoDeEnvioStr = args[1];
        String archivoDeConfiguracion = args[2];
        Sensor sensor = new Sensor();
        try {
            double tiempoDeEnvio = Double.parseDouble(tiempoDeEnvioStr);
            if (!ReadConfiguration.validarTipoDeSensor(tipoDeSensor)) {
                System.err.println("Tipo de sensor no valido. Debe ser uno de los tipos validos.");
                System.exit(1);
            }

            sensor = new Sensor(tipoDeSensor, tiempoDeEnvio, archivoDeConfiguracion);
            ReadConfiguration.leerArchivo("src\\main\\resources\\" + archivoDeConfiguracion + ".json", sensor);
        } catch (NumberFormatException e) {
            System.err.println("Tiempo de envio no valido. Debe ser un numero entero.");
            System.exit(1);
        }
        return sensor;
    }

    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }
}
