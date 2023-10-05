package com.example;

import java.io.FileReader;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ReadConfiguration {
    public static void leerArchivo(String nombreArchivoString, Sensor sensor){
        try (FileReader reader= new FileReader(nombreArchivoString)){
            Gson gson= new Gson();
            JsonObject jsonObject= gson.fromJson(reader, JsonObject.class);
            if (sensor!= null) {
                sensor.setProbValoresCorrectos(jsonObject.get("probabilidadNormal").getAsDouble());
                sensor.setProbValoresIncorrectos(jsonObject.get("probabilidadFallo").getAsDouble());
                sensor.setProbValoresFueraRango(jsonObject.get("probabilidadFueraDeRango").getAsDouble());
                JsonArray tiposSensores= jsonObject.getAsJsonArray("sensores");
                for (int i = 0; i < tiposSensores.size(); i++) {
                    JsonObject sensorTipo = tiposSensores.get(i).getAsJsonObject();
                    if(sensorTipo.get("tipo").getAsString().equalsIgnoreCase(sensor.getTipoSensor())){
                        sensor.setValorMaximo(sensorTipo.get("minimo").getAsDouble());
                        sensor.setValorMinimo(sensorTipo.get("maximo").getAsDouble());
                        sensor.setTema(sensorTipo.get("tema").getAsString());
                    }
                }
            } else {
                throw new RuntimeException("el objeto sensorno puede ser nulo!");
            }        
        } catch (Exception e) {
            e.printStackTrace();
            String curreString= System.getProperty("user.dir");
            System.out.println(curreString);
            System.err.println("Se produjo una excepcion: "+ e.getMessage());
        }
    }
    public static void leerArchivo(String nombreArchivoString, Monitor monitor){
        try (FileReader reader= new FileReader(nombreArchivoString)){
            Gson gson = new Gson();
            JsonObject jsonObject= gson.fromJson(reader, JsonObject.class);
            if(monitor!=null){
                JsonArray tipoJsonArray= jsonObject.getAsJsonArray("sensores");
                for (int i = 0; i < tipoJsonArray.size(); i++) {
                    JsonObject sensortipo= tipoJsonArray.get(i).getAsJsonObject();
                    if(sensortipo.get("tipo").getAsString().equalsIgnoreCase(monitor.getTipoSensor())){
                        monitor.setMinimo(sensortipo.get("minimo").getAsDouble());
                        monitor.setMaximo(sensortipo.get("maximo").getAsDouble());
                        monitor.setTema(sensortipo.get("tema").getAsString());
                    }
                }
            }
            else{
                throw new RuntimeException("El objeto monitor no puede ser nulo!");
            }
        } catch (Exception e) {
            e.printStackTrace();    
        }
    }
    public static boolean validarTipoDeSensor(String tipoDeSensor) {
        return tipoDeSensor.equalsIgnoreCase("Temperatura") || tipoDeSensor.equalsIgnoreCase("PH")
                || tipoDeSensor.equalsIgnoreCase("Oxigeno");
    }
}
