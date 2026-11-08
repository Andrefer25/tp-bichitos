package org.bichitos.repository;

import java.sql.*;

/** Instala datos ficticios exclusivamente cuando bichitos_tp4 todavía está vacío. */
public final class InstallDemo {
    private InstallDemo() { }
    public static void main(String[] args)throws Exception {
        String url=Database.setting("BICHITOS_DB_URL","");
        if(!url.matches("jdbc:mysql://(?:localhost|127\\.0\\.0\\.1)(?::[0-9]+)?/bichitos_tp4(?:\\?.*)?"))
            throw new IllegalStateException("La instalación solo admite bichitos_tp4 en MySQL local.");
        try(Connection db=Database.openServerForDemo();Statement s=db.createStatement();
            ResultSet rs=s.executeQuery("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='bichitos_tp4'")){
            rs.next();if(rs.getInt(1)>0)throw new IllegalStateException("El esquema ya contiene tablas; no se reemplazan datos. Usá el arranque normal.");
        }
        System.setProperty("BICHITOS_DEMO_RESET","true");
        try{DemoDatabase.initialize();}finally{System.clearProperty("BICHITOS_DEMO_RESET");}
    }
}
