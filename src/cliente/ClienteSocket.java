package cliente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Conexión de la aplicación cliente con el servidor (clase ClienteSocket del diagrama).
 * Hay UNA sola conexión por aplicación: la crea ClienteMain y la usan todas las ventanas.
 *
 * enviarSolicitud() siempre devuelve una línea del protocolo ("OK|..." o "ERROR|...").
 * Si se pierde la conexión no lanza excepciones: devuelve
 * "ERROR|Se perdió la conexión con el servidor", así las ventanas tratan
 * ese caso igual que cualquier otro error.
 */
public class ClienteSocket {

    // [AGREGADO] Tiempos máximos de espera (en milisegundos), para que la ventana
    // no se quede congelada si el servidor no contesta.
    private static final int TIEMPO_CONEXION = 3000;
    private static final int TIEMPO_RESPUESTA = 10000;

    private String host;
    private int puerto;
    private Socket socket;
    private BufferedReader entrada;
    private PrintWriter salida;

    public ClienteSocket(String host, int puerto) {
        this.host = host;
        this.puerto = puerto;
    }

    /**
     * Abre el socket TCP con el servidor y prepara los flujos de texto.
     * Devuelve true si se conectó y false si el servidor no está disponible.
     */
    public boolean conectar() {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, puerto), TIEMPO_CONEXION);
            socket.setSoTimeout(TIEMPO_RESPUESTA);   // readLine() espera como máximo 10 s

            // Mismos flujos que en ManejadorCliente, en UTF-8 para las tildes
            entrada = new BufferedReader(new InputStreamReader(
                    socket.getInputStream(), StandardCharsets.UTF_8));
            salida = new PrintWriter(new OutputStreamWriter(
                    socket.getOutputStream(), StandardCharsets.UTF_8), true);
            return true;
        } catch (IOException ex) {
            // Por ejemplo, el servidor no está encendido (Connection refused)
            System.err.println("No se pudo conectar con " + host + ":" + puerto + ": " + ex.getMessage());
            socket = null;
            return false;
        }
    }

    /**
     * Envía una solicitud (una línea) y espera la respuesta del servidor (otra línea).
     * Es synchronized para que dos solicitudes nunca se mezclen en el mismo socket.
     */
    public synchronized String enviarSolicitud(String solicitud) {
        if (socket == null || socket.isClosed()) {
            return "ERROR|No hay conexión con el servidor";
        }
        try {
            salida.println(solicitud);
            if (salida.checkError()) {   // PrintWriter no lanza excepciones; hay que preguntarle
                throw new IOException("no se pudo enviar el mensaje");
            }
            String respuesta = entrada.readLine();
            if (respuesta == null) {     // el servidor cerró la conexión
                throw new IOException("el servidor cerró la conexión");
            }
            return respuesta;
        } catch (IOException ex) {
            // También llega aquí si pasan 10 s sin respuesta (SocketTimeoutException)
            System.err.println("Error de comunicación: " + ex.getMessage());
            cerrarSocket();
            return "ERROR|Se perdió la conexión con el servidor";
        }
    }

    /**
     * Avisa al servidor con SALIR y cierra el socket.
     * Se llama al presionar "Salir" o al cerrar la ventana.
     */
    public void desconectar() {
        if (socket != null && !socket.isClosed()) {
            enviarSolicitud("SALIR");   // el servidor responde "OK|Hasta luego"
        }
        cerrarSocket();
    }

    /**
     * [AGREGADO] Privado. Cierra el socket sin enviar nada; al cerrar el socket
     * también se cierran sus flujos. Lo usan desconectar() y enviarSolicitud().
     */
    private void cerrarSocket() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ex) {
            System.err.println("Error al cerrar la conexión: " + ex.getMessage());
        }
    }
}
