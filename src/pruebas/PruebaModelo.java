package pruebas;

import java.util.ArrayList;
import java.util.List;
import modelo.Ejercicio;
import modelo.Rutina;
import modelo.TipoEjercicio;
import modelo.Usuario;

// [AGREGADO - TEMPORAL] Prueba de la Etapa 1. No es parte del diagrama:
// solo comprueba el modelo antes de construir el servidor. Se puede borrar al final.
public class PruebaModelo {

    public static void main(String[] args) {

        System.out.println("=== 1. Usuarios ===");
        Usuario ana = new Usuario(1, "admin", "admin123", "Ana Rojas", "ADMIN");
        Usuario cliente = new Usuario(2, "cliente1", "cliente123", "Sebastián Salas Chinchilla", "CLIENTE");

        System.out.println("Ana con datos correctos:   " + ana.coincideCredenciales("admin", "admin123"));   // true
        System.out.println("Ana con clave incorrecta:  " + ana.coincideCredenciales("admin", "otra"));       // false
        System.out.println("Ana es administradora:     " + ana.esAdministrador());                            // true
        System.out.println("cliente1 es administrador: " + cliente.esAdministrador());                        // false

        System.out.println("\n=== 2. Ejercicios ===");
        Ejercicio sentadilla = new Ejercicio(1, "Sentadilla", TipoEjercicio.PIERNA,
                "Ejercicio para trabajar piernas y glúteos.",
                "1. Párate con los pies a la altura de los hombros.\n"
                + "2. Baja flexionando las rodillas, con la espalda recta.\n"
                + "3. Sube empujando con los talones.");
        Ejercicio remo = new Ejercicio(4, "Remo con barra", TipoEjercicio.ESPALDA,
                "Ejercicio para trabajar la espalda.",
                "1. Sujeta la barra con las manos separadas.\n"
                + "2. Inclina el torso hacia adelante con la espalda recta.\n"
                + "3. Lleva la barra hacia el abdomen y baja despacio.");
        Ejercicio curl = new Ejercicio(5, "Curl de bíceps", TipoEjercicio.BRAZO,
                "Ejercicio para trabajar los bíceps.",
                "1. Toma una mancuerna en cada mano con los brazos estirados.\n"
                + "2. Dobla los codos y sube las mancuernas hacia los hombros.\n"
                + "3. Baja despacio hasta estirar los brazos.");

        // Polimorfismo: la variable es List (interfaz) y el objeto es un ArrayList
        List<Ejercicio> todos = new ArrayList<>();
        todos.add(sentadilla);
        todos.add(remo);
        todos.add(curl);

        // println llama al toString() sobrescrito de Ejercicio
        for (Ejercicio e : todos) {
            System.out.println(e);
        }
        System.out.println("Sentadilla es de pierna:   " + sentadilla.tieneTipo(TipoEjercicio.PIERNA));   // true
        System.out.println("Remo es de pierna:         " + remo.tieneTipo(TipoEjercicio.PIERNA));         // false
        System.out.println("Nombre interno del tipo:   " + TipoEjercicio.PIERNA.name());                  // PIERNA
        System.out.println("Tipo leído de un texto:    " + TipoEjercicio.valueOf("ESPALDA"));             // Espalda

        System.out.println("\n=== 3. Rutina ===");
        Rutina rutina = new Rutina(1, "Rutina de lunes", cliente);
        rutina.agregarEjercicio(sentadilla);
        rutina.agregarEjercicio(remo);
        rutina.agregarEjercicio(curl);
        System.out.println("Rutina (toString): " + rutina);
        System.out.println("Ejercicios: " + rutina.getEjercicios());

        rutina.quitarEjercicio(4);   // quita "Remo con barra" (id 4)
        System.out.println("Después de quitar el id 4: " + rutina.getEjercicios());

        // Manejo de excepciones: id que no está en la rutina y ejercicio nulo
        try {
            rutina.quitarEjercicio(99);
        } catch (IllegalArgumentException ex) {
            System.out.println("Error controlado: " + ex.getMessage());
        }
        try {
            rutina.agregarEjercicio(null);
        } catch (IllegalArgumentException ex) {
            System.out.println("Error controlado: " + ex.getMessage());
        }

        System.out.println("\n=== 4. Texto para exportar (HU-09) ===");
        System.out.println(rutina.generarTexto());
    }
}
