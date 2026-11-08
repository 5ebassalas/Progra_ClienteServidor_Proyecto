package pruebas;

import cliente.ClienteSocket;

// [AGREGADO - TEMPORAL] Prueba de la Etapa 4. No es parte del diagrama.
// Ejecútela dos veces:
//   1) con el servidor APAGADO: conectar() debe devolver false.
//   2) con servidor/ServidorMain.java corriendo: debe mostrar las respuestas.
public class PruebaClienteSocket {

    public static void main(String[] args) {
        ClienteSocket cliente = new ClienteSocket("localhost", 5000);

        System.out.println("Sin conectar todavía -> " + cliente.enviarSolicitud("LISTAR_EJERCICIOS|TODOS"));

        if (!cliente.conectar()) {
            System.out.println("conectar() devolvió false: el servidor no está encendido.");
            System.out.println("Ejecute servidor/ServidorMain.java y vuelva a probar.");
            return;
        }
        System.out.println("conectar() devolvió true");

        // HU-01
        System.out.println("Login malo  -> " + cliente.enviarSolicitud("LOGIN|cliente1|mala"));
        System.out.println("Login bueno -> " + cliente.enviarSolicitud("LOGIN|cliente1|cliente123"));

        // HU-04: la respuesta se separa en partes, como lo harán las ventanas
        String respuesta = cliente.enviarSolicitud("LISTAR_EJERCICIOS|PIERNA");
        System.out.println("Pierna      -> " + respuesta);
        if (respuesta.startsWith("OK|")) {
            String datos = respuesta.substring(3);      // quita "OK|"
            for (String item : datos.split("#")) {      // cada ejercicio
                String[] campos = item.split(";");      // id;nombre;TIPO
                System.out.println("   id=" + campos[0] + "  nombre=" + campos[1] + "  tipo=" + campos[2]);
            }
        }

        // HU-05: los pasos llegan con "\n" y se convierten en saltos de línea
        String[] detalle = cliente.enviarSolicitud("DETALLE_EJERCICIO|4").split("\\|", -1);
        System.out.println("Detalle     -> " + detalle[2] + ":");
        System.out.println(detalle[5].replace("\\n", "\n"));

        cliente.desconectar();   // envía SALIR y cierra
        System.out.println("Después de desconectar -> " + cliente.enviarSolicitud("LISTAR_RUTINAS"));
    }
}
