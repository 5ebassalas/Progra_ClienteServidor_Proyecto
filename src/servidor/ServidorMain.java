package servidor;

public class ServidorMain {

    public static void main(String[] args) {
        Servidor servidor = new Servidor(5000); // puerto de ejemplo, se puede cambiar
        servidor.iniciar();
    }
}
