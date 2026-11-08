package modelo;

public enum TipoEjercicio {
    PIERNA,
    ESPALDA,
    BRAZO,
    PECHO,
    ABDOMEN;

    // [AGREGADO] Sobrescribe toString() de Enum (polimorfismo).
    // Muestra "Pierna" en vez de "PIERNA", como en las tablas del prototipo.
    // Para archivos y mensajes se usa name() ("PIERNA") y valueOf("PIERNA").
    @Override
    public String toString() {
        String texto = name().toLowerCase();                              // "pierna"
        return texto.substring(0, 1).toUpperCase() + texto.substring(1);  // "Pierna"
    }
}
