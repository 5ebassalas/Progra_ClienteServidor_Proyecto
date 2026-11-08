package pruebas;

import cliente.ClienteSocket;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// [AGREGADO] Prueba de la Etapa 6 (HU-10). No es parte del diagrama.
// Primero ejecute servidor/ServidorMain.java y DESPUÉS esta clase.
// Lanza varios clientes al mismo tiempo, cada uno en su propio hilo, y revisa que:
//   1) todos reciban la lista de ejercicios completa,
//   2) un cliente que se cae de golpe no afecte a los demás,
//   3) las rutinas guardadas al mismo tiempo no repitan id (GestorDatos synchronized).
public class PruebaVariosClientes {

    private static final int CLIENTES = 5;           // clientes al mismo tiempo
    private static final int SOLICITUDES = 20;       // listas que pide cada uno

    // Contadores compartidos por todos los hilos de la prueba
    private static int respuestasCompletas = 0;
    private static int respuestasIncompletas = 0;
    private static int rutinasGuardadas = 0;

    // synchronized: dos hilos no pueden sumar al mismo tiempo
    private static synchronized void contarRespuesta(boolean completa) {
        if (completa) {
            respuestasCompletas++;
        } else {
            respuestasIncompletas++;
        }
    }

    private static synchronized void contarRutina() {
        rutinasGuardadas++;
    }

    public static void main(String[] args) throws InterruptedException {

        // ---- 0. Cuántos ejercicios hay ahora (para comparar) ----
        ClienteSocket referencia = new ClienteSocket("localhost", 5000);
        if (!referencia.conectar()) {
            System.out.println("No se pudo conectar. ¿Ejecutó primero ServidorMain?");
            return;
        }
        referencia.enviarSolicitud("LOGIN|cliente1|cliente123");
        String lista = referencia.enviarSolicitud("LISTAR_EJERCICIOS|TODOS");
        int esperados = lista.substring(3).split("#").length;
        referencia.desconectar();
        System.out.println("Ejercicios en el servidor: " + esperados);

        // ---- 1. Crear un hilo por cliente ----
        List<Thread> hilos = new ArrayList<>();
        for (int i = 1; i <= CLIENTES; i++) {
            final int numero = i;
            hilos.add(new Thread(() -> trabajar(numero, esperados), "Prueba-" + numero));
        }
        // Un cliente extra que se conecta y se cae de golpe (sin enviar SALIR)
        Thread clienteQueSeCae = new Thread(PruebaVariosClientes::caerseDeGolpe, "Prueba-caida");

        // ---- 2. Arrancarlos todos casi al mismo tiempo ----
        long inicio = System.currentTimeMillis();
        for (Thread hilo : hilos) {
            hilo.start();
        }
        clienteQueSeCae.start();

        // join() espera a que cada hilo termine
        for (Thread hilo : hilos) {
            hilo.join();
        }
        clienteQueSeCae.join();
        long duracion = System.currentTimeMillis() - inicio;

        // ---- 3. Revisar que las rutinas no repitan id ----
        ClienteSocket revision = new ClienteSocket("localhost", 5000);
        revision.conectar();
        revision.enviarSolicitud("LOGIN|cliente1|cliente123");
        String rutinas = revision.enviarSolicitud("LISTAR_RUTINAS");
        revision.desconectar();

        List<String> ids = new ArrayList<>();
        for (String item : rutinas.substring(3).split("#")) {
            ids.add(item.split(";")[0]);
        }
        Set<String> idsSinRepetir = new HashSet<>(ids);   // un Set no guarda repetidos

        // ---- 4. Resultado ----
        System.out.println();
        System.out.println("=========== RESULTADO ===========");
        System.out.println("Clientes al mismo tiempo:  " + CLIENTES + " (+1 que se cayó de golpe)");
        System.out.println("Respuestas completas:      " + respuestasCompletas + " de " + (CLIENTES * SOLICITUDES));
        System.out.println("Respuestas incompletas:    " + respuestasIncompletas);
        System.out.println("Rutinas guardadas a la vez: " + rutinasGuardadas + " de " + CLIENTES);
        System.out.println("Rutinas de cliente1: " + ids.size() + ", ids repetidos: " + (ids.size() - idsSinRepetir.size()));
        System.out.println("Tiempo total: " + duracion + " ms");
        boolean todoBien = respuestasIncompletas == 0
                && respuestasCompletas == CLIENTES * SOLICITUDES
                && rutinasGuardadas == CLIENTES
                && ids.size() == idsSinRepetir.size();
        System.out.println(todoBien ? "HU-10 CUMPLIDA" : "HU-10 CON FALLOS: revise la consola del servidor");
    }

    /**
     * Lo que hace cada cliente: inicia sesión, pide la lista varias veces,
     * guarda una rutina y se desconecta.
     */
    private static void trabajar(int numero, int esperados) {
        ClienteSocket cliente = new ClienteSocket("localhost", 5000);
        if (!cliente.conectar()) {
            contarRespuesta(false);
            return;
        }
        cliente.enviarSolicitud("LOGIN|cliente1|cliente123");

        for (int i = 0; i < SOLICITUDES; i++) {
            String respuesta = cliente.enviarSolicitud("LISTAR_EJERCICIOS|TODOS");
            boolean completa = respuesta.startsWith("OK|")
                    && respuesta.substring(3).split("#").length == esperados;
            contarRespuesta(completa);
        }

        String respuesta = cliente.enviarSolicitud("GUARDAR_RUTINA|Prueba del hilo " + numero + "|1,2");
        if (respuesta.startsWith("OK|")) {
            contarRutina();
        }
        cliente.desconectar();
        System.out.println(Thread.currentThread().getName() + " terminó");
    }

    /**
     * Simula un cliente que cierra la aplicación de golpe:
     * se conecta, inicia sesión y cierra el socket sin enviar SALIR.
     */
    private static void caerseDeGolpe() {
        try (Socket socket = new Socket("localhost", 5000)) {
            // setSoLinger(true, 0) hace que close() corte la conexión de golpe (reset),
            // como cuando la aplicación se cierra a la fuerza
            socket.setSoLinger(true, 0);
            PrintWriter salida = new PrintWriter(new OutputStreamWriter(
                    socket.getOutputStream(), StandardCharsets.UTF_8), true);
            salida.println("LOGIN|admin|admin123");
            Thread.sleep(30);
            // Al salir del try se cierra el socket: el servidor lo nota y solo cierra ESE hilo
        } catch (IOException | InterruptedException ex) {
            System.out.println("Error en el cliente que se cae: " + ex.getMessage());
        }
        System.out.println("Prueba-caida se desconectó de golpe");
    }
}
