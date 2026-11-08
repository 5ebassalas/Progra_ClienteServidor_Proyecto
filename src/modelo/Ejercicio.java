package modelo;

public class Ejercicio {

    private int id;
    private String nombre;
    private TipoEjercicio tipo;
    private String descripcion;
    private String pasosEjecucion;

    public Ejercicio(int id, String nombre, TipoEjercicio tipo, String descripcion, String pasosEjecucion) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.pasosEjecucion = pasosEjecucion;
    }

    // Texto corto con nombre y tipo, por ejemplo "Sentadilla (Pierna)".
    public String obtenerResumen() {
        return nombre + " (" + tipo + ")";   // tipo usa el toString() del enum
    }

    // true si el ejercicio es del tipo indicado. Lo usa listarPorTipo (HU-04).
    public boolean tieneTipo(TipoEjercicio tipo) {
        return this.tipo == tipo;   // los valores de un enum se comparan con ==
    }

    // [AGREGADO] Sobrescribe toString() de Object (polimorfismo).
    // Al imprimir un Ejercicio o mostrarlo en una lista, Java usa este método.
    @Override
    public String toString() {
        return obtenerResumen();
    }

    public int getId() { return id; }
    // [AGREGADO] El servidor le asigna el id al registrar un ejercicio nuevo (HU-02).
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public TipoEjercicio getTipo() { return tipo; }
    public String getDescripcion() { return descripcion; }
    public String getPasosEjecucion() { return pasosEjecucion; }
}
