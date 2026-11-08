# Bichitos Zona Norte aplicación local Swing

Versión independiente que corrige la arquitectura de la entrega 2. Java Swing, MySQL 8 y Connector/J 8.0.33. El caso, los usuarios y los datos de demostración son ficticios. No requiere navegador ni servidor HTTP.

## Inicio

Requisitos: JDK 16 o superior, MySQL 8 y una cuenta local con permisos para crear y usar `bichitos_tp4`. Para ejecutar las pruebas, esa cuenta necesita permisos sobre `bichitos_tp4_test`.

1. Abrir esta carpeta en IntelliJ o PowerShell. El JAR está en `lib/mysql-connector-j-8.0.33.jar`.
2. Copiar `.env.example` a `.env` si falta. Completar usuario y contraseña locales. La URL apunta a `jdbc:mysql://127.0.0.1:3306/bichitos_tp4?serverTimezone=America%2FArgentina%2FBuenos_Aires`. No compartir `.env`.
3. Solo para la primera instalación: `./run.ps1 -InstallDemo`. Crea 18 tablas y carga seis animales y siete postulaciones ficticias. Se niega a reemplazar un esquema que ya tenga tablas. La instalación de esta PC ya está preparada.
4. Arranque normal: `./run.ps1`. `BICHITOS_DEMO_RESET=false` conserva los registros entre arranques. La clase principal es `org.bichitos.desktop.App`.

La configuración de IntelliJ **Bichitos Desktop** está en `.run/`. Usa el módulo `tp-bichitos` y la raíz del proyecto como directorio de trabajo. No se ejecutó un build mediante la interfaz de IntelliJ; sí javac y la clase principal real. Si IntelliJ solicita un SDK, seleccionar el JDK instalado y comprobar la biblioteca del módulo.

La presentación del proyecto final se organiza en [PRESENTACION.md](PRESENTACION.md): explicación del código, POO, menú, recorrido de la aplicación e instrucciones reproducibles. La compilación y el arranque verificados para esta entrega están registrados en `evidencias/compilacion_ejecucion_tp4.txt` y `compilacion_ejecucion_tp4.json`.

## Uso del prototipo

La navegación entre las seis secciones se realiza desde el menú lateral. Se retiró el menú superior «Secciones» por duplicar esos accesos.

- **Resumen:** animales, indicadores de pendientes e historial del animal seleccionado.
- **Importación:** elegir `data/postulaciones_flujo_prueba.csv`. En la demo inicial: 5 incorporadas, 1 duplicada y 2 rechazadas. El separador es `;`, la codificación UTF-8 y el límite 2.000.000 bytes. Se informa el error por fila; los datos se corrigen en el origen.
- **Postulaciones:** abrir un expediente y verificar identidad, domicilio y consentimiento cuando corresponda. No se almacenan fotografías de documentos.
- **Evaluaciones:** abrir el expediente desde Postulaciones, completar las tres actividades con fechas y los cuatro criterios aplicables, comparar y registrar una decisión fundada. Al seleccionar otra candidatura cambian el ID, el nombre y el formulario. No hay puntaje ni selección automática.
- **Adopciones:** seleccionar una decisión aprobada y registrar fecha de entrega y compromiso firmado. Se generan los contactos inicial de 2–3 días, día 15 y día 30.
- **Seguimientos:** elegir una tarea, registrar canal, resultado y observaciones. Sin respuesta y acompañamiento generan contacto EXTRA; alerta y posible devolución requieren coordinación. Las acciones se cierran con una explicación.

Las tablas permiten desplazamiento horizontal y muestran el contenido completo de las celdas como ayuda al pasar el cursor. Los formularios extensos tienen desplazamiento vertical. Todas las operaciones JDBC y de archivos se ejecutan con SwingWorker fuera del hilo de eventos. Una validación fallida conserva el contenido ingresado.

## Pruebas y evidencia

`./run.ps1 -Test` compila y ejecuta seis pruebas unitarias y 32 casos de aceptación. Las escrituras de aceptación y la reposición de fixtures se limitan a `bichitos_tp4_test`; no se modifica `bichitos_tp2` ni la demo operativa `bichitos_tp4`. El caso CP10 fuerza un fallo después de guardar la verificación y comprueba el rollback. CP32 recorre la aplicación Swing de principio a fin.

Los casos, sus entradas, procedimientos, resultados esperados y obtenidos están en `evidencias/resultados_pruebas.json` y `evidencias/pruebas_ejecutadas.md`. Las capturas de las seis pantallas y de validación/flujo están en `evidencias/capturas/`. `StartupSmokeTest` comprueba el arranque de la clase principal real y cierra únicamente su ventana.

```powershell
java '-Dfile.encoding=UTF-8' -cp 'out;lib/mysql-connector-j-8.0.33.jar' org.bichitos.desktop.StartupSmokeTest
```

Los IDs autoincrementales de prueba pueden variar entre ejecuciones. Los resultados observados se regeneran al volver a probar. La fecha de las capturas corresponde a la ejecución registrada.

## Persistencia y comunicación

Las pantallas llaman métodos Java. JDBC es la API; Connector/J implementa el acceso al protocolo MySQL sobre TCP/IP por loopback al puerto 3306. No hay recorrido por Ethernet ni Wi-Fi. Forms y Sheets pueden utilizarse externamente para capturar y exportar; no existe sincronización automática.

`sql/01_schema.sql` contiene el DDL completo. `02_seed.sql` es una carga mínima alternativa; `07_rehidratar_demo.sql` es la carga completa utilizada por el instalador. `03_consultas.sql`, `04_delete_seguro.sql` y `08_consultas_demo.sql` documentan inserción, consulta y borrado controlado. Las decisiones compuestas usan transacciones y PreparedStatement.

La reposición destructiva mediante `BICHITOS_DEMO_RESET=true` es exclusivamente una herramienta explícita de demostración y no es el arranque normal. La migración heredada y la reposición de datos están restringidas al esquema local nuevo; las pruebas poseen su propio guardado de seguridad para el esquema `_test`.

## Límites declarados

La cuenta #1 es de demostración: no hay autenticación ni permisos por rol implementados. RF01/CU10 solo tienen fichas precargadas consultables; no hay alta o edición de animales. No se implementan excepciones de elegibilidad autorizadas (RF07), gestión de usuarios (RF18), copias automatizadas ni resolución completa de devoluciones. El prototipo registra `DEVOLUCION_PENDIENTE`; cerrar una acción no equivale a resolver la devolución. No se afirma validación con usuarios reales ni mediciones de carga de producción.

El repositorio TP2 publicado anteriormente corresponde a la versión web histórica. Esta copia local todavía no se publicó en GitHub.

## Repositorio del proyecto final

La versión completa del código y los archivos asociados está en [Andrefer25/tp-bichitos](https://github.com/Andrefer25/tp-bichitos). El informe de la entrega se presenta en MALPICA-ANDRE-AP4.PDF.
