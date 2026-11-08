# Pruebas ejecutadas del proyecto final

Ejecución del 2026-11-08T17:10:28-03:00. Los 32 casos se comprobaron en bichitos_tp4_test.

| Caso | Prueba | Resultado obtenido | Estado |
|---|---|---|---|
| CP01 | Creación del esquema | 18 tablas InnoDB creadas | APROBADO |
| CP02 | Edad inferior a 21 | NO_ELEGIBLE | APROBADO |
| CP03 | Edad exactamente 21 | ELEGIBLE; no implica adopción automática | APROBADO |
| CP04 | Importación mixta | 5 incorporadas, 1 duplicada y 2 rechazadas; 2 errores registrados | APROBADO |
| CP05 | Reimportación del mismo archivo | 0 incorporadas, 6 duplicadas, 2 rechazadas | APROBADO |
| CP06 | Datos incompletos y animal inexistente | 0 postulaciones inválidas; datos faltantes y animal inexistente registrados en cada lote | APROBADO |
| CP07 | Duplicado persona-animal y animal no apto | 0 incorporadas; 1 duplicada persona-animal; 1 rechazada por ADOPTADO | APROBADO |
| CP08 | Archivo mayor al límite | Archivo rechazado; no se creó lote | APROBADO |
| CP09 | Verificación documental | Postulación #7 ELEGIBLE; verificación y auditoría guardadas | APROBADO |
| CP10 | Rollback de verificación | El error posterior a la primera escritura revirtió la verificación y conservó el estado | APROBADO |
| CP11 | Evaluación incompleta | Rechazo por criterios incompletos; 0 evaluaciones parciales | APROBADO |
| CP12 | Evaluaciones independientes | #7 EVALUADA con 4 criterios y 3 actividades; evaluación de #4 sin cambios | APROBADO |
| CP13 | Fundamento obligatorio | Rechazo; ninguna decisión agregada | APROBADO |
| CP14 | Decisión humana fundada | Decisión #14 APROBADA con fundamento y auditoría | APROBADO |
| CP15 | Bloqueo de regresión de estado | Reverificación bloqueada; estado APROBADA conservado | APROBADO |
| CP16 | Una aprobación por animal | Una aprobación para Luna; segundo intento bloqueado | APROBADO |
| CP17 | Entrega sin compromiso | Rechazo; cantidad de adopciones sin cambios | APROBADO |
| CP18 | Entrega y agenda transaccional | Adopción #10; tres tareas con fechas correctas; Mora ADOPTADO | APROBADO |
| CP19 | Entrega duplicada | Segunda entrega bloqueada; una adopción | APROBADO |
| CP20 | Alerta sin responsable y rollback | Rollback completo: contacto pendiente; 0 alertas parciales | APROBADO |
| CP21 | Alerta y contacto extraordinario | Contacto guardado; alerta abierta asignada a #2; tarea EXTRA creada | APROBADO |
| CP22 | Cierre de acción | Acción cerrada con fecha y auditoría | APROBADO |
| CP23 | Sin respuesta y posible devolución | Reintento y alerta registrados; devolución pendiente de resolución completa | APROBADO |
| CP24 | Integridad referencial | MySQL 1452; fila rechazada | APROBADO |
| CP25 | INSERT, SELECT y DELETE controlado | INSERT 1; SELECT 1; DELETE 1; 0 restantes | APROBADO |
| CP26 | Conservación entre arranques | Dos arranques conservan decisiones y datos existentes | APROBADO |
| CP27 | Error de conexión | Error de conexión detectado y URL restaurada | APROBADO |
| CP28 | Navegación y seis pantallas | Seis pantallas cargadas; capturas 01 a 06 generadas | APROBADO |
| CP29 | Correspondencia expediente-formulario | #5 Diego y #4 Clara corresponden a sus respectivos formularios | APROBADO |
| CP30 | Error de validación conserva formulario | Mensaje Fecha inválida; notas conservadas; evaluación no modificada | APROBADO |
| CP31 | Etapas cerradas en solo consulta | Controles de verificación, evaluación y decisión deshabilitados | APROBADO |
| CP32 | Recorrido completo desde Swing | Importación 5/1/2; #7 verificada, evaluada y aprobada; entrega con 3 tareas; contacto, alerta y cierre persistidos desde Swing | APROBADO |
