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
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import modelo.Usuario;

// Pantalla 1: Inicio de sesion (HU-01, HU-10)
// Componentes del prototipo: título "Fidness", campos Usuario y Contraseña,
// botón Ingresar, etiqueta de error y barra de estado "Conectado al servidor".
public class VentanaLogin extends VentanaBase {

    private JTextField txtUsuario;
    private JPasswordField txtContrasena;
    private JLabel lblError;
    private JButton btnIngresar;

    public VentanaLogin(ClienteSocket cliente) {
        super("Fidness - Iniciar sesión", cliente);   // título, tamaño y barra de estado
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Al cerrar con la X, avisa al servidor antes de salir.
        // Clase anónima que sobrescribe windowClosing (polimorfismo).
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cliente.desconectar();
            }
        });

        // ----- Componentes -----
        JLabel lblTitulo = new JLabel("Fidness", SwingConstants.CENTER);
        lblTitulo.setFont(new Font(LETRA, Font.BOLD, 30));
        lblTitulo.setForeground(AZUL);

        JLabel lblSubtitulo = new JLabel("Consulta de ejercicios y rutinas", SwingConstants.CENTER);
        lblSubtitulo.setFont(new Font(LETRA, Font.PLAIN, 12));
        lblSubtitulo.setForeground(GRIS_TEXTO);

        txtUsuario = new JTextField();
        txtUsuario.setPreferredSize(new Dimension(320, 28));
        txtContrasena = new JPasswordField();            // muestra puntos en vez de letras
        txtContrasena.setPreferredSize(new Dimension(320, 28));

        lblError = new JLabel(" ");                       // un espacio para que ocupe su lugar
        lblError.setFont(new Font(LETRA, Font.PLAIN, 12));
        lblError.setForeground(ROJO);

        btnIngresar = crearBoton("Ingresar");
        btnIngresar.setPreferredSize(new Dimension(320, 32));

        // ----- Acomodo: una sola columna centrada (GridBagLayout) -----
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(55, 0, 0, 0));   // espacio arriba
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridy = 0; c.insets = new Insets(0, 0, 4, 0);   panel.add(lblTitulo, c);
        c.gridy = 1; c.insets = new Insets(0, 0, 22, 0);  panel.add(lblSubtitulo, c);
        c.gridy = 2; c.insets = new Insets(0, 0, 4, 0);   panel.add(crearEtiqueta("Usuario"), c);
        c.gridy = 3; c.insets = new Insets(0, 0, 12, 0);  panel.add(txtUsuario, c);
        c.gridy = 4; c.insets = new Insets(0, 0, 4, 0);   panel.add(crearEtiqueta("Contraseña"), c);
        c.gridy = 5; c.insets = new Insets(0, 0, 8, 0);   panel.add(txtContrasena, c);
        c.gridy = 6; c.insets = new Insets(0, 0, 14, 0);  panel.add(lblError, c);
        c.gridy = 7; c.insets = new Insets(0, 0, 0, 0);   panel.add(btnIngresar, c);

        // Fila vacía que "empuja" todo hacia arriba
        c.gridy = 8; c.weighty = 1;                        panel.add(new JLabel(), c);

        add(panel, BorderLayout.CENTER);

        // ----- Eventos -----
        btnIngresar.addActionListener(e -> ingresar());
        getRootPane().setDefaultButton(btnIngresar);   // Enter también presiona Ingresar
    }

    /**
     * HU-01. Valida los campos, envía LOGIN al servidor y, si es correcto,
     * abre el menú principal con el nombre del usuario.
     */
    private void ingresar() {
        String nombreUsuario = txtUsuario.getText().trim();
        String contrasena = new String(txtContrasena.getPassword());

        // Criterio de HU-01: si hay un campo vacío, avisa y NO envía nada al servidor
        if (nombreUsuario.isEmpty() || contrasena.isEmpty()) {
            lblError.setText("Debe escribir el usuario y la contraseña");
            return;
        }
        // El carácter | separa los campos del protocolo
        if (nombreUsuario.contains("|") || contrasena.contains("|")) {
            lblError.setText("El usuario y la contraseña no pueden tener el carácter |");
            return;
        }

        String respuesta = enviar("LOGIN|" + nombreUsuario + "|" + contrasena);

        if (respuesta.startsWith("OK|")) {
            // Respuesta: OK|id|nombreCompleto|rol
            String[] p = respuesta.split("\\|", -1);
            // En el cliente no se guarda la contraseña: se deja vacía
            Usuario usuario = new Usuario(Integer.parseInt(p[1]), nombreUsuario, "", p[2], p[3]);
            new VentanaMenu(cliente, usuario).setVisible(true);
            dispose();   // cierra el login SIN desconectar (dispose no llama a windowClosing)
        } else {
            // Por ejemplo "Usuario o contraseña incorrectos": no entra al menú
            lblError.setText(mensajeDe(respuesta));
            txtContrasena.setText("");
            txtContrasena.requestFocus();
        }
    }
}
