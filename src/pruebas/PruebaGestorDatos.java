package pruebas;

import java.util.List;
import modelo.Ejercicio;
import modelo.Rutina;
import modelo.TipoEjercicio;
import modelo.Usuario;
import servidor.GestorDatos;

// [AGREGADO - TEMPORAL] Prueba de la Etapa 2. No es parte del diagrama.
// Usa la carpeta "datos" del proyecto. Para volver a los datos iniciales,
// borre esa carpeta y vuelva a ejecutar.
public class PruebaGestorDatos {

    public static void main(String[] args) {

        GestorDatos gestor = new GestorDatos();
        gestor.cargarDatos();   // la primera vez crea los archivos

        System.out.println("\n=== HU-01: login ===");
        Usuario ana = gestor.validarLogin("admin", "admin123");
        Usuario cliente = gestor.validarLogin("cliente1", "cliente123");
        Usuario nadie = gestor.validarLogin("cliente1", "mala");
        System.out.println("admin    -> " + ana.getNombreCompleto() + ", admin: " + ana.esAdministrador());
        System.out.println("cliente1 -> " + cliente.getNombreCompleto() + ", admin: " + cliente.esAdministrador());
        System.out.println("Clave incorrecta -> " + nadie);   // null

        System.out.println("\n=== HU-03: todos los ejercicios ===");
        for (Ejercicio e : gestor.listarEjercicios()) {
            System.out.println(e.getId() + ". " + e);
        }

        System.out.println("\n=== HU-04: filtrar por tipo ===");
        System.out.println("Pierna:  " + gestor.listarPorTipo(TipoEjercicio.PIERNA));
        System.out.println("Abdomen: " + gestor.listarPorTipo(TipoEjercicio.ABDOMEN) + " (vacío: la ventana mostrará un aviso)");

        System.out.println("\n=== HU-05: detalle de un ejercicio ===");
        Ejercicio remo = gestor.buscarEjercicio(4);
        System.out.println(remo.getNombre() + " - " + remo.getDescripcion());
        System.out.println(remo.getPasosEjecucion());

        System.out.println("\n=== HU-02: registrar con un campo vacío ===");
        Ejercicio incompleto = new Ejercicio(0, "Plancha", TipoEjercicio.ABDOMEN, "", "1. Apoya los antebrazos.");
        System.out.println("¿Se registró? " + gestor.registrarEjercicio(incompleto));   // false

        System.out.println("\n=== HU-06 y HU-07: guardar una rutina ===");
        Rutina sinEjercicios = new Rutina(0, "Vacía", cliente);
        System.out.println("¿Se guardó la rutina sin ejercicios? " + gestor.guardarRutina(sinEjercicios));   // false

        Rutina rutina = new Rutina(0, "Rutina de lunes", cliente);
        rutina.agregarEjercicio(gestor.buscarEjercicio(1));
        rutina.agregarEjercicio(gestor.buscarEjercicio(5));
        rutina.agregarEjercicio(gestor.buscarEjercicio(4));
        System.out.println("¿Se guardó 'Rutina de lunes'? " + gestor.guardarRutina(rutina)
                + " (id asignado: " + rutina.getId() + ")");

        System.out.println("\n=== HU-08: cada usuario ve solo sus rutinas ===");
        System.out.println("Rutinas de cliente1: " + gestor.listarRutinas(cliente.getId()));
        System.out.println("Rutinas de admin:    " + gestor.listarRutinas(ana.getId()));

        System.out.println("\n=== Persistencia: otro GestorDatos lee los mismos archivos ===");
        GestorDatos otro = new GestorDatos();
        otro.cargarDatos();
        List<Rutina> leidas = otro.listarRutinas(2);
        Rutina ultima = leidas.get(leidas.size() - 1);
        System.out.println("Última rutina leída del archivo: " + ultima + " -> " + ultima.getEjercicios());
        System.out.println("Pasos con saltos de línea recuperados:\n" + otro.buscarEjercicio(1).getPasosEjecucion());
    }
}
