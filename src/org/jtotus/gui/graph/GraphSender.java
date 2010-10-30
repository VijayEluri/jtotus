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
 *
 *
 * http://tutorials.jenkov.com/java-collections/navigableset.html
 */
package org.jtotus.gui.graph;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.LocalDate;
import org.jtotus.common.StockUnit;
import org.jtotus.engine.Engine;

/**
 *
 * @author Evgeni Kappinen
 */
public final class GraphSender {
    private String mainReviewTarget = null;
    private Engine mainEngine = null;
    private LinkedBlockingDeque<GraphPacket> mainPort = null;
    private ArrayList<StockUnit> listOfValues = null;



    private GraphSeriesType type = GraphSeriesType.SIMPLELINE;
    private String seriesName = null;
    private String plotName = null;
    
    
    public GraphSender(String reviewTarget) {
        mainReviewTarget = reviewTarget;
        mainEngine = Engine.getInstance();

        listOfValues = new ArrayList<StockUnit>();
        mainPort = mainEngine.fetchGraph(mainReviewTarget);
    }

    
    public void addForSending(Date date, Double value) {
        StockUnit unit = new StockUnit();
        unit.date = LocalDate.fromDateFields(date);
        unit.value = value;
        listOfValues.add(unit);
    }

    public void addForSending(Date date, Double value, String annotation) {
        StockUnit unit = new StockUnit();
        unit.date = LocalDate.fromDateFields(date);
        unit.value = value;
        unit.annotation = annotation;
        listOfValues.add(unit);
    }

    public void sendAllStored() {
        GraphPacket packet = new GraphPacket();
        packet.seriesTitle  = this.getSeriesName();
        packet.plotName = this.getPlotName();
        packet.type = this.getType();
        packet.results  = listOfValues;

        this.sentPacket(mainReviewTarget, packet);
        
    }
    

    public boolean sentPacket(String reviewTarget, GraphPacket packetObj) {
        LinkedBlockingDeque<GraphPacket> queue = null;
        
        if (mainReviewTarget.compareTo(reviewTarget) != 0){
            queue = mainEngine.fetchGraph(reviewTarget);
            if (queue == null) {
                System.err.printf("Unable to fetch Graph port!\n");
                return false;
            }
        }else {
            queue = mainPort;
        }
        
        try {
            
            queue.putFirst(packetObj);
        } catch (InterruptedException ex) {
            Logger.getLogger(GraphSender.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }

    /**
     * @return the mainSeriesName
     */
    public String getSeriesName() {
        return seriesName;
    }

    /**
     * @param mainSeriesName the mainSeriesName to set
     */
    public void setSeriesName(String mainSeriesName) {
        this.seriesName = mainSeriesName;
    }

    /**
     * @return the plotName
     */
    public String getPlotName() {
        return plotName;
    }

    /**
     * @param plotName the plotName to set
     */
    public void setPlotName(String plotName) {
        this.plotName = plotName;
    }

    /**
     * @return the type
     */
    public GraphSeriesType getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(GraphSeriesType type) {
        this.type = type;
    }

}
