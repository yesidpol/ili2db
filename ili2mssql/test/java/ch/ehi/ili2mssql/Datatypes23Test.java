package ch.ehi.ili2mssql;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.After;
import org.junit.Test;

import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.ili2db.AbstractTestSetup;
import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.gui.Config;
import ch.ehi.ili2mssql.test_utils.TestUtils;
import ch.interlis.iox.IoxException;

public class Datatypes23Test {
    private static final String DBSCHEMA = "Datatypes23";
    private static final String TEST_OUT = "test/data/Datatypes23/";
    protected AbstractTestSetup setup;
    
    Connection jdbcConnection=null;
    Statement stmt=null;

    String dburl=System.getProperty("dburl"); 
    String dbuser=System.getProperty("dbusr");
    String dbpwd=System.getProperty("dbpwd"); 

    public Datatypes23Test() {
        setup = new MsSqlTestSetup(dburl, dbuser, dbpwd, DBSCHEMA);
    }
    
    public Config initConfig(String xtfFilename,String dbschema,String logfile) {
        Config config=new Config();
        new ch.ehi.ili2mssql.MsSqlMain().initConfig(config);
        config.setDburl(dburl);
        config.setDbusr(dbuser);
        config.setDbpwd(dbpwd);
        if(dbschema!=null){
            config.setDbschema(dbschema);
        }
        if(logfile!=null){
            config.setLogfile(logfile);
        }
        config.setXtffile(xtfFilename);
        if(xtfFilename!=null && Ili2db.isItfFilename(xtfFilename)){
            config.setItfTransferfile(true);
        }
        return config;
        
    }

    @After
    public void endDb() throws Exception
    {
        if(stmt!=null) {
            stmt.close();
            stmt=null;
        }
        if(jdbcConnection!=null){
            jdbcConnection.close();
        }
    }
    

    @Test
    public void importXtfLine() throws Exception{
        try {
            setup.createConnection();
            setup.resetDb();
            File data=new File(TEST_OUT+"Datatypes23Line.xtf");
            Config config=setup.initConfig(data.getPath(),data.getPath()+".log");
            config.setFunction(Config.FC_IMPORT);
            config.setDoImplicitSchemaImport(true);
            config.setCreateFk(config.CREATE_FK_YES);
            config.setCreateNumChecks(true);
            config.setTidHandling(Config.TID_HANDLING_PROPERTY);
            config.setBasketHandling(config.BASKET_HANDLING_READWRITE);
            config.setCatalogueRefTrafo(null);
            config.setMultiSurfaceTrafo(null);
            config.setMultilingualTrafo(null);
            config.setInheritanceTrafo(null);
            EhiLogger.getInstance().setTraceFilter(false);
            //Ili2db.readSettingsFromDb(config);
            Ili2db.run(config,null);
            // imported polyline
            {
                ResultSet rs = stmt.executeQuery("SELECT st_asewkt(straightsarcs2d) FROM datatypes23.line2 WHERE t_ili_tid = 'Line2.0';");
                ResultSetMetaData rsmd=rs.getMetaData();
                assertEquals(1, rsmd.getColumnCount());
                while(rs.next()){
                    assertEquals(null, rs.getObject(1));
                }
            }
            {
                ResultSet rs = stmt.executeQuery("SELECT st_asewkt(straightsarcs2d) FROM datatypes23.line2 WHERE t_ili_tid = 'Line2.1';");
                ResultSetMetaData rsmd=rs.getMetaData();
                assertEquals(1, rsmd.getColumnCount());
                while(rs.next()){
                    assertEquals("SRID=2056;COMPOUNDCURVE(CIRCULARSTRING(2460001 1045001,2460005 1045004,2460006 1045006),(2460006 1045006,2460010 1045010))", rs.getObject(1));
                }
            }
            {
                ResultSet rs = stmt.executeQuery("SELECT st_asewkt(straightsarcs3d) FROM datatypes23.line3 WHERE t_ili_tid = 'Line3.1';");
                ResultSetMetaData rsmd=rs.getMetaData();
                assertEquals(1, rsmd.getColumnCount());
                while(rs.next()){
                    assertEquals("SRID=2056;COMPOUNDCURVE(CIRCULARSTRING(2460001 1045001 300,2460005 1045004 0,2460006 1045006 300),(2460006 1045006 300,2460010 1045010 300))", rs.getObject(1));
                }
            }
            {
                ResultSet rs = stmt.executeQuery("SELECT st_asewkt(straights2d) FROM datatypes23.simpleline2 WHERE t_ili_tid = 'SimpleLine2.0';");
                ResultSetMetaData rsmd=rs.getMetaData();
                assertEquals(1, rsmd.getColumnCount());
                while(rs.next()){
                    assertEquals(null, rs.getObject(1));
                }
            }
            {
                ResultSet rs = stmt.executeQuery("SELECT st_asewkt(straights2d) FROM datatypes23.simpleline2 WHERE t_ili_tid = 'SimpleLine2.1';");
                ResultSetMetaData rsmd=rs.getMetaData();
                assertEquals(1, rsmd.getColumnCount());
                while(rs.next()){
                    assertEquals("SRID=2056;COMPOUNDCURVE((2460001 1045001,2460010 1045010))", rs.getObject(1));
                }
            }
            {
                ResultSet rs = stmt.executeQuery("SELECT st_asewkt(straights3d) FROM datatypes23.simpleline3 WHERE t_ili_tid = 'SimpleLine3.1';");
                ResultSetMetaData rsmd=rs.getMetaData();
                assertEquals(1, rsmd.getColumnCount());
                while(rs.next()){
                    assertEquals("SRID=2056;COMPOUNDCURVE((2460001 1045001 300,2460010 1045010 300))", rs.getObject(1));
                }
            }
        }catch(SQLException e) {
            throw new IoxException(e);
        }finally{
            if(jdbcConnection!=null){
                jdbcConnection.close();
            }
        }
    }
}
