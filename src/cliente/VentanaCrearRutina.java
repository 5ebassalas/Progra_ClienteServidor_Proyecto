package cliente;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import modelo.Ejercicio;
import modelo.Rutina;

// Pantalla 5: Crear rutina (HU-06, HU-07)
// Componentes del prototipo: campo Nombre de la rutina, tabla de ejercicios disponibles,
// botones Agregar y Quitar, tabla de ejercicios de la rutina, y botones Guardar rutina y Volver.
public class VentanaCrearRutina extends VentanaBase {

    private JTextField txtNombre;
    private DefaultTableModel modeloDisponibles;
    private JTable tablaDisponibles;
    private DefaultTableModel modeloRutina;
    private JTable tablaRutina;
    private JButton btnAgregar;
    private JButton btnQuitar;
    private JButton btnGuardar;
    private JButton btnVolver;

    // Ejercicios de la tabla izquierda, en el mismo orden que las filas
    private List<Ejercicio> disponibles = new ArrayList<>();

    // La rutina que se está armando: se usa la misma clase Rutina del modelo.
    // El dueño queda en null porque el servidor le asigna el usuario de la sesión.
    private Rutina rutina = new Rutina(0, "", null);

    public VentanaCrearRutina(ClienteSocket cliente, VentanaMenu menu) {
        super("Fidness - Crear rutina", cliente);
        configurarRegresoAlMenu(menu);

        // ========== Arriba: título y nombre de la rutina ==========
        txtNombre = new JTextField();
        txtNombre.setPreferredSize(new Dimension(300, 26));

        JPanel filaNombre = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        filaNombre.setOpaque(false);
        filaNombre.add(crearEtiqueta("Nombre de la rutina:  "));
        filaNombre.add(txtNombre);

        JPanel arriba = new JPanel(new BorderLayout(0, 8));
        arriba.setOpaque(false);
        arriba.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        arriba.add(crearTitulo("Crear rutina"), BorderLayout.NORTH);
        arriba.add(filaNombre, BorderLayout.CENTER);

        // ========== Centro: disponibles | botones | rutina ==========
        modeloDisponibles = crearModeloTabla("Nombre", "Tipo");
        tablaDisponibles = crearTabla(modeloDisponibles);
        modeloRutina = crearModeloTabla("Nombre", "Tipo");
        tablaRutina = crearTabla(modeloRutina);

        btnAgregar = crearBoton("Agregar >>");
        btnQuitar = crearBoton("<< Quitar");
        btnQuitar.setPreferredSize(btnAgregar.getPreferredSize());   // mismo ancho

        JPanel panelFlechas = new JPanel(new GridBagLayout());
        panelFlechas.setOpaque(false);
        GridBagConstraints cf = new GridBagConstraints();
        cf.gridx = 0;
        cf.gridy = 0; cf.insets = new Insets(26, 0, 8, 0); panelFlechas.add(btnAgregar, cf);
        cf.gridy = 1; cf.insets = new Insets(0, 0, 0, 0);  panelFlechas.add(btnQuitar, cf);
        cf.gridy = 2; cf.weighty = 1;                      panelFlechas.add(new JLabel(), cf);

        JPanel centro = new JPanel(new GridBagLayout());
        centro.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;

        // Fila 0: los dos subtítulos
        c.gridy = 0; c.weightx = 1; c.weighty = 0; c.insets = new Insets(0, 0, 4, 0);
        c.gridx = 0; centro.add(crearEtiqueta("Ejercicios disponibles"), c);
        c.gridx = 2; centro.add(crearEtiqueta("Ejercicios de la rutina"), c);

        // Fila 1: tabla, botones y tabla
        c.gridy = 1; c.weighty = 1; c.insets = new Insets(0, 0, 0, 0);
        c.gridx = 0; c.weightx = 1; centro.add(new JScrollPane(tablaDisponibles), c);
        c.gridx = 1; c.weightx = 0; c.insets = new Insets(0, 14, 0, 14); centro.add(panelFlechas, c);
        c.gridx = 2; c.weightx = 1; c.insets = new Insets(0, 0, 0, 0);   centro.add(new JScrollPane(tablaRutina), c);

        // ========== Abajo a la derecha: Guardar rutina y Volver ==========
        btnGuardar = crearBoton("Guardar rutina");
        btnVolver = crearBoton("Volver");
        JPanel abajo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        abajo.setOpaque(false);
        abajo.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        abajo.add(btnGuardar);
        abajo.add(Box.createHorizontalStrut(8));
        abajo.add(btnVolver);

        JPanel contenido = new JPanel(new BorderLayout());
        contenido.setOpaque(false);
        contenido.setBorder(BorderFactory.createEmptyBorder(12, 22, 10, 22));
        contenido.add(arriba, BorderLayout.NORTH);
        contenido.add(centro, BorderLayout.CENTER);
        contenido.add(abajo, BorderLayout.SOUTH);
        add(contenido, BorderLayout.CENTER);

        // ========== Eventos ==========
        btnAgregar.addActionListener(e -> agregar());
        btnQuitar.addActionListener(e -> quitar());
        btnGuardar.addActionListener(e -> guardar());
        btnVolver.addActionListener(e -> dispose());

        cargarDisponibles();
    }

    /**
     * Llena la tabla de la izquierda con todos los ejercicios del servidor.
     */
    private void cargarDisponibles() {
        List<Ejercicio> lista = pedirEjercicios("TODOS");
        if (lista == null) {
            return;
        }
        disponibles = lista;
        modeloDisponibles.setRowCount(0);
        for (Ejercicio e : disponibles) {
            modeloDisponibles.addRow(new Object[]{e.getNombre(), e.getTipo()});
        }
    }

    /**
     * Vuelve a dibujar la tabla de la derecha con los ejercicios de la rutina.
     */
    private void actualizarTablaRutina() {
        modeloRutina.setRowCount(0);
        for (Ejercicio e : rutina.getEjercicios()) {
            modeloRutina.addRow(new Object[]{e.getNombre(), e.getTipo()});
        }
    }

    /**
     * HU-06. Pasa el ejercicio seleccionado de "disponibles" a "la rutina".
     */
    private void agregar() {
        int fila = tablaDisponibles.getSelectedRow();
        if (fila < 0) {
            mostrarError("Seleccione un ejercicio de la lista de disponibles.");
            return;
        }
        Ejercicio elegido = disponibles.get(fila);
        for (Ejercicio e : rutina.getEjercicios()) {
            if (e.getId() == elegido.getId()) {
                mostrarError("Ese ejercicio ya está en la rutina.");
                return;
            }
        }
        rutina.agregarEjercicio(elegido);   // método de la clase Rutina
        actualizarTablaRutina();
    }

    /**
     * HU-06. Quita de la rutina el ejercicio seleccionado en la tabla de la derecha.
     */
    private void quitar() {
        int fila = tablaRutina.getSelectedRow();
        if (fila < 0) {
            mostrarError("Seleccione un ejercicio de la rutina para quitarlo.");
            return;
        }
        int idEjercicio = rutina.getEjercicios().get(fila).getId();
        rutina.quitarEjercicio(idEjercicio);   // método de la clase Rutina (por id)
        actualizarTablaRutina();
    }

    /**
     * HU-06 y HU-07. No deja guardar sin nombre o sin ejercicios.
     * Envía GUARDAR_RUTINA|nombre|1,5,4 y confirma con un mensaje.
     */
    private void guardar() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            mostrarError("Escriba el nombre de la rutina.");
            txtNombre.requestFocus();
            return;
        }
        if (!nombrePermitido(nombre)) {
            mostrarError("El nombre no puede tener los caracteres | ; #");
            return;
        }
        if (rutina.getEjercicios().isEmpty()) {
            mostrarError("Agregue al menos un ejercicio a la rutina.");
            return;
        }

        // Arma la lista de ids: "1,5,4"
        StringBuilder ids = new StringBuilder();
        for (Ejercicio e : rutina.getEjercicios()) {
            if (ids.length() > 0) {
                ids.append(",");
            }
            ids.append(e.getId());
        }

        String respuesta = enviar("GUARDAR_RUTINA|" + nombre + "|" + ids);
        if (respuesta.startsWith("OK|")) {
            mostrarInformacion(mensajeDe(respuesta));   // "La rutina quedó registrada"
            // Deja el formulario listo para crear otra rutina
            txtNombre.setText("");
            rutina = new Rutina(0, "", null);
            actualizarTablaRutina();
        } else {
            mostrarError(mensajeDe(respuesta));
        }
    }
}
