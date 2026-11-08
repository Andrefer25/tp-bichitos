# Presentación del desarrollo en Java

El prototipo Bichitos Zona Norte se presenta con sus fuentes completas y la aplicación local Swing. La resolución de la entrega 4 del informe final explica el patrón DAO, el acceso a MySQL, las excepciones, la clase abstracta y las estructuras de datos. Las capturas de las seis pantallas corresponden a esta implementación.

## Preparación y ejecución

Abrir la raíz `tp-bichitos` con JDK 16 o superior, mantener MySQL activo y configurar `.env` a partir de `.env.example` cuando corresponda. La instalación de esta computadora ya está preparada. En una instalación nueva, `./run.ps1 -InstallDemo` crea la demo únicamente si el esquema aún no tiene tablas. El arranque normal mantiene `BICHITOS_DEMO_RESET=false` y conserva los datos.

Ejecutar `./run.ps1` en PowerShell, o seleccionar **Bichitos Desktop** en IntelliJ. La clase principal es `org.bichitos.desktop.App`; el directorio de trabajo es la raíz del proyecto y el controlador está en `lib/mysql-connector-j-8.0.33.jar`. La compilación mediante la interfaz de IntelliJ no se verificó; sí se comprobó con `javac` y `java`.

## Explicación del código

1. En `desktop/App.java`, mostrar `main`, la comprobación de la conexión y la creación de `MainFrame` en el hilo de eventos.
2. En `desktop/MainFrame.java`, mostrar el constructor, el menú lateral `JList<String>`, `CardLayout` y las seis vistas. La llamada `views.get(current).refreshData()` demuestra despacho polimórfico a través de `WorkspacePanel`.
3. En `desktop/WorkspacePanel.java`, explicar la clase abstracta, el método `refreshData()`, la herencia de `JPanel`, el constructor protegido con parámetro y la inicialización mediante `super` y `this.frame`.
4. En `repository/Database.java`, mostrar la API pública `open()` y los métodos privados de configuración como ejemplo de encapsulamiento. Las credenciales reales se mantienen fuera del material compartido.
5. En `domain/Eligibility.java`, explicar `LocalDate`, `Period`, `Boolean`, el resultado tipado `Result` y los condicionales para la edad y las verificaciones. En `application/CsvImporter.java`, mostrar el ciclo `while`, los `for`, los contadores y el aislamiento de errores por fila.
6. En `application/WorkflowService.java`, mostrar las operaciones del proceso, los parámetros tipados, las validaciones, `PreparedStatement`, `commit`, `rollback` y `try-with-resources`. En `MainFrame.execute`, explicar `SwingWorker`, el retorno al hilo de interfaz y los mensajes de error.
7. En `repository/VerificacionDao.java`, mostrar `buscarParaVerificar` y `guardar`. El DAO recibe la conexión de `WorkflowService.verify`; el servicio controla la transacción y registra la auditoría.
8. En `repository/DesktopQueries.java`, mostrar `Grid` y `grid`. Un arreglo define las columnas y una lista creada con `ArrayList<Object[]>` contiene las filas, que se convierten a `Object[][]` en `DataTable.fill`.
9. En `DataTable`, mostrar la ordenación con el soporte de Swing y la búsqueda lineal `selectId`. Las listas y mapas almacenan vistas, filas y criterios; la ordenación de la tabla no decide quién adopta.

Las rutas de clases anteriores parten de `src/main/java/org/bichitos/`.

## Recorrido de la aplicación

1. **Resumen:** consultar animales, indicadores e historial.
2. **Importación:** elegir un CSV local y revisar incorporadas, duplicadas, rechazadas y errores. En una demo inicial, `data/postulaciones_flujo_prueba.csv` genera 5/1/2; al reimportar cambian los contadores por el control de duplicados.
3. **Postulaciones:** seleccionar un expediente recibido y registrar la verificación documental.
4. **Evaluaciones:** completar actividades con sus fechas, criterios y observaciones; comparar y registrar una decisión humana con fundamento.
5. **Adopciones:** seleccionar una decisión aprobada, comprobar el compromiso firmado y registrar la entrega. Observar la generación de tres seguimientos.
6. **Seguimientos:** registrar contactos y atender las acciones de alerta.
7. Mostrar el error de una fecha futura de entrevista y comprobar que el formulario conserva las observaciones.

Para repetir el recorrido automático desde un estado conocido, ejecutar `./run.ps1 -Test`. Este comando repone únicamente `bichitos_tp4_test`, ejecuta seis pruebas unitarias y los 32 casos documentados, y regenera la evidencia. El caso CP32 recorre las pantallas de importación, verificación, evaluación, decisión, entrega, contacto y cierre de alerta. CP10 comprueba el rollback de la verificación ante una falla posterior a la primera escritura.

## Evidencia de compilación y ejecución

`evidencias/compilacion_ejecucion_tp4.txt` registra la versión del JDK, la fecha, la compilación de las 14 fuentes principales y las 4 fuentes de pruebas, las seis pruebas unitarias y el arranque de la clase principal real contra MySQL local, con códigos de salida 0. `compilacion_ejecucion_tp4.json` conserva el mismo resultado en formato estructurado.

`StartupSmokeTest` llama a `App.main`, comprueba que la ventana principal esté visible y cierra únicamente esa ventana. Para reproducirlo después de compilar:

```powershell
java '-Dfile.encoding=UTF-8' '-DBICHITOS_DEMO_RESET=false' -cp 'out;lib/mysql-connector-j-8.0.33.jar' org.bichitos.desktop.StartupSmokeTest
```

Los 32 casos de aceptación del TP4 están en `evidencias/resultados_pruebas.json` y `pruebas_ejecutadas.md`. La evidencia actual identifica el esquema bichitos_tp4 y las pruebas en bichitos_tp4_test.

## Alcance del prototipo

La cuenta es de demostración. Siguen pendientes autenticación, permisos, excepciones de elegibilidad autorizadas, alta y edición de animales y resolución completa de devoluciones. Las fichas están precargadas y las decisiones de adopción requieren intervención humana. El esquema operativo es `bichitos_tp4`; el TP2 conserva su proyecto y su base anteriores.

## Repositorio del proyecto final

La versión completa del código y los archivos asociados está en [Andrefer25/tp-bichitos](https://github.com/Andrefer25/tp-bichitos). El informe de la entrega se presenta en MALPICA-ANDRE-AP4.PDF.
