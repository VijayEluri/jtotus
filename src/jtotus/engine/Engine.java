/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jtotus.engine;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import jtotus.JtotusView;
import jtotus.common.Helper;
import jtotus.database.DataFetcher;
import jtotus.threads.*;



/**
 *
 * @author kappiev
 */


public class Engine {

    private Dispatcher dispatcher = null;
    private LinkedList <VoterThread>methodList;
    private DataFetcher fetcher = null;
    private Helper help = null;
    private JtotusView mainWindow = null;


    private void prepareMethodsList(){
        // Available methods
        methodList.add(new DummyMethod(dispatcher));

        File scriptDir = new File("./src/jtotus/rulebase/");
        if(!scriptDir.isDirectory()) {
            return;
        }

        FileFilter filter = fileIsGroovyScript();
        File[] listOfFiles = scriptDir.listFiles(filter);

        for ( File tmp : listOfFiles) {
            try {
                methodList.add(new DecisionScript(tmp.getCanonicalPath()));
            } catch (IOException ex) {
                Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }



    public Engine(){
        help = Helper.getInstance();
        dispatcher = new Dispatcher();
        fetcher = new DataFetcher();
        
        methodList = new LinkedList<VoterThread>();

        dispatcher.setFetcher(fetcher);

        prepareMethodsList();
        
        
    }

   public void setGUI(JtotusView tempView) {
        mainWindow = tempView;
        mainWindow.prepareMethodList(methodList);
    }
    

    public void run(){

        if(dispatcher.setList(methodList)){
            help.debug(1, "Dispatcher is already full");
            return;
        }
        
       // dispatcher.run();
    }


    public void train(){

        LinkedList<String>methodNames = mainWindow.getMethodList();
        Iterator methodIter = methodList.iterator();
        boolean found = false;


        while(methodIter.hasNext())
        {
            Iterator nameIter = methodNames.iterator();
            VoterThread methName = (VoterThread)methodIter.next();
            String tempName = methName.getMethName();
            
            while(nameIter.hasNext()){
                String nameList = (String)nameIter.next();
               // System.out.printf("Search name:%s in list:%s\n",tempName, nameList);
                if(nameList.compareTo(tempName)==0){
                    found=true;
                    break;
                }
            }
            if (!found){
               // System.out.printf("Removeing:%s\n",tempName);
                methodIter.remove();
            }
            found=false;
        }

        if(dispatcher.setList(methodList)){
            help.debug(1, "Dispatcher is already full");
            return;
        }
        dispatcher.run();


    }



    private FileFilter fileIsGroovyScript()
    {
       FileFilter fileFilter = new FileFilter() {
           public boolean accept(File file)
           {
                if(!file.isFile() || !file.canRead()) {
                    return false;
                }

                String name = file.getName();
                if (!name.endsWith(".groovy"))
                {
                    return false;
                }
               return true;
           }
       };
       return fileFilter;
    }

 

}
