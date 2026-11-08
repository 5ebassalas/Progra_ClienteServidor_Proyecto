package modelo;

import java.util.ArrayList;
import java.util.List;

public class Rutina {

    private int id;
    private String nombre;
    private Usuario propietario;
    private List<Ejercicio> ejercicios;

    public Rutina(int id, String nombre, Usuario propietario) {
        this.id = id;
        this.nombre = nombre;
        this.propietario = propietario;
        this.ejercicios = new ArrayList<>();
    }

    // Agrega un ejercicio al final de la rutina (HU-06, botón "Agregar >>").
    public void agregarEjercicio(Ejercicio e) {
        if (e == null) {
            throw new IllegalArgumentException("El ejercicio no puede ser nulo.");
        }
        ejercicios.add(e);
    }

    // Quita el ejercicio con ese id (HU-06, botón "<< Quitar").
    // Si el mismo ejercicio está dos veces, quita solo el primero.
    public void quitarEjercicio(int idEjercicio) {
        for (int i = 0; i < ejercicios.size(); i++) {
            if (ejercicios.get(i).getId() == idEjercicio) {
                ejercicios.remove(i);   // remove(int) quita por posición
                return;
            }
        }
        throw new IllegalArgumentException(
                "La rutina no tiene un ejercicio con id " + idEjercicio + ".");
    }

    // Arma el texto del archivo que se exporta (HU-09):
    // nombre de la rutina y, por cada ejercicio, su nombre, tipo y pasos.
    public String generarTexto() {
        StringBuilder texto = new StringBuilder();
        texto.append("Rutina: ").append(nombre).append("\n");
        if (propietario != null) {
            texto.append("Usuario: ").append(propietario.getNombreCompleto()).append("\n");
        }
        texto.append("Cantidad de ejercicios: ").append(ejercicios.size()).append("\n");
        texto.append("========================================\n");

        int numero = 1;
        for (Ejercicio e : ejercicios) {
            texto.append("\n").append(numero).append(". ").append(e.getNombre()).append("\n");
            texto.append("   Tipo: ").append(e.getTipo()).append("\n");
            texto.append("   Pasos:\n");
            // Cada línea de los pasos va con sangría para que se lea mejor
            for (String linea : e.getPasosEjecucion().split("\n")) {
                texto.append("      ").append(linea).append("\n");
            }
            numero++;
        }
        return texto.toString();
    }

    // [AGREGADO] Sobrescribe toString() de Object (polimorfismo).
    // La lista "Rutinas guardadas" de la Pantalla 6 muestra solo el nombre.
    @Override
    public String toString() {
        return nombre;
    }

    public int getId() { return id; }
    // [AGREGADO] El servidor le asigna el id al guardar la rutina (HU-07).
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public Usuario getPropietario() { return propietario; }
    public List<Ejercicio> getEjercicios() { return ejercicios; }
}
