/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package botsqldumper;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import com.ciberado.botserver.model.Taxon;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.hibernate.jdbc.Work;
/**
 *
 * @author ciberado
 */
public class OfflineDataDumper {
    private static final Logger log = LogManager.getLogger(OfflineDataDumper.class);

    public static void main(String[] args) throws IOException {
        
        org.apache.log4j.BasicConfigurator.configure();
        org.apache.log4j.LogManager.getRootLogger().setLevel(Level.WARN);
        EntityManager em = null;
        EntityTransaction tx = null;

        EntityManagerFactory factory =
                Persistence.createEntityManagerFactory("data");

        try {
            
            ObjectMapper mapper = new ObjectMapper(); 
            mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
            
            em = factory.createEntityManager();
            
            org.hibernate.Session session =
                    (org.hibernate.Session) em.getDelegate();
            
            org.hibernate.ScrollableResults scroll =
                    // session.createQuery("from Taxon as t left join fetch t.resources order by t.code").scroll();
                    session.createQuery("select distinct t from Specimen as s "
                                      + "join s.taxon as t left join t.resources").scroll();
            int fila = 0;
            while (scroll.next() == true) {
                Taxon taxon = (Taxon) scroll.get(0);
                String taxonJSON = mapper.writeValueAsString(taxon).replaceAll("'", "\\\\'");
                System.out.println(MessageFormat.format("i([''{0}'', ''{1}'']);", taxon.getCode(), taxonJSON));
                session.flush();
                session.clear();
                fila = fila + 1;
            }
            System.out.println("*** " + fila);
            

            session.doWork(new Work() {
                @Override
                public void execute(Connection con) throws SQLException {                    
                    String sql = "SELECT i.codi_ind, i.codi_esp, i.lat, i.lon "
                            + "   FROM t_inventari i JOIN taxa t ON i.codi_esp = t.codi_esp "
                            + "   WHERE i.lat <> 0 and i.lon <> 0";
                    PreparedStatement stmt = con.prepareStatement(sql);
                    ResultSet rs = stmt.executeQuery();
                    int filas = 0;
                    while (rs.next() == true) {
                        System.out.println(MessageFormat.format("i([''{0}'', ''{1}'', {2}, {3}]);", 
                                                                rs.getString("codi_ind"), rs.getString("codi_esp"), String.valueOf(rs.getDouble("lat")), String.valueOf(rs.getDouble("lon"))));
                        filas = filas + 1;
                    }
                    rs.close();
                    stmt.close();
                    System.out.println("Filas: " + filas);
                }            
            });
        } catch (PersistenceException e) {
            e.printStackTrace();
            if (tx.isActive() == true) {
                tx.rollback();
            }
        } finally {
            em.close();
        }

    }
}
