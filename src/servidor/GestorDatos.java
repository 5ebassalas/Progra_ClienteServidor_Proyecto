package servidor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import modelo.Ejercicio;
import modelo.Rutina;
import modelo.TipoEjercicio;
import modelo.Usuario;

/**
 * Guarda y busca usuarios, ejercicios y rutinas (clase GestorDatos del diagrama).
 * Hay UN solo objeto GestorDatos y lo comparten todos los hilos ManejadorCliente,
 * por eso todos sus métodos públicos son synchronized.
 *
 * Los datos se guardan en tres archivos de texto dentro de la carpeta "datos",
 * una línea por objeto y los campos separados con "|":
 *   usuarios.txt   -> id|nombreUsuario|contrasena|nombreCompleto|rol
 *   ejercicios.txt -> id|nombre|TIPO|descripcion|pasos
 *   rutinas.txt    -> id|nombre|idPropietario|idEjercicio1,idEjercicio2,...
 * Los saltos de línea de la descripción y de los pasos se guardan como "\n"
 * (una barra y una n) para que cada ejercicio ocupe una sola línea del archivo.
 */
public class GestorDatos {

    // [AGREGADO] Constantes con la carpeta y los nombres de los archivos de texto.
    // Son de la clase (static final), no atributos de cada objeto.
    private static final String CARPETA = "datos";
    private static final String ARCHIVO_USUARIOS = "usuarios.txt";
    private static final String ARCHIVO_EJERCICIOS = "ejercicios.txt";
    private static final String ARCHIVO_RUTINAS = "rutinas.txt";

    // Atributos del diagrama: colecciones declaradas con la interfaz List
    private List<Usuario> usuarios = new ArrayList<>();
    private List<Ejercicio> ejercicios = new ArrayList<>();
    private List<Rutina> rutinas = new ArrayList<>();

    // Los metodos son synchronized porque varios hilos usan este objeto a la vez.
    // synchronized hace que, si un hilo está dentro de un método, los demás
    // esperen su turno antes de entrar a cualquier método synchronized del objeto.

    // ===================== Usuarios =====================

    /**
     * HU-01. Devuelve el usuario si el nombre y la contraseña coinciden,
     * o null si no coinciden con ninguno.
     */
    public synchronized Usuario validarLogin(String nombreUsuario, String contrasena) {
        for (Usuario u : usuarios) {
            if (u.coincideCredenciales(nombreUsuario, contrasena)) {
                return u;
            }
        }
        return null;
    }

    // ===================== Ejercicios =====================

    /**
     * HU-02. Registra un ejercicio nuevo. Los cuatro campos son obligatorios.
     * El servidor le asigna el id y guarda el archivo de inmediato.
     * Devuelve false si falta algún dato.
     */
    public synchronized boolean registrarEjercicio(Ejercicio e) {
        if (e == null || e.getTipo() == null
                || !textoValido(e.getNombre())
                || !textoValido(e.getDescripcion())
                || !textoValido(e.getPasosEjecucion())) {
            return false;
        }
        // El id nuevo es el mayor id que exista + 1
        int nuevoId = 1;
        for (Ejercicio x : ejercicios) {
            if (x.getId() >= nuevoId) {
                nuevoId = x.getId() + 1;
            }
        }
        e.setId(nuevoId);
        ejercicios.add(e);
        guardarDatos();   // queda en el archivo para todos los clientes
        return true;
    }

    /**
     * HU-03. Devuelve todos los ejercicios.
     * Devuelve una COPIA de la lista: así el hilo que la recibe puede recorrerla
     * tranquilo aunque otro hilo registre un ejercicio al mismo tiempo.
     */
    public synchronized List<Ejercicio> listarEjercicios() {
        return new ArrayList<>(ejercicios);
    }

    /**
     * HU-04. Devuelve solo los ejercicios del tipo indicado (también una copia).
     */
    public synchronized List<Ejercicio> listarPorTipo(TipoEjercicio tipo) {
        List<Ejercicio> resultado = new ArrayList<>();
        for (Ejercicio e : ejercicios) {
            if (e.tieneTipo(tipo)) {
                resultado.add(e);
            }
        }
        return resultado;
    }

    /**
     * HU-05 y HU-06. Busca un ejercicio por su id; devuelve null si no existe.
     */
    public synchronized Ejercicio buscarEjercicio(int id) {
        for (Ejercicio e : ejercicios) {
            if (e.getId() == id) {
                return e;
            }
        }
        return null;
    }

    // ===================== Rutinas =====================

    /**
     * HU-07. Guarda la rutina de un usuario. No acepta una rutina sin nombre,
     * sin propietario o sin ejercicios (multiplicidad 1..* del diagrama).
     */
    public synchronized boolean guardarRutina(Rutina r) {
        if (r == null || r.getPropietario() == null
                || !textoValido(r.getNombre())
                || r.getEjercicios().isEmpty()) {
            return false;
        }
        int nuevoId = 1;
        for (Rutina x : rutinas) {
            if (x.getId() >= nuevoId) {
                nuevoId = x.getId() + 1;
            }
        }
        r.setId(nuevoId);
        rutinas.add(r);
        guardarDatos();
        return true;
    }

    /**
     * HU-07 y HU-08. Devuelve solo las rutinas del usuario indicado:
     * así un cliente nunca ve las rutinas de otro.
     */
    public synchronized List<Rutina> listarRutinas(int idUsuario) {
        List<Rutina> resultado = new ArrayList<>();
        for (Rutina r : rutinas) {
            if (r.getPropietario().getId() == idUsuario) {
                resultado.add(r);
            }
        }
        return resultado;
    }

    // ===================== Archivos de texto =====================

    /**
     * Lee los tres archivos. Si es la primera vez (no existe usuarios.txt),
     * crea los datos iniciales y los guarda.
     * Una línea dañada se ignora y se avisa en la consola, sin detener el servidor.
     */
    public synchronized void cargarDatos() {
        usuarios.clear();
        ejercicios.clear();
        rutinas.clear();

        File archivoUsuarios = new File(CARPETA, ARCHIVO_USUARIOS);
        if (!archivoUsuarios.exists()) {
            crearDatosIniciales();
            guardarDatos();
            System.out.println("Primera ejecución: se crearon los datos iniciales en la carpeta '" + CARPETA + "'.");
            return;
        }

        // ---- 1. Usuarios: id|nombreUsuario|contrasena|nombreCompleto|rol ----
        try (BufferedReader lector = new BufferedReader(
                new FileReader(archivoUsuarios, StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                if (linea.trim().isEmpty()) {
                    continue;   // salta líneas vacías
                }
                try {
                    String[] p = linea.split("\\|", -1);   // "\\|" porque | es especial en split
                    usuarios.add(new Usuario(Integer.parseInt(p[0]), p[1], p[2], p[3], p[4]));
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                    System.err.println("Línea de usuario ignorada: " + linea);
                }
            }
        } catch (IOException ex) {
            System.err.println("No se pudo leer " + ARCHIVO_USUARIOS + ": " + ex.getMessage());
        }

        // ---- 2. Ejercicios: id|nombre|TIPO|descripcion|pasos ----
        File archivoEjercicios = new File(CARPETA, ARCHIVO_EJERCICIOS);
        try (BufferedReader lector = new BufferedReader(
                new FileReader(archivoEjercicios, StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                if (linea.trim().isEmpty()) {
                    continue;
                }
                try {
                    String[] p = linea.split("\\|", -1);
                    ejercicios.add(new Ejercicio(
                            Integer.parseInt(p[0]),
                            p[1],
                            TipoEjercicio.valueOf(p[2]),       // "PIERNA" -> TipoEjercicio.PIERNA
                            p[3].replace("\\n", "\n"),         // "\n" del archivo -> salto de línea
                            p[4].replace("\\n", "\n")));
                } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException ex) {
                    // IllegalArgumentException incluye NumberFormatException y un tipo inválido
                    System.err.println("Línea de ejercicio ignorada: " + linea);
                }
            }
        } catch (IOException ex) {
            System.err.println("No se pudo leer " + ARCHIVO_EJERCICIOS + ": " + ex.getMessage());
        }

        // ---- 3. Rutinas: id|nombre|idPropietario|idEjercicio1,idEjercicio2,... ----
        File archivoRutinas = new File(CARPETA, ARCHIVO_RUTINAS);
        try (BufferedReader lector = new BufferedReader(
                new FileReader(archivoRutinas, StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                if (linea.trim().isEmpty()) {
                    continue;
                }
                try {
                    String[] p = linea.split("\\|", -1);
                    int idPropietario = Integer.parseInt(p[2]);

                    // Busca el usuario dueño de la rutina
                    Usuario propietario = null;
                    for (Usuario u : usuarios) {
                        if (u.getId() == idPropietario) {
                            propietario = u;
                        }
                    }
                    if (propietario == null) {
                        System.err.println("Rutina sin usuario válido, ignorada: " + linea);
                        continue;
                    }

                    Rutina rutina = new Rutina(Integer.parseInt(p[0]), p[1], propietario);
                    // Los ejercicios vienen como "1,4,5": se busca cada uno por su id
                    for (String idTexto : p[3].split(",")) {
                        Ejercicio e = buscarEjercicio(Integer.parseInt(idTexto.trim()));
                        if (e != null) {
                            rutina.agregarEjercicio(e);
                        }
                    }
                    rutinas.add(rutina);
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                    System.err.println("Línea de rutina ignorada: " + linea);
                }
            }
        } catch (IOException ex) {
            // Por ejemplo, si alguien borró el archivo: el servidor sigue sin rutinas
            System.err.println("No se pudo leer " + ARCHIVO_RUTINAS + ": " + ex.getMessage());
        }

        System.out.println("Datos cargados: " + usuarios.size() + " usuarios, "
                + ejercicios.size() + " ejercicios, " + rutinas.size() + " rutinas.");
    }

    /**
     * Escribe las tres listas en sus archivos (los reemplaza completos).
     * Se llama después de registrar un ejercicio o guardar una rutina.
     */
    public synchronized void guardarDatos() {
        new File(CARPETA).mkdirs();   // crea la carpeta "datos" si no existe

        // ---- 1. Usuarios ----
        try (PrintWriter escritor = new PrintWriter(new FileWriter(
                new File(CARPETA, ARCHIVO_USUARIOS), StandardCharsets.UTF_8))) {
            for (Usuario u : usuarios) {
                escritor.println(u.getId() + "|" + u.getNombreUsuario() + "|" + u.getContrasena()
                        + "|" + u.getNombreCompleto() + "|" + u.getRol());
            }
        } catch (IOException ex) {
            System.err.println("No se pudo guardar " + ARCHIVO_USUARIOS + ": " + ex.getMessage());
        }

        // ---- 2. Ejercicios (los saltos de línea se guardan como "\n") ----
        try (PrintWriter escritor = new PrintWriter(new FileWriter(
                new File(CARPETA, ARCHIVO_EJERCICIOS), StandardCharsets.UTF_8))) {
            for (Ejercicio e : ejercicios) {
                escritor.println(e.getId() + "|" + e.getNombre() + "|" + e.getTipo().name()
                        + "|" + e.getDescripcion().replace("\r", "").replace("\n", "\\n")
                        + "|" + e.getPasosEjecucion().replace("\r", "").replace("\n", "\\n"));
            }
        } catch (IOException ex) {
            System.err.println("No se pudo guardar " + ARCHIVO_EJERCICIOS + ": " + ex.getMessage());
        }

        // ---- 3. Rutinas (los ejercicios se guardan como lista de ids: 1,4,5) ----
        try (PrintWriter escritor = new PrintWriter(new FileWriter(
                new File(CARPETA, ARCHIVO_RUTINAS), StandardCharsets.UTF_8))) {
            for (Rutina r : rutinas) {
                StringBuilder ids = new StringBuilder();
                for (Ejercicio e : r.getEjercicios()) {
                    if (ids.length() > 0) {
                        ids.append(",");
                    }
                    ids.append(e.getId());
                }
                escritor.println(r.getId() + "|" + r.getNombre() + "|"
                        + r.getPropietario().getId() + "|" + ids);
            }
        } catch (IOException ex) {
            System.err.println("No se pudo guardar " + ARCHIVO_RUTINAS + ": " + ex.getMessage());
        }
    }

    // ===================== Métodos privados de apoyo =====================

    /**
     * [AGREGADO] Privado. Revisa que un texto no sea null, no esté vacío
     * y no tenga "|", porque ese carácter separa los campos en los archivos
     * y en los mensajes.
     */
    private static boolean textoValido(String texto) {
        return texto != null && !texto.trim().isEmpty() && !texto.contains("|");
    }

    /**
     * [AGREGADO] Privado. Datos de la primera ejecución: el administrador,
     * el cliente y los 6 ejercicios del prototipo.
     */
    private void crearDatosIniciales() {
        usuarios.add(new Usuario(1, "admin", "admin123", "Ana Rojas", "ADMIN"));
        usuarios.add(new Usuario(2, "cliente1", "cliente123", "Sebastián Salas Chinchilla", "CLIENTE"));

        ejercicios.add(new Ejercicio(1, "Sentadilla", TipoEjercicio.PIERNA,
                "Ejercicio para trabajar piernas y glúteos.",
                "1. Párate con los pies a la altura de los hombros.\n"
                + "2. Baja flexionando las rodillas, con la espalda recta.\n"
                + "3. Sube empujando con los talones."));

        ejercicios.add(new Ejercicio(2, "Zancadas", TipoEjercicio.PIERNA,
                "Ejercicio para trabajar cuádriceps y glúteos, una pierna a la vez.",
                "1. Párate derecho con los pies juntos.\n"
                + "2. Da un paso largo hacia adelante y baja hasta que ambas rodillas formen un ángulo de 90 grados.\n"
                + "3. Empuja con el pie de adelante para volver al inicio y cambia de pierna."));

        ejercicios.add(new Ejercicio(3, "Prensa de pierna", TipoEjercicio.PIERNA,
                "Ejercicio en máquina para trabajar cuádriceps, isquiotibiales y glúteos.",
                "1. Siéntate en la máquina con la espalda apoyada y los pies en la plataforma, separados al ancho de los hombros.\n"
                + "2. Quita los seguros y baja la plataforma doblando las rodillas despacio.\n"
                + "3. Empuja la plataforma hasta casi estirar las piernas, sin bloquear las rodillas."));

        ejercicios.add(new Ejercicio(4, "Remo con barra", TipoEjercicio.ESPALDA,
                "Ejercicio para trabajar la espalda.",
                "1. Sujeta la barra con las manos separadas.\n"
                + "2. Inclina el torso hacia adelante con la espalda recta.\n"
                + "3. Lleva la barra hacia el abdomen y baja despacio."));

        ejercicios.add(new Ejercicio(5, "Curl de bíceps", TipoEjercicio.BRAZO,
                "Ejercicio para trabajar los bíceps.",
                "1. Toma una mancuerna en cada mano con los brazos estirados.\n"
                + "2. Dobla los codos y sube las mancuernas hacia los hombros.\n"
                + "3. Baja despacio hasta estirar los brazos."));

        ejercicios.add(new Ejercicio(6, "Press de pecho", TipoEjercicio.PECHO,
                "Ejercicio para trabajar el pecho, los hombros y los tríceps.",
                "1. Acuéstate en el banco con los pies apoyados en el piso.\n"
                + "2. Toma la barra con las manos un poco más separadas que los hombros y bájala despacio hasta el pecho.\n"
                + "3. Empuja la barra hacia arriba hasta estirar los brazos."));
    }
}
