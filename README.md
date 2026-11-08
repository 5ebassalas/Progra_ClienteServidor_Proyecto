# Fidness – Avance 2

Aplicación cliente-servidor en Java para la cadena de gimnasios Fidness.
Los clientes inician sesión, consultan ejercicios por tipo, arman rutinas y las exportan a un archivo de texto.
El servidor atiende a varios clientes a la vez, con un hilo por cliente.

**Curso:** Programación Cliente-Servidor Concurrente, Universidad Fidélitas
**Estudiante:** Sebastián Salas Chinchilla

## Requisitos

- JDK 11 o superior.
- NetBeans, recomendado. Es un proyecto *Java with Ant*.
- No usa librerías externas: solo Java estándar, sockets TCP y Swing.

## Cómo ejecutar en NetBeans

1. **Servidor:** abra `servidor/ServidorMain.java` y use *Run File* (Shift+F6).
   La consola debe mostrar `Servidor Fidness escuchando en el puerto 5000...`. Déjelo abierto.
2. **Cliente:** ejecute el proyecto con **F6**. La clase principal es `cliente.ClienteMain`.
   Cada F6 abre otro cliente, así que puede abrir varios a la vez.
3. Inicie sesión con uno de estos usuarios:

| Usuario    | Contraseña   | Nombre                      | Rol     |
|------------|--------------|-----------------------------|---------|
| `admin`    | `admin123`   | Ana Rojas                   | ADMIN   |
| `cliente1` | `cliente123` | Sebastián Salas Chinchilla  | CLIENTE |

Si el cliente se abre con el servidor apagado, muestra un aviso con la opción de reintentar.

## Cómo ejecutar fuera de NetBeans

Después de *Clean and Build*, NetBeans crea `dist/Fidness.jar`:

```
java -cp dist/Fidness.jar servidor.ServidorMain     (servidor)
java -jar dist/Fidness.jar                          (cada cliente)
```

## Estructura

```
src/
 ├─ modelo/     Usuario, Ejercicio, Rutina, TipoEjercicio
 ├─ servidor/   Servidor, ManejadorCliente (extends Thread), GestorDatos (synchronized), ServidorMain
 ├─ cliente/    ClienteSocket, VentanaBase y las 6 ventanas Swing, ClienteMain
 └─ pruebas/    clases de prueba por etapa (no forman parte del diagrama)
```

## Datos

El servidor guarda los datos en la carpeta `datos/`, dentro del proyecto. Son tres archivos de texto en UTF-8, con los campos separados por `|`:
- `usuarios.txt`
- `ejercicios.txt`
- `rutinas.txt`

La primera vez que se ejecuta, el servidor crea los 2 usuarios y los 6 ejercicios del prototipo.
**Para volver a los datos iniciales, borre la carpeta `datos/`** con el servidor apagado.

## Protocolo

Cada mensaje es una línea. La respuesta empieza con `OK|...` o con `ERROR|mensaje`.

```
LOGIN|usuario|contrasena                            -> OK|id|nombreCompleto|rol
LISTAR_EJERCICIOS|TODOS  (o PIERNA, ESPALDA...)      -> OK|id;nombre;TIPO#id;nombre;TIPO...
DETALLE_EJERCICIO|id                                -> OK|id|nombre|TIPO|descripcion|pasos
REGISTRAR_EJERCICIO|nombre|TIPO|descripcion|pasos   -> OK|Ejercicio guardado correctamente   (solo ADMIN)
GUARDAR_RUTINA|nombre|1,5,4                          -> OK|La rutina quedó registrada
LISTAR_RUTINAS                                      -> OK|id;nombre;1,5,4#...
SALIR                                               -> OK|Hasta luego
```

## Pruebas (paquete `pruebas`)

| Clase                  | Necesita el servidor | Qué prueba |
|------------------------|----------------------|------------|
| `PruebaModelo`         | No | Clases del modelo, `toString()` y excepciones. |
| `PruebaGestorDatos`    | No | Login, filtros y rutinas por usuario en archivos de texto. Agrega una rutina de prueba cada vez que se ejecuta. |
| `PruebaServidor`       | Sí | Todos los mensajes del protocolo, con dos clientes a la vez. |
| `PruebaClienteSocket`  | Sí | La clase `ClienteSocket`, con el servidor encendido o apagado. |
| `PruebaVariosClientes` | Sí | HU-10: 5 clientes al mismo tiempo, más uno que se cae de golpe. |

La relación entre historias de usuario, clases y pantallas está en `TRAZABILIDAD.md`.
