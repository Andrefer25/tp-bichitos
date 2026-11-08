package org.bichitos.repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/** Vistas de lectura de escritorio. Ninguna pantalla construye consultas SQL. */
public final class DesktopQueries {
    public record Grid(String[] columns, List<Object[]> rows) { }
    public record ApplicationInfo(long id, long animalId, String person, String animal,
            String state, String animalState, String details, String needs,
            Boolean identity, Boolean address, Boolean consent) {
        public boolean canVerify() { return Set.of("RECIBIDA","PENDIENTE","ELEGIBLE").contains(state); }
        public boolean canEvaluate() { return animalState.equals("APTO") && Set.of("ELEGIBLE","EVALUADA").contains(state); }
        public boolean canDecide() { return animalState.equals("APTO") && Set.of("EVALUADA","EN_ESPERA").contains(state); }
    }
    public record Criterion(long id, String name, String value, String notes) { }
    public record Evaluation(String interview, String home, String meeting, String notes,
            Map<String,LocalDate> dates, List<Criterion> criteria) { }

    private final CaseQueries queries = new CaseQueries();

    private Grid grid(String[] columns, String sql, long... ids) throws SQLException {
        var source = queries.rows(sql, ids);
        List<Object[]> data = new ArrayList<>();
        for (var row : source) {
            if(row.size()!=columns.length)throw new SQLException("La vista de lectura no coincide con sus columnas");
            data.add(row.values().toArray());
        }
        return new Grid(columns, data);
    }
    public Grid animals() throws SQLException {
        return grid(new String[]{"ID","Animal","Especie","Estado","Necesidades"},
            "SELECT id,nombre,especie,estado,necesidades_cuidado FROM animal ORDER BY id");
    }
    public Grid applications() throws SQLException {
        return grid(new String[]{"ID","Animal","Postulante","Estado","Motivo"},
            "SELECT p.id,a.nombre AS animal,t.nombre AS persona,p.estado,p.motivo_estado FROM postulacion p JOIN animal a ON a.id=p.animal_id JOIN postulante t ON t.id=p.postulante_id ORDER BY p.id");
    }
    public ApplicationInfo application(long id) throws SQLException {
        var r=queries.one("SELECT p.id,p.animal_id,t.nombre AS persona,a.nombre AS animal,p.estado,a.estado AS animal_estado,p.motivo_estado,a.necesidades_cuidado,t.tipo_documento,t.numero_documento,t.fecha_nacimiento,t.domicilio,t.localidad,t.telefono,t.correo,p.alquila,p.consentimiento_propietario,p.tiempo_y_recursos,v.identidad_confirmada,v.domicilio_confirmado,v.consentimiento_confirmado FROM postulacion p JOIN animal a ON a.id=p.animal_id JOIN postulante t ON t.id=p.postulante_id LEFT JOIN verificacion v ON v.postulacion_id=p.id WHERE p.id=?",id);
        if(r==null)throw new IllegalArgumentException("Postulación inexistente");
        String details="Documento: "+r.get("tipo_documento")+" "+r.get("numero_documento")
            +"   | Nacimiento: "+r.get("fecha_nacimiento")+"\nDomicilio: "+r.get("domicilio")+", "+r.get("localidad")
            +"\nContacto: "+r.get("telefono")+" / "+r.get("correo")
            +"\nAlquila: "+yes(r.get("alquila"))+" | Consentimiento declarado: "+yes(r.get("consentimiento_propietario"))
            +" | Tiempo y recursos: "+yes(r.get("tiempo_y_recursos"))+"\nSituación: "+r.get("motivo_estado");
        return new ApplicationInfo(id,number(r,"animal_id"),str(r,"persona"),str(r,"animal"),str(r,"estado"),str(r,"animal_estado"),details,str(r,"necesidades_cuidado"),bool(r.get("identidad_confirmada")),bool(r.get("domicilio_confirmado")),bool(r.get("consentimiento_confirmado")));
    }
    public Evaluation evaluation(long postId,long animalId) throws SQLException {
        var r=queries.one("SELECT id,entrevista,vivienda,encuentro,observaciones FROM evaluacion WHERE postulacion_id=?",postId);
        long evalId=r==null?0:number(r,"id");
        List<Criterion> criteria=new ArrayList<>();
        for(var c:queries.rows("SELECT c.id,c.nombre,ec.valoracion,ec.observaciones FROM criterio c JOIN animal_criterio ac ON ac.criterio_id=c.id LEFT JOIN evaluacion_criterio ec ON ec.criterio_id=c.id AND ec.evaluacion_id=? WHERE ac.animal_id=? AND c.activo=TRUE ORDER BY c.id",evalId,animalId))
            criteria.add(new Criterion(number(c,"id"),str(c,"nombre"),str(c,"valoracion"),str(c,"observaciones")));
        Map<String,LocalDate> dates=new LinkedHashMap<>();
        for(var a:queries.rows("SELECT tipo,fecha FROM evaluacion_actividad WHERE evaluacion_id=?",evalId))dates.put(str(a,"tipo"),LocalDate.parse(str(a,"fecha")));
        return new Evaluation(r==null?"":str(r,"entrevista"),r==null?"":str(r,"vivienda"),r==null?"":str(r,"encuentro"),r==null?"":str(r,"observaciones"),dates,criteria);
    }
    public Grid comparison(long animalId) throws SQLException {
        return grid(new String[]{"Postulación","Postulante","Estado","Entrevista","Vivienda","Encuentro","Criterios y observaciones"},
            "SELECT p.id,t.nombre,p.estado,e.entrevista,e.vivienda,e.encuentro,(SELECT GROUP_CONCAT(CONCAT(c.nombre,': ',ec.valoracion,' - ',ec.observaciones) SEPARATOR '; ') FROM evaluacion_criterio ec JOIN criterio c ON c.id=ec.criterio_id WHERE ec.evaluacion_id=e.id) AS criterios FROM postulacion p JOIN postulante t ON t.id=p.postulante_id LEFT JOIN evaluacion e ON e.postulacion_id=p.id WHERE p.animal_id=? ORDER BY p.id",animalId);
    }
    public Grid decisions() throws SQLException {
        return grid(new String[]{"ID decisión","Postulación","Animal","Postulante","Resultado","Fundamento","Adopción","Estado adopción"},
            "SELECT d.id AS decision_id,p.id AS postulacion_id,a.nombre AS animal,t.nombre AS persona,d.resultado,d.fundamento,ad.id AS adopcion_id,ad.estado AS estado_adopcion FROM decision d JOIN postulacion p ON p.id=d.postulacion_id JOIN animal a ON a.id=p.animal_id JOIN postulante t ON t.id=p.postulante_id LEFT JOIN adopcion ad ON ad.decision_id=d.id ORDER BY d.id DESC");
    }
    public Grid followups() throws SQLException {
        return grid(new String[]{"ID","Adopción","Animal","Adoptante","Tipo","Programada","Límite","Contacto","Resultado","Canal","Observaciones","Situación"},
            "SELECT s.id,s.adopcion_id,a.nombre AS animal,t.nombre AS persona,s.tipo,s.fecha_programada,s.fecha_limite,s.fecha_contacto,s.resultado,s.canal,s.observaciones,CASE WHEN s.fecha_contacto IS NOT NULL THEN 'REGISTRADO' WHEN s.fecha_limite<CURRENT_DATE() THEN 'VENCIDO' ELSE 'PENDIENTE' END AS situacion FROM seguimiento s JOIN adopcion ad ON ad.id=s.adopcion_id JOIN decision d ON d.id=ad.decision_id JOIN postulacion p ON p.id=d.postulacion_id JOIN animal a ON a.id=p.animal_id JOIN postulante t ON t.id=p.postulante_id ORDER BY (s.fecha_contacto IS NOT NULL),s.fecha_limite,s.id");
    }
    public Grid alerts() throws SQLException {
        return grid(new String[]{"ID","Seguimiento","Motivo","Acción","Estado","Límite","Responsable"},
            "SELECT al.id,al.seguimiento_id,al.motivo,al.accion,al.estado,al.fecha_limite,u.nombre FROM accion_alerta al JOIN usuario u ON u.id=al.responsable_id ORDER BY al.estado,al.fecha_limite,al.id");
    }
    public Grid batches() throws SQLException {
        return grid(new String[]{"ID","Archivo","Inicio","Incorporadas","Duplicadas","Rechazadas"},
            "SELECT id,archivo,inicio,incorporadas,duplicadas,fallidas FROM importacion_lote ORDER BY id DESC");
    }
    public Grid batchErrors(long batchId) throws SQLException {
        return grid(new String[]{"Fila","ID origen","Motivo del rechazo"},
            "SELECT numero_fila,id_origen,detalle FROM importacion_error WHERE lote_id=? ORDER BY numero_fila",batchId);
    }
    public Grid history(long animalId) throws SQLException {
        return grid(new String[]{"Fecha","Evento","Usuario","Detalle"},
            "SELECT au.fecha,au.evento,u.nombre,au.detalle FROM auditoria_evento au LEFT JOIN usuario u ON u.id=au.usuario_id WHERE (au.entidad='postulacion' AND au.entidad_id IN (SELECT id FROM postulacion WHERE animal_id=?)) OR (au.entidad='evaluacion' AND au.entidad_id IN (SELECT e.id FROM evaluacion e JOIN postulacion p ON p.id=e.postulacion_id WHERE p.animal_id=?)) OR (au.entidad='decision' AND au.entidad_id IN (SELECT d.id FROM decision d JOIN postulacion p ON p.id=d.postulacion_id WHERE p.animal_id=?)) OR (au.entidad='adopcion' AND au.entidad_id IN (SELECT ad.id FROM adopcion ad JOIN decision d ON d.id=ad.decision_id JOIN postulacion p ON p.id=d.postulacion_id WHERE p.animal_id=?)) OR (au.entidad='seguimiento' AND au.entidad_id IN (SELECT s.id FROM seguimiento s JOIN adopcion ad ON ad.id=s.adopcion_id JOIN decision d ON d.id=ad.decision_id JOIN postulacion p ON p.id=d.postulacion_id WHERE p.animal_id=?)) OR (au.entidad='accion_alerta' AND au.entidad_id IN (SELECT al.id FROM accion_alerta al JOIN seguimiento s ON s.id=al.seguimiento_id JOIN adopcion ad ON ad.id=s.adopcion_id JOIN decision d ON d.id=ad.decision_id JOIN postulacion p ON p.id=d.postulacion_id WHERE p.animal_id=?)) ORDER BY au.fecha,au.id",animalId,animalId,animalId,animalId,animalId,animalId);
    }
    public long coordinator() throws SQLException {
        var r=queries.one("SELECT id FROM usuario WHERE rol='COORDINACION' AND activo=TRUE ORDER BY id LIMIT 1");
        if(r==null)throw new IllegalStateException("No hay coordinación activa para asignar la alerta");
        return number(r,"id");
    }
    public String overview() throws SQLException {
        var r=queries.one("SELECT (SELECT COUNT(*) FROM animal) AS animales,(SELECT COUNT(*) FROM postulacion) AS postulaciones,(SELECT COUNT(*) FROM seguimiento WHERE fecha_contacto IS NULL AND fecha_limite<CURRENT_DATE()) AS vencidos,(SELECT COUNT(*) FROM accion_alerta WHERE estado='ABIERTA') AS alertas");
        return "Animales: "+r.get("animales")+"     Postulaciones: "+r.get("postulaciones")+"     Seguimientos vencidos: "+r.get("vencidos")+"     Alertas abiertas: "+r.get("alertas");
    }
    private static long number(Map<String,Object> r,String key){return ((Number)r.get(key)).longValue();}
    private static String str(Map<String,Object> r,String key){return r.get(key)==null?"":r.get(key).toString();}
    private static Boolean bool(Object v){return v==null?null:v instanceof Boolean?(Boolean)v:((Number)v).intValue()!=0;}
    private static String yes(Object v){return v==null?"Pendiente":Boolean.TRUE.equals(bool(v))?"Sí":"No";}
}
