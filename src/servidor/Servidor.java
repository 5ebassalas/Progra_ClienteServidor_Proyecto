package servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Abre el puerto, espera clientes y crea un ManejadorCliente por cada uno
 * (clase Servidor del diagrama).
 */
public class Servidor {

    private int puerto;
    private ServerSocket serverSocket;
    private GestorDatos gestorDatos;   // un solo GestorDatos para todos los hilos

    public Servidor(int puerto) {
        this.puerto = puerto;
        this.gestorDatos = new GestorDatos();
    }

    /**
     * Carga los datos, abre el ServerSocket y se queda esperando clientes.
     * Por cada cliente crea un hilo nuevo, así atiende a varios a la vez (HU-10).
     */
    public void iniciar() {
        gestorDatos.cargarDatos();
        try {
            serverSocket = new ServerSocket(puerto);
            System.out.println("Servidor Fidness escuchando en el puerto " + puerto + "...");

            int numeroCliente = 0;
            while (true) {
                // accept() espera aquí hasta que un cliente se conecta
                Socket socketCliente = serverSocket.accept();
                numeroCliente++;

                // Composición: el Servidor crea cada ManejadorCliente
                ManejadorCliente hilo = new ManejadorCliente(socketCliente, gestorDatos);
                hilo.setName("Cliente-" + numeroCliente);
                hilo.start();   // start() crea el hilo nuevo y este ejecuta run()
                // El ciclo vuelve enseguida a accept(): no espera a que el cliente termine
            }
        } catch (IOException ex) {
            if (serverSocket != null && serverSocket.isClosed()) {
                // Se cerró a propósito con detener()
                System.out.println("Servidor detenido.");
            } else {
                // Por ejemplo, el puerto ya lo está usando otro programa
                System.err.println("No se pudo abrir el puerto " + puerto + ": " + ex.getMessage());
            }
        }
    }

    /**
     * Cierra el ServerSocket. accept() lanza una excepción y iniciar() termina.
     */
    public void detener() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ex) {
            System.err.println("Error al detener el servidor: " + ex.getMessage());
        }
    }
}
