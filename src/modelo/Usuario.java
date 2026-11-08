package modelo;

public class Usuario {

    private int id;
    private String nombreUsuario;
    private String contrasena;
    private String nombreCompleto;
    private String rol; // "ADMIN" o "CLIENTE"

    public Usuario(int id, String nombreUsuario, String contrasena, String nombreCompleto, String rol) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
        this.nombreCompleto = nombreCompleto;
        this.rol = rol;
    }

    // Revisa si el usuario y la contraseña del login son los de este usuario (HU-01).
    // Lo usa GestorDatos.validarLogin.
    public boolean coincideCredenciales(String nombreUsuario, String contrasena) {
        // equals devuelve false si el parámetro es null, así que no se cae
        return this.nombreUsuario.equals(nombreUsuario)
                && this.contrasena.equals(contrasena);
    }

    // true si el rol es ADMIN. Decide quién ve "Registrar ejercicio" (HU-02).
    public boolean esAdministrador() {
        return "ADMIN".equals(rol);
    }

    public int getId() { return id; }
    public String getNombreUsuario() { return nombreUsuario; }
    public String getContrasena() { return contrasena; }
    public String getNombreCompleto() { return nombreCompleto; }
    public String getRol() { return rol; }
}
