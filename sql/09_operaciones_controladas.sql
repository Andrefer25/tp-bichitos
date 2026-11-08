USE bichitos_tp4;
START TRANSACTION;
INSERT INTO hogar_transito(nombre_referente,localidad)
VALUES ('MARCADOR_SQL_TP3','TEST');
SET @hogar_demo := LAST_INSERT_ID();
SELECT id,nombre_referente,localidad FROM hogar_transito
WHERE id=@hogar_demo AND nombre_referente='MARCADOR_SQL_TP3';
DELETE FROM hogar_transito
WHERE id=@hogar_demo AND nombre_referente='MARCADOR_SQL_TP3';
SELECT ROW_COUNT() AS filas_borradas;
SELECT COUNT(*) AS restantes FROM hogar_transito WHERE id=@hogar_demo;
COMMIT;
