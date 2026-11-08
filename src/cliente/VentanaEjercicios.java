package cliente;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import modelo.Ejercicio;
import modelo.TipoEjercicio;

// Pantalla 3: Ejercicios (HU-03, HU-04, HU-05)
// Componentes del prototipo: lista desplegable de tipo, tabla Nombre y Tipo,
// panel de detalle con descripción y pasos (texto de solo lectura) y botón Volver.
public class VentanaEjercicios extends VentanaBase {

    private JComboBox<Object> cmbTipo;
    private DefaultTableModel modeloTabla;
    private JTable tabla;
    private JLabel lblMensaje;          // aviso cuando no hay ejercicios (HU-03)
    private JLabel lblNombre;
    private JLabel lblTipo;
    private JLabel lblDescripcion;
    private JTextArea txtPasos;
    private JButton btnVolver;

    // Ejercicios que están en la tabla, en el mismo orden que las filas.
    // Sirve para saber el id del ejercicio de la fila seleccionada.
    private List<Ejercicio> ejerciciosMostrados = new ArrayList<>();

    public VentanaEjercicios(ClienteSocket cliente, VentanaMenu menu) {
        super("Fidness - Ejercicios", cliente);
        configurarRegresoAlMenu(menu);   // Volver o la X regresan al menú

        // ========== Columna izquierda: filtro y tabla ==========
        // "Todos" y luego los 5 tipos. En la lista se ve "Pierna" gracias al
        // toString() sobrescrito de TipoEjercicio (polimorfismo).
        cmbTipo = new JComboBox<>();
        cmbTipo.addItem("Todos");
        for (TipoEjercicio tipo : TipoEjercicio.values()) {
            cmbTipo.addItem(tipo);
        }

        JPanel panelFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelFiltro.setOpaque(false);
        panelFiltro.add(crearEtiqueta("Tipo: "));
        panelFiltro.add(cmbTipo);
        panelFiltro.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        modeloTabla = crearModeloTabla("Nombre", "Tipo");
        tabla = crearTabla(modeloTabla);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(220);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(110);

        lblMensaje = new JLabel(" ");
        lblMensaje.setFont(new Font(LETRA, Font.ITALIC, 12));
        lblMensaje.setForeground(GRIS_TEXTO);
        lblMensaje.setBorder(BorderFactory.createEmptyBorder(6, 2, 0, 0));

        JPanel izquierda = new JPanel(new BorderLayout());
        izquierda.setOpaque(false);
        izquierda.add(panelFiltro, BorderLayout.NORTH);
        izquierda.add(new JScrollPane(tabla), BorderLayout.CENTER);
        izquierda.add(lblMensaje, BorderLayout.SOUTH);

        // ========== Columna derecha: detalle del ejercicio ==========
        JLabel lblTituloDetalle = new JLabel("Detalle del ejercicio");
        lblTituloDetalle.setFont(new Font(LETRA, Font.BOLD, 13));

        lblNombre = new JLabel();
        lblTipo = new JLabel();
        lblDescripcion = new JLabel();
        for (JLabel etiqueta : new JLabel[]{lblNombre, lblTipo, lblDescripcion}) {
            etiqueta.setFont(new Font(LETRA, Font.PLAIN, 12));
        }

        // Pasos: área de texto de solo lectura, con barra de desplazamiento (HU-05)
        txtPasos = new JTextArea();
        txtPasos.setEditable(false);
        txtPasos.setLineWrap(true);          // corta las líneas largas
        txtPasos.setWrapStyleWord(true);     // sin partir palabras
        txtPasos.setFont(new Font(java.awt.Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollPasos = new JScrollPane(txtPasos);
        scrollPasos.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPasos.setPreferredSize(new Dimension(250, 150));
        scrollPasos.setMinimumSize(new Dimension(150, 150));   // nunca más bajo que 150 px

        JPanel derecha = new JPanel(new GridBagLayout());
        derecha.setBackground(java.awt.Color.WHITE);
        derecha.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;

        c.gridy = 0; c.insets = new Insets(0, 0, 8, 0); derecha.add(lblTituloDetalle, c);
        c.gridy = 1; c.insets = new Insets(0, 0, 3, 0); derecha.add(lblNombre, c);
        c.gridy = 2;                                    derecha.add(lblTipo, c);
        c.gridy = 3;                                    derecha.add(lblDescripcion, c);
        c.gridy = 4;                                    derecha.add(crearEtiqueta("Pasos de ejecución:"), c);
        c.gridy = 5; c.insets = new Insets(0, 0, 0, 0); derecha.add(scrollPasos, c);
        c.gridy = 6; c.weighty = 1;                     derecha.add(new JLabel(), c);   // espacio libre abajo

        // ========== Acomodo general ==========
        JPanel columnas = new JPanel(new GridLayout(1, 2, 18, 0));   // dos columnas iguales
        columnas.setOpaque(false);
        columnas.add(izquierda);
        columnas.add(derecha);

        btnVolver = crearBoton("Volver");
        JPanel panelVolver = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panelVolver.setOpaque(false);
        panelVolver.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        panelVolver.add(btnVolver);

        JLabel lblTitulo = crearTitulo("Ejercicios");
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JPanel contenido = new JPanel(new BorderLayout());
        contenido.setOpaque(false);
        contenido.setBorder(BorderFactory.createEmptyBorder(12, 22, 10, 22));
        contenido.add(lblTitulo, BorderLayout.NORTH);
        contenido.add(columnas, BorderLayout.CENTER);
        contenido.add(panelVolver, BorderLayout.SOUTH);
        add(contenido, BorderLayout.CENTER);

        // ========== Eventos ==========
        cmbTipo.addActionListener(e -> cargarTabla());                    // HU-04
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {    // evita repetir mientras se arrastra el mouse
                mostrarDetalle();              // HU-05
            }
        });
        btnVolver.addActionListener(e -> dispose());

        limpiarDetalle();
        cargarTabla();   // al abrir muestra "Todos" (HU-03)
    }

    /**
     * HU-03 y HU-04. Pide al servidor los ejercicios del tipo elegido y llena la tabla.
     */
    private void cargarTabla() {
        Object seleccion = cmbTipo.getSelectedItem();
        // "Todos" es un String; los demás son TipoEjercicio (se envía su name(): "PIERNA")
        String tipo = (seleccion instanceof TipoEjercicio)
                ? ((TipoEjercicio) seleccion).name()
                : "TODOS";

        List<Ejercicio> lista = pedirEjercicios(tipo);
        if (lista == null) {
            return;   // hubo un error y ya se mostró
        }
        ejerciciosMostrados = lista;
        modeloTabla.setRowCount(0);   // borra las filas anteriores
        for (Ejercicio e : lista) {
            modeloTabla.addRow(new Object[]{e.getNombre(), e.getTipo()});   // el tipo se ve como "Pierna"
        }
        limpiarDetalle();

        // HU-03: si no hay ejercicios, la ventana lo indica
        if (lista.isEmpty()) {
            lblMensaje.setText(seleccion instanceof TipoEjercicio
                    ? "No hay ejercicios de tipo " + seleccion + "."
                    : "No hay ejercicios registrados.");
        } else {
            lblMensaje.setText(" ");
        }
    }

    /**
     * HU-05. Muestra nombre, tipo, descripción y pasos del ejercicio seleccionado.
     */
    private void mostrarDetalle() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return;   // no hay fila seleccionada
        }
        Ejercicio e = pedirDetalle(ejerciciosMostrados.get(fila).getId());
        if (e == null) {
            return;
        }
        lblNombre.setText("<html><b>Nombre:</b> " + e.getNombre() + "</html>");
        lblTipo.setText("<html><b>Tipo:</b> " + e.getTipo() + "</html>");
        // width hace que una descripción larga pase a la siguiente línea
        lblDescripcion.setText("<html><div style='width:250px'><b>Descripción:</b> "
                + e.getDescripcion() + "</div></html>");
        txtPasos.setText(e.getPasosEjecucion());
        txtPasos.setCaretPosition(0);   // muestra desde el primer paso
    }

    private void limpiarDetalle() {
        lblNombre.setText("<html><b>Nombre:</b></html>");
        lblTipo.setText("<html><b>Tipo:</b></html>");
        lblDescripcion.setText("<html><b>Descripción:</b></html>");
        txtPasos.setText("Seleccione un ejercicio de la tabla.");
    }
}
