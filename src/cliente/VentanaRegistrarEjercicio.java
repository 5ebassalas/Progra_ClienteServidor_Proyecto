package cliente;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import modelo.TipoEjercicio;

// Pantalla 4: Registrar ejercicio, solo administrador (HU-02)
// Componentes del prototipo: campo Nombre, lista de Tipo, áreas de texto Descripción
// y Pasos, botones Guardar y Cancelar, y etiqueta de mensaje.
public class VentanaRegistrarEjercicio extends VentanaBase {

    private static final int ANCHO_CAMPOS = 560;

    private JTextField txtNombre;
    private JComboBox<Object> cmbTipo;
    private JTextArea txtDescripcion;
    private JTextArea txtPasos;
    private JButton btnGuardar;
    private JButton btnCancelar;
    private JLabel lblMensaje;

    public VentanaRegistrarEjercicio(ClienteSocket cliente, VentanaMenu menu) {
        super("Fidness - Registrar ejercicio", cliente);
        configurarRegresoAlMenu(menu);   // Cancelar o la X regresan al menú

        // ----- Componentes -----
        txtNombre = new JTextField();
        txtNombre.setPreferredSize(new Dimension(ANCHO_CAMPOS, 26));

        // Primera opción vacía para que el tipo también sea obligatorio
        cmbTipo = new JComboBox<>();
        cmbTipo.addItem("Seleccione un tipo");
        for (TipoEjercicio tipo : TipoEjercicio.values()) {
            cmbTipo.addItem(tipo);   // se ve "Pierna", "Espalda"... (toString del enum)
        }
        cmbTipo.setPreferredSize(new Dimension(210, 26));

        txtDescripcion = crearAreaTexto();
        JScrollPane scrollDescripcion = new JScrollPane(txtDescripcion);
        scrollDescripcion.setPreferredSize(new Dimension(ANCHO_CAMPOS, 48));

        txtPasos = crearAreaTexto();
        JScrollPane scrollPasos = new JScrollPane(txtPasos);
        scrollPasos.setPreferredSize(new Dimension(ANCHO_CAMPOS, 100));

        btnGuardar = crearBoton("Guardar");
        btnCancelar = crearBoton("Cancelar");
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelBotones.setOpaque(false);
        panelBotones.add(btnGuardar);
        panelBotones.add(javax.swing.Box.createHorizontalStrut(8));
        panelBotones.add(btnCancelar);

        lblMensaje = new JLabel(" ");
        lblMensaje.setFont(new Font(LETRA, Font.PLAIN, 12));

        // ----- Acomodo: una columna de 560 px centrada -----
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridy = 0;  c.insets = new Insets(0, 0, 8, 0);  panel.add(crearTitulo("Registrar ejercicio"), c);
        c.gridy = 1;  c.insets = new Insets(0, 0, 3, 0);  panel.add(crearEtiqueta("Nombre"), c);
        c.gridy = 2;  c.insets = new Insets(0, 0, 9, 0);  panel.add(txtNombre, c);
        c.gridy = 3;  c.insets = new Insets(0, 0, 3, 0);  panel.add(crearEtiqueta("Tipo"), c);
        c.gridy = 4;  c.insets = new Insets(0, 0, 9, 0);
        c.fill = GridBagConstraints.NONE;                  // el combo no se estira
        panel.add(cmbTipo, c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy = 5;  c.insets = new Insets(0, 0, 3, 0);  panel.add(crearEtiqueta("Descripción"), c);
        c.gridy = 6;  c.insets = new Insets(0, 0, 9, 0);  panel.add(scrollDescripcion, c);
        c.gridy = 7;  c.insets = new Insets(0, 0, 3, 0);  panel.add(crearEtiqueta("Pasos de ejecución"), c);
        c.gridy = 8;  c.insets = new Insets(0, 0, 12, 0); panel.add(scrollPasos, c);
        c.gridy = 9;  c.insets = new Insets(0, 0, 8, 0);  panel.add(panelBotones, c);
        c.gridy = 10; c.insets = new Insets(0, 0, 0, 0);  panel.add(lblMensaje, c);
        c.gridy = 11; c.weighty = 1;                       panel.add(new JLabel(), c);   // empuja hacia arriba

        add(panel, BorderLayout.CENTER);

        // ----- Eventos -----
        btnGuardar.addActionListener(e -> guardar());
        btnCancelar.addActionListener(e -> dispose());
    }

    private JTextArea crearAreaTexto() {
        JTextArea area = new JTextArea();
        area.setFont(new Font(LETRA, Font.PLAIN, 12));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return area;
    }

    /**
     * HU-02. Revisa que los cuatro campos estén llenos y envía REGISTRAR_EJERCICIO.
     * Al guardar, el ejercicio queda en el servidor para todos los clientes.
     */
    private void guardar() {
        String nombre = txtNombre.getText().trim();
        Object tipoElegido = cmbTipo.getSelectedItem();
        String descripcion = txtDescripcion.getText().trim();
        String pasos = txtPasos.getText().trim();

        // Los cuatro campos son obligatorios
        if (nombre.isEmpty()) {
            mostrarMensaje("Complete el campo Nombre.", ROJO);
            return;
        }
        if (!(tipoElegido instanceof TipoEjercicio)) {
            mostrarMensaje("Seleccione el tipo de ejercicio.", ROJO);
            return;
        }
        if (descripcion.isEmpty()) {
            mostrarMensaje("Complete el campo Descripción.", ROJO);
            return;
        }
        if (pasos.isEmpty()) {
            mostrarMensaje("Complete el campo Pasos de ejecución.", ROJO);
            return;
        }
        // Caracteres que usa el protocolo
        if (!nombrePermitido(nombre)) {
            mostrarMensaje("El nombre no puede tener los caracteres | ; #", ROJO);
            return;
        }
        if (descripcion.contains("|") || pasos.contains("|")) {
            mostrarMensaje("La descripción y los pasos no pueden tener el carácter |", ROJO);
            return;
        }

        TipoEjercicio tipo = (TipoEjercicio) tipoElegido;
        // Los saltos de línea viajan como "\n" para que el mensaje sea una sola línea
        String respuesta = enviar("REGISTRAR_EJERCICIO|" + nombre + "|" + tipo.name()
                + "|" + descripcion.replace("\r", "").replace("\n", "\\n")
                + "|" + pasos.replace("\r", "").replace("\n", "\\n"));

        if (respuesta.startsWith("OK|")) {
            mostrarMensaje(mensajeDe(respuesta), VERDE);   // "Ejercicio guardado correctamente"
            // Limpia el formulario para registrar otro
            txtNombre.setText("");
            cmbTipo.setSelectedIndex(0);
            txtDescripcion.setText("");
            txtPasos.setText("");
            txtNombre.requestFocus();
        } else {
            mostrarMensaje(mensajeDe(respuesta), ROJO);
        }
    }

    private void mostrarMensaje(String texto, Color color) {
        lblMensaje.setText(texto);
        lblMensaje.setForeground(color);
    }
}
