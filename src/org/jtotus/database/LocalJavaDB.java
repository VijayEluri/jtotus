/*
    This file is part of jTotus.

    jTotus is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    jTotus is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with jTotus.  If not, see <http://www.gnu.org/licenses/>.
*/


package org.jtotus.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jtotus.common.Helper;


/**
 *
 * @author Evgeni Kappinen
 */
public class LocalJavaDB implements InterfaceDataBase {

    private static Connection conJavaDB = null;
    private String driver = "org.apache.derby.jdbc.ClientDriver";
    private String []connectionUrl = { "jdbc:derby://localhost:1527/OMXHelsinki",
                                       "hex", "hex"};
    public String mainTable = "APP.OMXHELSINKI";
    private Helper help = Helper.getInstance();


    public LocalJavaDB() {
        
    }

    public int initialize() {


        try {

             Class.forName(driver);
             conJavaDB = DriverManager.getConnection(connectionUrl[0],
                                                     connectionUrl[1],
                                                     connectionUrl[2]);


        } catch (SQLException ex) {
           // Logger.getLogger(LocalJavaDB.class.getName()).log(Level.SEVERE, null, ex);
           System.err.printf("Unable to connecto to JavaDB !\n");
            return -2;//Unable to connect to database
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(LocalJavaDB.class.getName()).log(Level.SEVERE, null, ex);
            return -1; // Cannot load driver
        }

        return 0;
    }




    public BigDecimal fetchClosingPrice(String stockName, Calendar calendar) {
        return this.fetchData(stockName, calendar, "CLOSINGPRICE");
    }

    public BigDecimal fetchVolume(String stockName, Calendar calendar) {
        return this.fetchData(stockName, calendar, "VOLUME");
    }

    private BigDecimal fetchData(String stockName, Calendar calendar, String data) {
        BigDecimal closingPrice = null;
        
        try {
            if (conJavaDB == null) {
                if (initialize() < 0) {
                    return closingPrice;
                }
            }

            String query = "SELECT * FROM "+mainTable+" WHERE STOCKNAME=? AND DATE=?";
            PreparedStatement pstmt = conJavaDB.prepareStatement(query);
            pstmt.setString(1, stockName);

            java.util.Date searchDay = calendar.getTime();
            java.sql.Date sqlDate = new java.sql.Date(searchDay.getTime());
            pstmt.setDate(2, sqlDate, calendar);

            //Perform query
            ResultSet results = pstmt.executeQuery();

            while (results.next()) {
                BigDecimal tmpBigDecimal = results.getBigDecimal(data);
                //System.out.printf("Javadb got closing price %f for %s\n", tmpBigDecimal, stockName);
                closingPrice = tmpBigDecimal;
                break;
            }

            pstmt.clearParameters();
            pstmt.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(LocalJavaDB.class.getName()).log(Level.SEVERE, null, ex);
        }

        return closingPrice;

    }

    public BigDecimal fetchAveragePrice(String stockName, Calendar time) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean stockNameExists(String stockName, Calendar date){

      boolean result = false;

        try {
            if (conJavaDB == null) {
                if (initialize() < 0) {
                    return false;
                }
            }


            String query = "SELECT * FROM "+mainTable+" WHERE STOCKNAME=? AND DATE=?";
            PreparedStatement pstmt = conJavaDB.prepareStatement(query);
            pstmt.setString(1, stockName);

            java.util.Date searchDay = date.getTime();

            java.sql.Date sqlDate = new java.sql.Date(searchDay.getTime());
            pstmt.setDate(2, sqlDate, date);

            ResultSet results = pstmt.executeQuery();
            if (results.next()) {
                result=true;
            }
            pstmt.clearParameters();
            pstmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(LocalJavaDB.class.getName()).log(Level.SEVERE, null, ex);
        }

      return result;
    }

    public void storeClosingPrice(String stockName,
                                  Calendar date,
                                  BigDecimal value) {

        this.storeData(stockName, date, value, "CLOSINGPRICE");
        
    }

    public void storeVolume(String stockName,
                            Calendar date,
                            BigDecimal value) {

      this.storeData(stockName, date, value, "VOLUME");

    }

    //FIXME:add if failed store values to file in other database
    public void storeData(String stockName,
                          Calendar calendar,
                          BigDecimal value,
                          String param) {
        
        if (conJavaDB == null) {
            if (initialize() < 0) {
                return;
            }
        }

        try {

            if (this.stockNameExists(stockName, calendar)){
                String query = "UPDATE "+mainTable+" SET "+param+"=? WHERE STOCKNAME=? AND DATE=?";
                PreparedStatement pstmt = conJavaDB.prepareStatement(query);

                pstmt.setBigDecimal(1, value);
                pstmt.setString(2, stockName);
                
                java.util.Date searchDay = calendar.getTime();
                java.sql.Date sqlDate = new java.sql.Date(searchDay.getTime());
                pstmt.setDate(3, sqlDate, calendar);

                System.out.printf("UPDATING; Stock:%s data: res:%d\n",stockName,value.intValue());
                pstmt.executeUpdate();

            }else {
                String query = "INSERT INTO "+mainTable+" (STOCKNAME,DATE,"+param+") VALUES (?,?,?)";

                PreparedStatement pstmt = conJavaDB.prepareStatement(query);

                pstmt.setString(1, stockName);

                java.util.Date searchDay = calendar.getTime();
                java.sql.Date sqlDate = new java.sql.Date(searchDay.getTime());
                pstmt.setDate(2, sqlDate, calendar);


                pstmt.setBigDecimal(3, value);

                int result = pstmt.executeUpdate();

                System.out.printf("INSERTING; Stock:%s data:"+value+" res:%d\n", stockName, result);

                pstmt.clearParameters();
                pstmt.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(LocalJavaDB.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return ;

    }





    /*
     * CREATE table APP.OMXHELSIKI (
                            ID          INTEGER NOT NULL
                                        PRIMARY KEY GENERATED ALWAYS AS IDENTITY
                                        (START WITH 1, INCREMENT BY 1),
                            STOCKNAME    VARCHAR(40) NOT NULL,
                            DATE         DATE,
                            CLOSINGPRICE REAL,
                            TRADES       BIGINT,
                            AVRPRICE     REAL,
                            VOLUME       BIGINT)
     */
    
    public boolean createTable(){
        boolean bCreatedTables = false;
        Statement statement = null;
        String query = "CREATE table APP.OMXHELSIKI ("
                + "ID          INTEGER NOT NULL "
                + "PRIMARY KEY GENERATED ALWAYS AS IDENTITY "
                + "(START WITH 1, INCREMENT BY 1),"
                + "STOCKNAME    VARCHAR(40) NOT NULL,"
                + "DATE         DATE,"
                + "CLOSINGPRICE REAL,"
                + "TRADES       BIGINT,"
                + "AVRPRICE     REAL,"
                + "VOLUME       BIGINT)";


        try {
            statement = conJavaDB.createStatement();
            statement.execute(query);

            bCreatedTables = true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return bCreatedTables;
    }


}