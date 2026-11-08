package cliente;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import modelo.Ejercicio;
import modelo.TipoEjercicio;

/**
 * [AGREGADO] Clase base de las 6 ventanas Swing. Hereda de JFrame y las
 * ventanas heredan de ella, así que todas siguen siendo JFrame.
 * Reúne lo que el prototipo repite en todas las pantallas: tamaño 800 x 520,
 * fondo gris claro, títulos en azul, botones grises y la barra de estado
 * "Conectado al servidor". Así ese código no se copia seis veces.
 *
 * Es abstract porque no se muestra sola: solo sirve de base para las demás.
 */
public abstract class VentanaBase extends JFrame {

    // Colores y letra del prototipo (protected: los usan las clases hijas)
    protected static final Color AZUL = new Color(44, 82, 140);
    protected static final Color ROJO = new Color(200, 30, 45);
    protected static final Color VERDE = new Color(30, 120, 50);
    protected static final Color GRIS_TEXTO = new Color(110, 110, 110);
    protected static final Color FONDO = new Color(242, 242, 242);
    protected static final String LETRA = "SansSerif";

    // Conexión compartida: la misma para todas las ventanas de este cliente
    protected ClienteSocket cliente;

    private JLabel lblEstado;   // barra de estado de abajo

    public VentanaBase(String titulo, ClienteSocket cliente) {
        super(titulo);                     // llama al constructor de JFrame
        this.cliente = cliente;
        setSize(800, 520);                 // mismo tamaño que los frames del prototipo
        setMinimumSize(new Dimension(700, 460));
        setLocationRelativeTo(null);       // centrada en la pantalla
        getContentPane().setBackground(FONDO);
        setLayout(new BorderLayout());

        // Barra de estado (abajo, en todas las pantallas)
        lblEstado = new JLabel("Conectado al servidor");
        lblEstado.setFont(new Font(LETRA, Font.PLAIN, 11));
        lblEstado.setOpaque(true);
        lblEstado.setBackground(new Color(228, 228, 228));
        lblEstado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(190, 190, 190)),   // línea arriba
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));                           // margen
        add(lblEstado, BorderLayout.SOUTH);
    }

    /**
     * Envía una solicitud al servidor por medio de ClienteSocket.
     * Si la respuesta dice que se perdió la conexión, la barra de estado lo muestra en rojo.
     */
    protected String enviar(String solicitud) {
        String respuesta = cliente.enviarSolicitud(solicitud);
        if (respuesta.startsWith("ERROR|") && respuesta.contains("conexión")) {
            lblEstado.setText("Sin conexión con el servidor");
            lblEstado.setForeground(ROJO);
        }
        return respuesta;
    }

    /**
     * Devuelve el texto de una respuesta sin "OK|" ni "ERROR|".
     * Ejemplo: "ERROR|Usuario o contraseña incorrectos" -> "Usuario o contraseña incorrectos".
     */
    protected String mensajeDe(String respuesta) {
        int posicion = respuesta.indexOf('|');
        return (posicion >= 0) ? respuesta.substring(posicion + 1) : respuesta;
    }

    /**
     * Crea un botón gris con borde, como los del prototipo.
     */
    protected JButton crearBoton(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font(LETRA, Font.PLAIN, 12));
        boton.setBackground(new Color(224, 224, 224));
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 150, 150)),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)));
        return boton;
    }

    /**
     * Crea el título azul que llevan las pantallas arriba a la izquierda
     * ("Ejercicios", "Crear rutina", ...).
     */
    protected JLabel crearTitulo(String texto) {
        JLabel titulo = new JLabel(texto);
        titulo.setFont(new Font(LETRA, Font.BOLD, 18));
        titulo.setForeground(AZUL);
        return titulo;
    }

    /**
     * Crea una etiqueta en negrita para los campos ("Usuario", "Nombre", ...).
     */
    protected JLabel crearEtiqueta(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(new Font(LETRA, Font.BOLD, 12));
        return etiqueta;
    }

    // ===================== Tablas =====================

    /**
     * Crea el modelo de una tabla con las columnas indicadas.
     * Es una clase anónima que sobrescribe isCellEditable (polimorfismo)
     * para que el usuario no pueda escribir dentro de las celdas.
     */
    protected DefaultTableModel crearModeloTabla(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    /**
     * Crea una tabla con el estilo del prototipo: encabezado gris en negrita,
     * fila seleccionada en celeste y una sola fila seleccionable a la vez.
     */
    protected JTable crearTabla(DefaultTableModel modelo) {
        JTable tabla = new JTable(modelo);
        tabla.setFont(new Font(LETRA, Font.PLAIN, 12));
        tabla.setRowHeight(24);
        tabla.setGridColor(new Color(205, 205, 205));
        tabla.setSelectionBackground(new Color(184, 207, 233));
        tabla.setSelectionForeground(Color.BLACK);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setFillsViewportHeight(true);   // fondo blanco aunque haya pocas filas
        // Tamaño inicial pequeño: la tabla luego crece para llenar el espacio libre
        tabla.setPreferredScrollableViewportSize(new Dimension(280, 160));
        JTableHeader encabezado = tabla.getTableHeader();
        encabezado.setFont(new Font(LETRA, Font.BOLD, 12));
        encabezado.setBackground(new Color(214, 214, 214));
        encabezado.setReorderingAllowed(false);
        // Títulos de columna alineados a la izquierda, como en el prototipo
        ((DefaultTableCellRenderer) encabezado.getDefaultRenderer())
                .setHorizontalAlignment(SwingConstants.LEFT);
        return tabla;
    }

    // ===================== Ejercicios desde el servidor =====================

    /**
     * Pide al servidor la lista de ejercicios ("TODOS" o un tipo como "PIERNA")
     * y la convierte en una List de objetos Ejercicio del modelo.
     * La lista solo trae id, nombre y tipo; la descripción y los pasos quedan vacíos.
     * Devuelve null si hubo un error (y ya lo mostró en pantalla).
     */
    protected List<Ejercicio> pedirEjercicios(String tipo) {
        String respuesta = enviar("LISTAR_EJERCICIOS|" + tipo);
        if (!respuesta.startsWith("OK|")) {
            mostrarError(mensajeDe(respuesta));
            return null;
        }
        List<Ejercicio> lista = new ArrayList<>();
        String datos = respuesta.substring(3);          // quita "OK|"
        if (datos.isEmpty()) {
            return lista;                               // no hay ejercicios: lista vacía
        }
        for (String item : datos.split("#")) {          // "1;Sentadilla;PIERNA"
            String[] campos = item.split(";");
            lista.add(new Ejercicio(Integer.parseInt(campos[0]), campos[1],
                    TipoEjercicio.valueOf(campos[2]), "", ""));
        }
        return lista;
    }

    /**
     * Pide al servidor el detalle completo de un ejercicio (con descripción y pasos).
     * Devuelve null si hubo un error (y ya lo mostró en pantalla).
     */
    protected Ejercicio pedirDetalle(int idEjercicio) {
        String respuesta = enviar("DETALLE_EJERCICIO|" + idEjercicio);
        if (!respuesta.startsWith("OK|")) {
            mostrarError(mensajeDe(respuesta));
            return null;
        }
        // OK|id|nombre|TIPO|descripcion|pasos   ("\n" -> salto de línea)
        String[] p = respuesta.split("\\|", -1);
        return new Ejercicio(Integer.parseInt(p[1]), p[2], TipoEjercicio.valueOf(p[3]),
                p[4].replace("\\n", "\n"), p[5].replace("\\n", "\n"));
    }

    // ===================== Navegación y mensajes =====================

    /**
     * Para las pantallas 3 a 6: al cerrarlas (con Volver, Cancelar o la X)
     * no termina la aplicación, solo se vuelve a mostrar el menú.
     */
    protected void configurarRegresoAlMenu(VentanaMenu menu) {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {   // se llama después de dispose()
                menu.setVisible(true);
            }
        });
    }

    protected void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Fidness", JOptionPane.ERROR_MESSAGE);
    }

    protected void mostrarInformacion(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Fidness", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Revisa que un nombre no tenga los caracteres que usa el protocolo (| ; #).
     */
    protected boolean nombrePermitido(String nombre) {
        return !nombre.contains("|") && !nombre.contains(";") && !nombre.contains("#");
    }
}
