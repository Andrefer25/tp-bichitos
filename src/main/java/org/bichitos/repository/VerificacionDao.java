package org.bichitos.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.bichitos.domain.Eligibility;

/** Acceso a datos de verificación con la conexión de la transacción del servicio. */
public final class VerificacionDao {
    private final Connection db;
    public record Datos(LocalDate nacimiento, Boolean alquila, Boolean cuidado,
                        Boolean consentimiento, String estado, List<String> faltantes) { }

    public VerificacionDao(Connection db) { this.db = db; }

    public Datos buscarParaVerificar(long id) throws SQLException {
        String sql = "SELECT t.fecha_nacimiento,p.alquila,p.tiempo_y_recursos,p.consentimiento_propietario,p.estado,t.tipo_documento,t.numero_documento,t.nombre,t.domicilio,t.localidad,t.correo FROM postulacion p JOIN postulante t ON t.id=p.postulante_id WHERE p.id=? FOR UPDATE";
        try (PreparedStatement st=db.prepareStatement(sql)) {
            st.setLong(1,id);
            try (ResultSet rs=st.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Postulación inexistente");
                List<String> missing=new ArrayList<>();
                String[] names={"tipo de documento","número de documento","nombre","domicilio","localidad","correo"};
                for (int i=0;i<names.length;i++) if (rs.getString(i+6)==null || rs.getString(i+6).isBlank()) missing.add(names[i]);
                return new Datos(rs.getDate(1)==null ? null : rs.getDate(1).toLocalDate(),
                    (Boolean)rs.getObject(2),(Boolean)rs.getObject(3),(Boolean)rs.getObject(4),rs.getString(5),missing);
            }
        }
    }

    public void guardar(long id, Boolean identidad, Boolean domicilio, Boolean consentimiento,
                        long usuario, Eligibility.Result resultado) throws SQLException {
        String sql="INSERT INTO verificacion(postulacion_id,identidad_confirmada,domicilio_confirmado,consentimiento_confirmado,usuario_id) VALUES (?,?,?,?,?) ON DUPLICATE KEY UPDATE identidad_confirmada=VALUES(identidad_confirmada),domicilio_confirmado=VALUES(domicilio_confirmado),consentimiento_confirmado=VALUES(consentimiento_confirmado),fecha=NOW(),usuario_id=VALUES(usuario_id)";
        try (PreparedStatement st=db.prepareStatement(sql)) {
            st.setLong(1,id); ponerBoolean(st,2,identidad); ponerBoolean(st,3,domicilio);
            ponerBoolean(st,4,consentimiento); st.setLong(5,usuario); st.executeUpdate();
        }
        try (PreparedStatement st=db.prepareStatement("UPDATE postulacion SET estado=?,motivo_estado=? WHERE id=?")) {
            st.setString(1,resultado.state()); st.setString(2,resultado.reason()); st.setLong(3,id); st.executeUpdate();
        }
    }
    private static void ponerBoolean(PreparedStatement st,int i,Boolean value) throws SQLException {
        if (value==null) st.setNull(i,Types.BOOLEAN); else st.setBoolean(i,value);
    }
}
