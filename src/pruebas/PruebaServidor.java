package pruebas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

// [AGREGADO - TEMPORAL] Prueba de la Etapa 3. No es parte del diagrama.
// Primero ejecute servidor/ServidorMain.java y DESPUÉS esta clase.
// Abre dos conexiones al mismo tiempo (cliente1 y admin) y envía mensajes del protocolo.
public class PruebaServidor {

    public static void main(String[] args) {
        try (Socket socketA = new Socket("localhost", 5000);
             Socket socketB = new Socket("localhost", 5000)) {

            BufferedReader entradaA = new BufferedReader(new InputStreamReader(socketA.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter salidaA = new PrintWriter(new OutputStreamWriter(socketA.getOutputStream(), StandardCharsets.UTF_8), true);
            BufferedReader entradaB = new BufferedReader(new InputStreamReader(socketB.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter salidaB = new PrintWriter(new OutputStreamWriter(socketB.getOutputStream(), StandardCharsets.UTF_8), true);

            System.out.println("--- HU-01: iniciar sesión ---");
            enviar("A", salidaA, entradaA, "LISTAR_EJERCICIOS|TODOS");          // sin sesión -> ERROR
            enviar("A", salidaA, entradaA, "LOGIN|cliente1|mala");              // ERROR
            enviar("A", salidaA, entradaA, "LOGIN|cliente1|cliente123");        // OK
            enviar("B", salidaB, entradaB, "LOGIN|admin|admin123");             // OK (otro hilo)

            System.out.println("\n--- HU-03, HU-04, HU-05: consultar ejercicios ---");
            enviar("A", salidaA, entradaA, "LISTAR_EJERCICIOS|TODOS");
            enviar("A", salidaA, entradaA, "LISTAR_EJERCICIOS|PIERNA");
            enviar("A", salidaA, entradaA, "DETALLE_EJERCICIO|1");

            System.out.println("\n--- HU-02: registrar ejercicio ---");
            enviar("A", salidaA, entradaA, "REGISTRAR_EJERCICIO|Plancha|ABDOMEN|Para el abdomen.|1. Apoya los antebrazos.");  // cliente -> ERROR
            enviar("B", salidaB, entradaB, "REGISTRAR_EJERCICIO|Plancha|ABDOMEN||1. Apoya los antebrazos.");                  // falta descripción -> ERROR
            enviar("B", salidaB, entradaB, "REGISTRAR_EJERCICIO|Plancha|ABDOMEN|Ejercicio para fortalecer el abdomen."
                    + "|1. Apoya los antebrazos y las puntas de los pies.\\n2. Mantén el cuerpo recto 30 segundos.");          // OK
            enviar("A", salidaA, entradaA, "LISTAR_EJERCICIOS|ABDOMEN");        // cliente1 ya ve la Plancha

            System.out.println("\n--- HU-06, HU-07, HU-08: rutinas ---");
            enviar("A", salidaA, entradaA, "GUARDAR_RUTINA|Rutina de lunes|");          // sin ejercicios -> ERROR
            enviar("A", salidaA, entradaA, "GUARDAR_RUTINA|Rutina de lunes|1,5,4");     // OK
            enviar("A", salidaA, entradaA, "LISTAR_RUTINAS");                          // la de cliente1
            enviar("B", salidaB, entradaB, "LISTAR_RUTINAS");                          // admin no la ve

            System.out.println("\n--- HU-10: el cliente A se desconecta de golpe (sin SALIR) ---");
            socketA.close();
            enviar("B", salidaB, entradaB, "LISTAR_EJERCICIOS|BRAZO");          // B sigue funcionando
            enviar("B", salidaB, entradaB, "SALIR");

        } catch (IOException ex) {
            System.out.println("No se pudo conectar. ¿Ejecutó primero ServidorMain? " + ex.getMessage());
        }
    }

    // Envía una línea y muestra la respuesta del servidor
    private static void enviar(String quien, PrintWriter salida, BufferedReader entrada, String mensaje) throws IOException {
        salida.println(mensaje);
        String respuesta = entrada.readLine();
        System.out.println(quien + " >> " + mensaje);
        System.out.println(quien + " << " + respuesta);
    }
}
