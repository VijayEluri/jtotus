/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jtotus.database;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.math.BigDecimal;
import jtotus.common.Helper;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;


/**
 *
 * @author kappiev
 */
public class FileSystemFromHex implements InterfaceDataBase {

    String pathToDataBaseDir = "OMXNordic/";
    String filePattern = "yyyy-MM-dd";
    Helper help = null;


    public FileSystemFromHex(){
        help = Helper.getInstance();
    }


    private FileFilter filterForDir()
    {
       FileFilter fileFilter = new FileFilter() {
           public boolean accept(File file)
           {
                if(!file.isFile() || !file.canRead()) {
                    return false;
                }

                String name = file.getName();
                if (!name.endsWith(".xls"))
                {
                    return false;
                }
               return true;
           }
       };
       return fileFilter;
    }

    
    
public BigDecimal fetchHighestPrice(String stockName, SimpleDateFormat time){
    return fetchValue(stockName, time, 1);
}

public BigDecimal fetchLowestPrice(String stockName, SimpleDateFormat time){
    return fetchValue(stockName, time, 2);
}

public BigDecimal fetchClosingPrice(String stockName, SimpleDateFormat time){
    return fetchValue(stockName, time, 3);
}

public BigDecimal fetchAveragePrice(String stockName, SimpleDateFormat time){
    return fetchValue(stockName, time, 4);
}

public BigDecimal fetchTotalVolume(String stockName, SimpleDateFormat time){
    return fetchValue(stockName, time, 5);
}

public BigDecimal fetchTurnOver(String stockName, SimpleDateFormat time){
    return fetchValue(stockName, time, 6);
}

public BigDecimal fetchTrades(String stockName, SimpleDateFormat time){
    return fetchValue(stockName, time, 7);
}


public BigDecimal fetchValue(String stockName, SimpleDateFormat time, int row)
{
    BigDecimal result = null;

    File dir = new File("./" + pathToDataBaseDir);
    FileFilter filter = filterForDir();

    File[] listOfFiles = dir.listFiles(filter);


    for (int i = 0; i < listOfFiles.length ; i++) {
        String nameOfFile = listOfFiles[i].getName();

        if (nameOfFile.indexOf(stockName) != -1) {
            help.debug("FileSystemFromHex","Found File:%s\n", nameOfFile);
            result = omxNordicFile(nameOfFile, time, row);
            if (result != null) {
               return result;
            }
        }
    }
    help.debug("FileSystemFromHex", "Not found value for:%s\n", stockName);
    return result;
}





    public BigDecimal omxNordicFile(String fileName, SimpleDateFormat time,int row) {
        BigDecimal result = null;

        try {

            POIFSFileSystem fs = new POIFSFileSystem(
                new FileInputStream(pathToDataBaseDir+fileName));

            HSSFWorkbook workbook = new HSSFWorkbook(fs);

            HSSFSheet worksheet = workbook.getSheetAt(0);
            //HSSFRow row1 = worksheet.getRow(0);

            // Year-Mount-Data
            time.applyPattern(filePattern);

            //System.out.printf("Class :%s : %s\n",this.getClass().toString(), this.toString());
            String correctTime = help.dateToString(time);
            Iterator rowIter = worksheet.rowIterator();

            while(rowIter.hasNext())
            {
                HSSFRow rows = (HSSFRow)rowIter.next();
                HSSFCell cell = rows.getCell(0);
                String dateString = null;
                if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                    dateString = cell.getStringCellValue();
                } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC){
                    Date date = cell.getDateCellValue();
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    dateString = format.format(date);
                    
//                    System.err.printf("File (%s) is corrucped ? type:%s\n", fileName, dateString);
                    
                } else {
                    System.err.printf("File (%s) is corrucped ? type:%d formula:%d\n", fileName, cell.getCellType(), Cell.CELL_TYPE_FORMULA);
                    return null;
                }
              

              //  help.debug("FileSystemFromHex","Searching:%s from:%s\n", correctTime, temp);
                if (correctTime.compareTo(dateString) == 0)
                {
                    HSSFCell closingPrice = rows.getCell(row);
                    if (closingPrice == null)
                        return null;
                    
                    float floatTemp = (float)closingPrice.getNumericCellValue();
                    help.debug("FileSystemFromHex",
                            "Closing price at:%d f:%.4f Time:%s\n",
                            cell.getRowIndex(), floatTemp, correctTime);
                    
                    return new BigDecimal(floatTemp);
                }
            }

             
        } catch (IOException ex) {
            Logger.getLogger(FileSystemFromHex.class.getName()).log(Level.SEVERE, null, ex);
        }

        return result;
    }

}
