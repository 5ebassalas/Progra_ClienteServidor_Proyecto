package cliente;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import modelo.Usuario;

// Pantalla 2: Menu principal (HU-01, HU-02, HU-03, HU-06, HU-08, HU-10)
// Componentes del prototipo: bienvenida con nombre y rol, y botones Ver ejercicios,
// Crear rutina, Mis rutinas, Registrar ejercicio (solo administrador) y Salir.
public class VentanaMenu extends VentanaBase {

    private Usuario usuario;   // el usuario que inició sesión

    private JButton btnVerEjercicios;
    private JButton btnCrearRutina;
    private JButton btnMisRutinas;
    private JButton btnRegistrarEjercicio;
    private JButton btnSalir;

    public VentanaMenu(ClienteSocket cliente, Usuario usuario) {
        super("Fidness - Menú principal", cliente);
        this.usuario = usuario;
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Cerrar con la X equivale a "Salir": avisa al servidor
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cliente.desconectar();
            }
        });

        // ----- Encabezado: "Bienvenido, <nombre>" y el rol (arriba a la izquierda) -----
        JLabel lblBienvenida = new JLabel("Bienvenido, " + usuario.getNombreCompleto());
        lblBienvenida.setFont(new Font(LETRA, Font.BOLD, 18));
        lblBienvenida.setForeground(AZUL);

        String rol = usuario.esAdministrador() ? "Administrador" : "Cliente";
        JLabel lblRol = new JLabel("Rol: " + rol);
        lblRol.setFont(new Font(LETRA, Font.PLAIN, 11));
        lblRol.setForeground(GRIS_TEXTO);

        JPanel encabezado = new JPanel();
        encabezado.setLayout(new BoxLayout(encabezado, BoxLayout.Y_AXIS));   // uno debajo del otro
        encabezado.setOpaque(false);
        encabezado.setBorder(BorderFactory.createEmptyBorder(14, 22, 0, 0));
        encabezado.add(lblBienvenida);
        encabezado.add(lblRol);
        add(encabezado, BorderLayout.NORTH);

        // ----- Botones: una columna centrada -----
        btnVerEjercicios = crearBoton("Ver ejercicios");
        btnCrearRutina = crearBoton("Crear rutina");
        btnMisRutinas = crearBoton("Mis rutinas");
        btnRegistrarEjercicio = crearBoton("Registrar ejercicio");
        btnSalir = crearBoton("Salir");

        JPanel panelBotones = new JPanel(new GridBagLayout());
        panelBotones.setOpaque(false);
        panelBotones.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 14, 0);

        c.gridy = 0; panelBotones.add(tamano(btnVerEjercicios), c);
        c.gridy = 1; panelBotones.add(tamano(btnCrearRutina), c);
        c.gridy = 2; panelBotones.add(tamano(btnMisRutinas), c);

        // HU-02: solo el administrador ve la opción "Registrar ejercicio"
        if (usuario.esAdministrador()) {
            c.gridy = 3; panelBotones.add(tamano(btnRegistrarEjercicio), c);
        }

        c.gridy = 4; c.insets = new Insets(18, 0, 0, 0);
        panelBotones.add(tamano(btnSalir), c);

        c.gridy = 5; c.weighty = 1;                       // fila vacía que empuja todo hacia arriba
        panelBotones.add(new JLabel(), c);

        add(panelBotones, BorderLayout.CENTER);

        // ----- Eventos -----
        // Cada botón oculta el menú y abre su pantalla. Al cerrar esa pantalla
        // (Volver, Cancelar o la X) el menú vuelve a aparecer.
        btnVerEjercicios.addActionListener(e -> abrir(new VentanaEjercicios(cliente, this)));
        btnCrearRutina.addActionListener(e -> abrir(new VentanaCrearRutina(cliente, this)));
        btnMisRutinas.addActionListener(e -> abrir(new VentanaMisRutinas(cliente, this)));
        btnRegistrarEjercicio.addActionListener(e -> abrir(new VentanaRegistrarEjercicio(cliente, this)));
        btnSalir.addActionListener(e -> salir());
    }

    public Usuario getUsuario() {
        return usuario;
    }

    // Recibe cualquier ventana hija de VentanaBase (polimorfismo)
    private void abrir(VentanaBase ventana) {
        setVisible(false);
        ventana.setVisible(true);
    }

    // Da a los botones del menú el mismo tamaño que en el prototipo
    private JButton tamano(JButton boton) {
        boton.setPreferredSize(new Dimension(320, 40));
        return boton;
    }

    // Botón Salir: avisa al servidor (SALIR), cierra la ventana y termina la aplicación
    private void salir() {
        cliente.desconectar();
        dispose();
        System.exit(0);
    }
}
