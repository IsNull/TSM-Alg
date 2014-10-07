package mse.alg.ex2;


import com.vividsolutions.jts.geom.Coordinate;

import java.util.LinkedList;

/**
 * Created by Simon on 07.10.2014.
 */
public class SortHorizon {
    private LinkedList<MonotoneChain> chains = new LinkedList<>();	// x-monotone linked list of chains
    private boolean finished;
    private boolean isStart;
    private boolean isStop;
    private boolean isInner;
    private int inIndex;
    private int outIndex;
    private int stopIndex;

    public SortHorizon(LinkedList<MonotoneChain> m_chains) {
        chains = m_chains;
        isStart = true;
        isInner = false;
        isStop  = false;
        inIndex = 0;
        outIndex = 0;
        finished = false;
        stopIndex = 0;

        if(chains.size() == 0){
            finished = true;
        }

    }

    public MonotoneChain getNext(){
        for(int i=0;i<chains.get(outIndex).size();i++){
        }
        //Handle s Start Event
        if(isStart){
            //Chain with 2 Element
            if(chains.get(outIndex).get(1).compareTo(chains.get(outIndex).getLast()) == 0){
                isStop = true;
                inIndex = 1;
                stopIndex = 1;
            }else{
                isInner = true;
                inIndex = 1;
            }

            isStart = false;
            return chains.get(outIndex);
        }

        //Handle Inner Event
        if(isInner){
            //If next Event is a Stop
            if(chains.get(outIndex).get(inIndex+1).compareTo(chains.get(outIndex).getLast()) == 0){
                isInner = false;
                isStop = true;
            }
            inIndex++;
            return chains.get(outIndex);
        }

        //Handle Stop Event (No if needed)
        outIndex++;
        //Check if last Element in List
        if(chains.size() == outIndex){
            finished = true;
            isStop = false;
            stopIndex = inIndex;
        }else{
            isStart=true;
            stopIndex = inIndex;
            inIndex = 0;
        }

        isStop = false;
        return chains.get(outIndex-1);


    }

    public int getIndex(){
        return inIndex;
    }
    public String getStatus(){
        return "isStart:"+isStart+" -isInner:"+isInner+" -isStop:"+isStop+" -isFinished:"+finished;
    }

    public int getStopIndex(){
        return stopIndex;
    }

    public double getX(){
        return chains.get(outIndex).get(inIndex).x;
    }

    public double getY(){
        return chains.get(outIndex).get(inIndex).y;
    }

    public boolean isFinished(){
        return finished;
    }

    public boolean isNextStart(){
        return isStart;
    }

    public boolean isNextStop(){
        return isStop;
    }

    public boolean isNextInner(){
        return isInner;
    }

}