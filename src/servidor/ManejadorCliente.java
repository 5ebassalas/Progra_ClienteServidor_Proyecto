package servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import modelo.Ejercicio;
import modelo.Rutina;
import modelo.TipoEjercicio;
import modelo.Usuario;

/**
 * Hilo que atiende a UN cliente (clase ManejadorCliente del diagrama).
 * Hereda de Thread: el Servidor crea uno por cada cliente que se conecta
 * y llama a start(), que ejecuta run() en un hilo aparte.
 *
 * [AGREGADO] PROTOCOLO DE MENSAJES (lo que el Avance 1 dejó pendiente).
 * Cada solicitud y cada respuesta es UNA línea de texto.
 * Los campos se separan con "|". En las listas, cada elemento se separa con "#"
 * y sus datos con ";". Los saltos de línea de los textos viajan como "\n".
 *
 *   Solicitud                                         Respuesta
 *   LOGIN|usuario|contrasena                          OK|id|nombreCompleto|rol
 *   LISTAR_EJERCICIOS|TODOS  (o PIERNA, BRAZO...)      OK|id;nombre;TIPO#id;nombre;TIPO...
 *   DETALLE_EJERCICIO|id                              OK|id|nombre|TIPO|descripcion|pasos
 *   REGISTRAR_EJERCICIO|nombre|TIPO|descripcion|pasos OK|Ejercicio guardado correctamente
 *   GUARDAR_RUTINA|nombre|1,5,4                        OK|La rutina quedó registrada
 *   LISTAR_RUTINAS                                    OK|id;nombre;1,5,4#...
 *   SALIR                                             OK|Hasta luego
 * Si algo sale mal, la respuesta es: ERROR|mensaje para mostrar al usuario
 */
public class ManejadorCliente extends Thread {

    private Socket socket;
    private BufferedReader entrada;
    private PrintWriter salida;
    private GestorDatos gestorDatos;      // el MISMO objeto para todos los hilos
    private Usuario usuarioActual;        // null hasta que el cliente inicia sesión

    public ManejadorCliente(Socket socket, GestorDatos gestorDatos) {
        this.socket = socket;
        this.gestorDatos = gestorDatos;
    }

    /**
     * Sobrescribe run() de Thread (polimorfismo): es lo que hace este hilo.
     * Lee solicitudes una por una hasta que el cliente envía SALIR o se desconecta.
     */
    @Override
    public void run() {
        System.out.println("[" + getName() + "] Cliente conectado desde " + socket.getInetAddress());
        try {
            // Flujos de texto del socket, en UTF-8 para que viajen bien las tildes
            entrada = new BufferedReader(new InputStreamReader(
                    socket.getInputStream(), StandardCharsets.UTF_8));
            salida = new PrintWriter(new OutputStreamWriter(
                    socket.getOutputStream(), StandardCharsets.UTF_8), true);   // true = envía en cada println

            String solicitud;
            // readLine() devuelve null cuando el cliente cierra la conexión
            while ((solicitud = entrada.readLine()) != null) {
                // Solo se muestra el comando, para no imprimir contraseñas en la consola
                System.out.println("[" + getName() + "] Solicitud: " + solicitud.split("\\|")[0]);
                String respuesta = procesarSolicitud(solicitud);
                salida.println(respuesta);
                if (solicitud.equals("SALIR")) {
                    break;
                }
            }
        } catch (IOException ex) {
            // Por ejemplo, el cliente cerró la ventana de golpe. Solo afecta a ESTE hilo:
            // el servidor y los demás clientes siguen funcionando (HU-10).
            System.out.println("[" + getName() + "] Se perdió la conexión: " + ex.getMessage());
        } finally {
            cerrarConexion();   // se ejecuta siempre, con o sin error
        }
    }

    /**
     * Lee una solicitud, la resuelve con GestorDatos y devuelve la respuesta.
     */
    private String procesarSolicitud(String solicitud) {
        // -1 hace que split conserve los campos vacíos (por ejemplo "LOGIN||")
        String[] p = solicitud.split("\\|", -1);
        String comando = p[0];

        // Antes de iniciar sesión solo se permite LOGIN y SALIR
        if (usuarioActual == null && !comando.equals("LOGIN") && !comando.equals("SALIR")) {
            return "ERROR|Debe iniciar sesión primero";
        }

        try {
            switch (comando) {

                case "LOGIN": {   // HU-01
                    if (p.length != 3 || p[1].trim().isEmpty() || p[2].isEmpty()) {
                        return "ERROR|Debe escribir el usuario y la contraseña";
                    }
                    Usuario u = gestorDatos.validarLogin(p[1].trim(), p[2]);
                    if (u == null) {
                        return "ERROR|Usuario o contraseña incorrectos";
                    }
                    usuarioActual = u;   // la sesión queda guardada en este hilo
                    return "OK|" + u.getId() + "|" + u.getNombreCompleto() + "|" + u.getRol();
                }

                case "LISTAR_EJERCICIOS": {   // HU-03 y HU-04
                    List<Ejercicio> lista;
                    if (p.length < 2 || p[1].equals("TODOS")) {
                        lista = gestorDatos.listarEjercicios();
                    } else {
                        lista = gestorDatos.listarPorTipo(TipoEjercicio.valueOf(p[1]));
                    }
                    // Arma "1;Sentadilla;PIERNA#2;Zancadas;PIERNA"
                    StringBuilder datos = new StringBuilder();
                    for (Ejercicio e : lista) {
                        if (datos.length() > 0) {
                            datos.append("#");
                        }
                        datos.append(e.getId()).append(";").append(e.getNombre())
                             .append(";").append(e.getTipo().name());
                    }
                    return "OK|" + datos;   // si no hay ejercicios queda "OK|"
                }

                case "DETALLE_EJERCICIO": {   // HU-05
                    Ejercicio e = gestorDatos.buscarEjercicio(Integer.parseInt(p[1]));
                    if (e == null) {
                        return "ERROR|No existe el ejercicio";
                    }
                    return "OK|" + e.getId() + "|" + e.getNombre() + "|" + e.getTipo().name()
                            + "|" + e.getDescripcion().replace("\r", "").replace("\n", "\\n")
                            + "|" + e.getPasosEjecucion().replace("\r", "").replace("\n", "\\n");
                }

                case "REGISTRAR_EJERCICIO": {   // HU-02
                    // Se revisa en el servidor también, no solo en la ventana
                    if (!usuarioActual.esAdministrador()) {
                        return "ERROR|Solo el administrador puede registrar ejercicios";
                    }
                    if (p.length != 5) {
                        return "ERROR|Los textos no pueden tener el carácter |";
                    }
                    if (p[1].contains(";") || p[1].contains("#")) {
                        return "ERROR|El nombre no puede tener los caracteres ; ni #";
                    }
                    Ejercicio nuevo = new Ejercicio(0, p[1].trim(), TipoEjercicio.valueOf(p[2]),
                            p[3].replace("\\n", "\n"), p[4].replace("\\n", "\n"));
                    if (gestorDatos.registrarEjercicio(nuevo)) {
                        return "OK|Ejercicio guardado correctamente";
                    }
                    return "ERROR|Todos los campos son obligatorios";
                }

                case "GUARDAR_RUTINA": {   // HU-06 y HU-07
                    if (p.length != 3) {
                        return "ERROR|El nombre no puede tener el carácter |";
                    }
                    if (p[1].contains(";") || p[1].contains("#")) {
                        return "ERROR|El nombre no puede tener los caracteres ; ni #";
                    }
                    if (p[2].isEmpty()) {
                        return "ERROR|La rutina debe tener al menos un ejercicio";
                    }
                    // La rutina queda asociada al usuario de ESTE hilo
                    Rutina rutina = new Rutina(0, p[1].trim(), usuarioActual);
                    for (String idTexto : p[2].split(",")) {
                        Ejercicio e = gestorDatos.buscarEjercicio(Integer.parseInt(idTexto.trim()));
                        if (e == null) {
                            return "ERROR|Uno de los ejercicios ya no existe";
                        }
                        rutina.agregarEjercicio(e);
                    }
                    if (gestorDatos.guardarRutina(rutina)) {
                        return "OK|La rutina quedó registrada";
                    }
                    return "ERROR|La rutina debe tener nombre y al menos un ejercicio";
                }

                case "LISTAR_RUTINAS": {   // HU-08
                    // Solo las del usuario con sesión: los demás clientes no las ven
                    List<Rutina> lista = gestorDatos.listarRutinas(usuarioActual.getId());
                    StringBuilder datos = new StringBuilder();
                    for (Rutina r : lista) {
                        if (datos.length() > 0) {
                            datos.append("#");
                        }
                        datos.append(r.getId()).append(";").append(r.getNombre()).append(";");
                        for (int i = 0; i < r.getEjercicios().size(); i++) {
                            if (i > 0) {
                                datos.append(",");
                            }
                            datos.append(r.getEjercicios().get(i).getId());
                        }
                    }
                    return "OK|" + datos;
                }

                case "SALIR":
                    return "OK|Hasta luego";

                default:
                    return "ERROR|Solicitud no reconocida: " + comando;
            }
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException ex) {
            // Un número o un tipo inválido, o faltan campos.
            // (NumberFormatException es hija de IllegalArgumentException.)
            return "ERROR|La solicitud tiene datos inválidos";
        }
    }

    /**
     * Cierra el socket del cliente. Al cerrar el socket también se cierran
     * sus flujos de entrada y salida.
     */
    private void cerrarConexion() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ex) {
            System.out.println("[" + getName() + "] Error al cerrar: " + ex.getMessage());
        }
        String quien = (usuarioActual != null) ? usuarioActual.getNombreUsuario() : "sin sesión";
        System.out.println("[" + getName() + "] Conexión cerrada (" + quien + ")");
    }
}
