# Modelos de examen – Servlets y sesiones

## MODELO 1 – Biblioteca personal

**Dificultad:** similar a la práctica de la caja fuerte.

### Objetivos

Al finalizar esta actividad, el alumno deberá ser capaz de:

- Crear y gestionar sesiones HTTP.
- Compartir objetos entre distintos servlets mediante sesiones.
- Recibir y validar parámetros de peticiones.
- Implementar operaciones de almacenamiento y consulta.
- Gestionar errores mediante códigos HTTP.

### Enunciado

Se desea desarrollar una aplicación web que permita a cada usuario gestionar su propia biblioteca personal.

Cada usuario tendrá una biblioteca independiente asociada a su sesión.

La aplicación deberá disponer de los siguientes servlets:

**1. ConfigurarBiblioteca (`/config`)**

Recibe los siguientes parámetros:

- `nombre`: nombre de la biblioteca.
- `capacidad`: número máximo de libros que puede almacenar.

Se deberá validar que ambos parámetros sean correctos y que la capacidad sea un número entero positivo.

Se creará una biblioteca y se almacenará en la sesión del usuario.

**2. AnadirLibro (`/anadir`)**

Permite añadir un libro a la biblioteca.

Recibe:

- `titulo`: título del libro.
- `autor`: autor del libro.

Condiciones:

- Debe existir una sesión y una biblioteca configurada.
- No se podrán añadir libros si se ha alcanzado la capacidad máxima.
- El título y el autor no pueden ser nulos ni estar vacíos.
- Si todo es correcto, el libro se almacenará en la biblioteca.

**3. ConsultarLibro (`/consultar`)**

Recibe el parámetro `titulo`.

Busca el libro correspondiente y devuelve su autor.

Si no existe, deberá responder con un error HTTP adecuado.

**4. ConsultarBiblioteca (`/biblioteca`)**

Devuelve el nombre de la biblioteca, su capacidad y el número actual de libros almacenados.

### Requisitos adicionales

- Cada sesión debe tener su propia biblioteca.
- No se debe permitir añadir ni consultar libros antes de configurar la biblioteca.
- Se deberán validar todos los parámetros recibidos.
- Se utilizarán respuestas HTTP para comunicar los errores.

---

## MODELO 2 – Sistema de reservas de cine

**Dificultad:** intermedia.

### Objetivos

- Gestionar objetos almacenados en sesiones.
- Controlar estados mediante atributos de objetos.
- Validar parámetros numéricos.
- Implementar operaciones de reserva y cancelación.
- Controlar las condiciones de acceso a los servlets.

### Enunciado

Se desea desarrollar una aplicación web para gestionar reservas de entradas de cine.

Cada usuario podrá crear una sesión de reserva y seleccionar películas y asientos.

La aplicación deberá disponer de los siguientes servlets:

**1. CrearReserva (`/crear`)**

Recibe:

- `pelicula`: nombre de la película.
- `sesion`: hora de la sesión.
- `plazas`: número total de plazas disponibles.

Se deberán validar los parámetros y comprobar que el número de plazas sea un entero positivo.

Se creará un objeto `Reserva` y se almacenará en la sesión.

**2. ReservarEntrada (`/reservar`)**

Recibe:

- `asiento`: número del asiento que se desea reservar.

Condiciones:

- Debe existir una sesión con una reserva configurada.
- El número de asiento debe ser un entero válido.
- El asiento debe estar dentro del rango disponible.
- No se podrá reservar un asiento que ya esté ocupado.

Si la reserva se realiza correctamente, el asiento quedará ocupado.

**3. CancelarEntrada (`/cancelar`)**

Recibe:

- `asiento`: número del asiento que se desea liberar.

Condiciones:

- Debe existir una reserva configurada.
- El asiento debe ser válido y estar reservado.
- Si se cumplen las condiciones, se liberará el asiento.

**4. ConsultarReserva (`/consultar`)**

Devuelve:

- Nombre de la película.
- Hora de la sesión.
- Número total de plazas.
- Número de plazas ocupadas.
- Número de plazas disponibles.

### Requisitos adicionales

- Cada usuario tendrá su propia reserva.
- No se podrá reservar ni cancelar antes de crear una reserva.
- No se podrán realizar operaciones sobre asientos inexistentes.
- Se deberán controlar los errores de conversión numérica.
- Las modificaciones deberán mantenerse entre peticiones.

---

## MODELO 3 – Gestor de tareas personales

**Dificultad:** intermedia-alta.

### Objetivos

- Diseñar clases de modelo con varios atributos.
- Gestionar colecciones de objetos dentro de sesiones.
- Implementar operaciones de alta, consulta y modificación.
- Validar parámetros y controlar estados.
- Gestionar errores HTTP de forma adecuada.

### Enunciado

Se desea desarrollar una aplicación web que permita a cada usuario gestionar sus propias tareas pendientes.

Cada usuario dispondrá de una lista de tareas independiente asociada a su sesión.

Cada tarea tendrá:

- `id`: identificador numérico.
- `titulo`: título de la tarea.
- `descripcion`: descripción.
- `prioridad`: número del 1 al 3.
- `completada`: indica si la tarea está finalizada.

La aplicación deberá disponer de los siguientes servlets:

**1. CrearLista (`/crear`)**

Recibe:

- `maxTareas`: número máximo de tareas que podrá almacenar el usuario.

Se deberá validar que el parámetro sea un entero positivo.

Se creará una lista de tareas y se almacenará en la sesión.

**2. AnadirTarea (`/anadir`)**

Recibe:

- `titulo`
- `descripcion`
- `prioridad`

Condiciones:

- Debe existir una sesión y una lista configurada.
- No se podrán superar las tareas máximas permitidas.
- El título y la descripción no pueden estar vacíos.
- La prioridad debe ser un número entero entre 1 y 3.
- El identificador se asignará automáticamente.

**3. CompletarTarea (`/completar`)**

Recibe:

- `id`: identificador de la tarea.

Busca la tarea correspondiente y modifica su estado para marcarla como completada.

Si el identificador no es válido o no existe ninguna tarea con ese ID, deberá responder con un error HTTP adecuado.

**4. ConsultarTarea (`/consultar`)**

Recibe:

- `id`: identificador de la tarea.

Devuelve toda la información de la tarea solicitada.

**5. ConsultarLista (`/lista`)**

Devuelve un resumen de todas las tareas del usuario, incluyendo:

- Número total de tareas.
- Número de tareas completadas.
- Número de tareas pendientes.

### Requisitos adicionales

- Cada usuario tendrá su propia lista de tareas.
- Los identificadores no se podrán repetir.
- No se podrán consultar ni modificar tareas antes de configurar la lista.
- Se deberán validar todos los parámetros.
- Se deberán gestionar los errores de conversión y de búsqueda.
- Los cambios realizados en las tareas deberán mantenerse entre peticiones.

---

# Condiciones comunes para los tres modelos

1. Utilizar Java Servlets (`HttpServlet`).
2. Utilizar `@WebServlet` para definir las rutas.
3. Implementar las operaciones mediante `doGet`.
4. Utilizar sesiones HTTP para almacenar los objetos principales.
5. Crear las clases de modelo necesarias.
6. No utilizar bases de datos: toda la información se almacenará en memoria.
7. Validar los parámetros recibidos y responder adecuadamente ante errores.
8. No utilizar variables estáticas para almacenar información de los usuarios.

**Consejo:** intenta resolver cada modelo sin consultar el código de la caja fuerte. Puedes consultar tus apuntes de Java y Servlet, pero procura diseñar tú mismo las clases y decidir qué métodos necesitas.