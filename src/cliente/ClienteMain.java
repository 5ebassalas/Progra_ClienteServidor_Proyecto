package cliente;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class ClienteMain {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Una sola conexión para toda la aplicación cliente
            ClienteSocket cliente = new ClienteSocket("localhost", 5000);

            // Si el servidor no está encendido, pregunta si se quiere reintentar
            while (!cliente.conectar()) {
                int opcion = JOptionPane.showConfirmDialog(null,
                        "No se pudo conectar con el servidor.\n"
                        + "Verifique que el servidor esté encendido.\n\n"
                        + "¿Desea intentar de nuevo?",
                        "Fidness", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
                if (opcion != JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }

            new VentanaLogin(cliente).setVisible(true);
        });
    }
}
