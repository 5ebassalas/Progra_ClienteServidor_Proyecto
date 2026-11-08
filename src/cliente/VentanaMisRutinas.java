package cliente;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import modelo.Ejercicio;
import modelo.Rutina;
import modelo.Usuario;

// Pantalla 6: Mis rutinas (HU-08, HU-09)
// Componentes del prototipo: lista de mis rutinas, tabla con los ejercicios de la rutina
// seleccionada y botones Exportar a archivo (.txt) y Volver.
public class VentanaMisRutinas extends VentanaBase {

    private Usuario usuario;   // dueño de las rutinas (para el texto exportado)

    // La JList muestra objetos Rutina: se ve su nombre gracias al toString() de Rutina
    private DefaultListModel<Rutina> modeloLista;
    private JList<Rutina> lstRutinas;
    private JLabel lblMensaje;
    private DefaultTableModel modeloTabla;
    private JTable tablaEjercicios;
    private JButton btnExportar;
    private JButton btnVolver;

    public VentanaMisRutinas(ClienteSocket cliente, VentanaMenu menu) {
        super("Fidness - Mis rutinas", cliente);
        this.usuario = menu.getUsuario();
        configurarRegresoAlMenu(menu);

        // ========== Componentes ==========
        modeloLista = new DefaultListModel<>();
        lstRutinas = new JList<>(modeloLista);
        lstRutinas.setFont(new Font(LETRA, Font.PLAIN, 12));
        lstRutinas.setFixedCellHeight(28);
        lstRutinas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lstRutinas.setSelectionBackground(new Color(184, 207, 233));
        lstRutinas.setSelectionForeground(Color.BLACK);
        JScrollPane scrollLista = new JScrollPane(lstRutinas);
        scrollLista.setPreferredSize(new Dimension(240, 160));
        scrollLista.setMinimumSize(new Dimension(240, 100));   // mantiene el ancho del prototipo

        lblMensaje = new JLabel(" ");
        lblMensaje.setFont(new Font(LETRA, Font.ITALIC, 12));
        lblMensaje.setForeground(GRIS_TEXTO);

        modeloTabla = crearModeloTabla("Nombre", "Tipo");
        tablaEjercicios = crearTabla(modeloTabla);

        btnExportar = crearBoton("Exportar a archivo (.txt)");
        btnVolver = crearBoton("Volver");

        // ========== Acomodo ==========
        JPanel centro = new JPanel(new GridBagLayout());
        centro.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;

        c.gridy = 0; c.weighty = 0; c.insets = new Insets(0, 0, 4, 0);
        c.gridx = 0; c.weightx = 0; centro.add(crearEtiqueta("Rutinas guardadas"), c);
        c.gridx = 1; c.weightx = 1; c.insets = new Insets(0, 18, 4, 0);
        centro.add(crearEtiqueta("Ejercicios de la rutina"), c);

        c.gridy = 1; c.weighty = 1; c.insets = new Insets(0, 0, 0, 0);
        c.gridx = 0; c.weightx = 0; centro.add(scrollLista, c);
        c.gridx = 1; c.weightx = 1; c.insets = new Insets(0, 18, 0, 0);
        centro.add(new JScrollPane(tablaEjercicios), c);

        c.gridy = 2; c.weighty = 0; c.insets = new Insets(4, 0, 0, 0);
        c.gridx = 0; c.weightx = 0; centro.add(lblMensaje, c);

        JPanel abajo = new JPanel(new BorderLayout());
        abajo.setOpaque(false);
        abajo.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        abajo.add(btnExportar, BorderLayout.WEST);   // izquierda, como en el prototipo
        abajo.add(btnVolver, BorderLayout.EAST);     // derecha

        JLabel lblTitulo = crearTitulo("Mis rutinas");
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JPanel contenido = new JPanel(new BorderLayout());
        contenido.setOpaque(false);
        contenido.setBorder(BorderFactory.createEmptyBorder(12, 22, 10, 22));
        contenido.add(lblTitulo, BorderLayout.NORTH);
        contenido.add(centro, BorderLayout.CENTER);
        contenido.add(abajo, BorderLayout.SOUTH);
        add(contenido, BorderLayout.CENTER);

        // ========== Eventos ==========
        lstRutinas.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarEjercicios();   // HU-08
            }
        });
        btnExportar.addActionListener(e -> exportar());   // HU-09
        btnVolver.addActionListener(e -> dispose());

        cargarRutinas();
    }

    /**
     * HU-08. Pide al servidor las rutinas del usuario y arma objetos Rutina.
     * El servidor envía "id;nombre;1,5,4#...": los ids se buscan en la lista
     * de todos los ejercicios para conocer su nombre y tipo.
     */
    private void cargarRutinas() {
        List<Ejercicio> todos = pedirEjercicios("TODOS");
        if (todos == null) {
            return;
        }
        String respuesta = enviar("LISTAR_RUTINAS");
        if (!respuesta.startsWith("OK|")) {
            mostrarError(mensajeDe(respuesta));
            return;
        }

        modeloLista.clear();
        String datos = respuesta.substring(3);
        if (!datos.isEmpty()) {
            for (String item : datos.split("#")) {        // cada rutina
                String[] campos = item.split(";");         // id;nombre;1,5,4
                Rutina rutina = new Rutina(Integer.parseInt(campos[0]), campos[1], usuario);
                for (String idTexto : campos[2].split(",")) {
                    int id = Integer.parseInt(idTexto);
                    for (Ejercicio e : todos) {
                        if (e.getId() == id) {
                            rutina.agregarEjercicio(e);
                        }
                    }
                }
                modeloLista.addElement(rutina);
            }
        }

        if (modeloLista.isEmpty()) {
            lblMensaje.setText("Todavía no tiene rutinas guardadas.");
        } else {
            lblMensaje.setText(" ");
            lstRutinas.setSelectedIndex(0);   // muestra la primera rutina
        }
    }

    /**
     * HU-08. Muestra en la tabla los ejercicios de la rutina seleccionada.
     */
    private void mostrarEjercicios() {
        modeloTabla.setRowCount(0);
        Rutina rutina = lstRutinas.getSelectedValue();
        if (rutina == null) {
            return;
        }
        for (Ejercicio e : rutina.getEjercicios()) {
            modeloTabla.addRow(new Object[]{e.getNombre(), e.getTipo()});
        }
    }

    /**
     * HU-09. Pide los pasos de cada ejercicio, deja escoger dónde guardar
     * y escribe el texto de Rutina.generarTexto() en un archivo .txt.
     */
    private void exportar() {
        Rutina seleccionada = lstRutinas.getSelectedValue();
        if (seleccionada == null) {
            mostrarError("Seleccione una rutina para exportar.");
            return;
        }

        // La lista solo trae nombre y tipo; para el archivo hacen falta los pasos
        Rutina completa = new Rutina(seleccionada.getId(), seleccionada.getNombre(), usuario);
        for (Ejercicio e : seleccionada.getEjercicios()) {
            Ejercicio detalle = pedirDetalle(e.getId());
            if (detalle == null) {
                return;   // error de conexión, ya se mostró
            }
            completa.agregarEjercicio(detalle);
        }

        // Ventana para escoger dónde guardar (criterio de HU-09)
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Exportar rutina");
        selector.setFileFilter(new FileNameExtensionFilter("Archivos de texto (*.txt)", "txt"));
        // Nombre sugerido: el de la rutina, sin caracteres que Windows no permite
        String nombreSugerido = seleccionada.getNombre().replaceAll("[\\\\/:*?\"<>|]", "_");
        selector.setSelectedFile(new File(nombreSugerido + ".txt"));

        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;   // el usuario canceló
        }
        File archivo = selector.getSelectedFile();
        if (!archivo.getName().toLowerCase().endsWith(".txt")) {
            archivo = new File(archivo.getParentFile(), archivo.getName() + ".txt");
        }
        if (archivo.exists()) {
            int opcion = JOptionPane.showConfirmDialog(this,
                    "El archivo ya existe. ¿Desea reemplazarlo?", "Fidness",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (opcion != JOptionPane.YES_OPTION) {
                return;
            }
        }

        // Escribe el archivo en UTF-8. Los "\n" se cambian por el salto de línea
        // del sistema (en Windows "\r\n") para que cualquier editor lo lea bien.
        try (PrintWriter escritor = new PrintWriter(new FileWriter(archivo, StandardCharsets.UTF_8))) {
            escritor.print(completa.generarTexto().replace("\n", System.lineSeparator()));
        } catch (IOException ex) {
            mostrarError("No se pudo guardar el archivo: " + ex.getMessage());
            return;
        }
        mostrarInformacion("La rutina se exportó en:\n" + archivo.getAbsolutePath());
    }
}
