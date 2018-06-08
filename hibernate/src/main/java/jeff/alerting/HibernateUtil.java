package jeff.alerting;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.envers.configuration.EnversSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class that initializes Hibernate configuration and created a
 * session factory.
 *
 * Some properties configurable with a HibernateProperties instance, itself
 * retrieved through the HibernatePropertyFactory instance.
 */
public abstract class HibernateUtil{

    private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);

    protected Configuration cfg;
    private SessionFactory sessionFactory = null;
    private static final String HIBERNATE_DIALECT = "hibernate.dialect";
    private final HibernateProperties props;

    public HibernateUtil(){
        cfg = new Configuration();
        props = HibernatePropertyFactory.getPropertiesFor(this.getClass());

        /**
         * Connection Information..
         */
        cfg.setProperty("hibernate.show_sql", props.getShowSql());
        cfg.setProperty("hibernate.format_sql", "true");
        cfg.setProperty("hibernate.current_session_context_class", "thread");

        if("AscHibernateTest".equalsIgnoreCase(props.getDialect()) || "test".equalsIgnoreCase(props.getDialect())){
            configureAsTest();
        }else{
            configurePostgres();

            cfg.setProperty("connection.provider_class",
                    "org.hibernate.service.jdbc.connections.internal.C3P0Connecti‌​onProvider");
            cfg.setProperty("hibernate.c3p0.acquire_increment", "1");
            cfg.setProperty("hibernate.c3p0.idle_test_period", "60");
            cfg.setProperty("hibernate.c3p0.min_size", "1");
            cfg.setProperty("hibernate.c3p0.max_size", props.getMaxConnections());
            cfg.setProperty("hibernate.c3p0.max_statements", "50");
            cfg.setProperty("hibernate.c3p0.timeout", "0");
            cfg.setProperty("hibernate.c3p0.acquireRetryAttempts", "2");
            cfg.setProperty("hibernate.c3p0.acquireRetryDelay", "250");

            // Control creation and update of table using hibernate utility.
            // Online
            // recommendation is not to use this in production, so only use for
            // development testing and initial deployment.
            if(props.getTableUpdate()){
                cfg.setProperty("hibernate.hbm2ddl.auto", "update");
            }
        }

        addAnnotatedClasses();
    }

    private void configureAsTest(){
        cfg.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
        cfg.setProperty("hibernate.connection.url", "jdbc:h2:mem:test-core");
        cfg.setProperty(HIBERNATE_DIALECT, "org.hibernate.spatial.dialect.h2geodb.GeoDBDialect");
        cfg.setProperty("hibernate.enable_lazy_load_no_trans", "true");
        // For testing, always want hibernate to update the schema and drop it
        // when the session is closed for clean unit testing.
        cfg.setProperty("hibernate.hbm2ddl.auto", "create-drop");
    }

    private void configurePostgres(){
        cfg.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        cfg.setProperty("hibernate.connection.url", "jdbc:postgresql://" + props.getHost() + "/" + props.getDataBase());
        cfg.setProperty("hibernate.connection.username", props.getUser());
        cfg.setProperty("hibernate.connection.password", props.getPass());

        if("postgis".equalsIgnoreCase(props.getDialect())){
            cfg.setProperty(HIBERNATE_DIALECT, "org.hibernate.spatial.dialect.postgis.PostgisDialect");
        }else{
            cfg.setProperty(HIBERNATE_DIALECT, "org.hibernate.dialect.PostgreSQL92Dialect");
        }
    }

    public final Configuration getConfiguration(){
        return cfg;
    }

    public final SessionFactory getSessionFactory(){
        // Building it on demand as a particular use case doesn't want the
        // session
        if(null == sessionFactory || sessionFactory.isClosed()){
            sessionFactory = cfg.buildSessionFactory();
        }
        return sessionFactory;
    }

    public void close(){
        if(null != sessionFactory){
            if(!sessionFactory.isClosed()){
                sessionFactory.close();
            }
            sessionFactory = null;
        }
    }

    protected abstract void addAnnotatedClasses();
