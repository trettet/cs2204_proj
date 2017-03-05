import sun.awt.image.ImageWatched;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Created by theLa on Feb-14-2017.
 */
public class GanttChartController
{
    private final ArrayList<GanttChartObj> ganttChart;
    private int last;
    public GanttChartController()
    {
        ganttChart = new ArrayList<>();
        last = -1;
    }

    private void addWidthProportion()
    {
        long totalWidth;
        totalWidth = ganttChart.get(getLast()).getEnd();
        System.out.print(totalWidth);
        int i;
        for(i=0; i<getSize(); i++) {
            ganttChart.get(i).setWidth((ganttChart.get(i).getEnd() - ganttChart.get(i).getStart()) /(double)totalWidth * 100);
        }
    }

    public void addProcess(String pName, long started, long ended)
    {
        System.out.println("Added PROCESS: " + pName);
        ganttChart.add(new GanttChartObj(pName, started, ended));
        last++;
    }

    public int getSize()
    {
        return ganttChart.size();
    }

    public int getLast()
    {
        return last;
    }

    public ArrayList<String> getPNameList()
    {
        addWidthProportion();
        return ganttChart.stream().map(GanttChartObj::getPName).collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<GanttChartObj> getList()
    {
        addWidthProportion();
        return ganttChart;
    }

    public double getWidthByIndex(int ndx)
    {
        return ganttChart.get(ndx).getWidth();

    }

    public long[] getNums() {
        long[] ret = new long[ganttChart.size()+1];
        int i;

        i = 0;
        while (i < ganttChart.size()) {
            ret[i] = ganttChart.get(i).getStart();
            i++;
        }

        ret[i] = ganttChart.get(i-1).getEnd();

        return ret;
    }

    public GanttChartObj getLastOccurrence(String name)
    {
        GanttChartObj ret = null;
        for(GanttChartObj trav : ganttChart){
            if(trav.getPName().equalsIgnoreCase(name)){
                ret=trav;
            }
        }
        return ret;
    }

    public ArrayList<GanttChartObj> getGanttListByName(String name)
    {
        return ganttChart.stream().filter(trav2 ->
                trav2.getPName().equals(name)).collect(Collectors.toCollection(ArrayList::new));
    }
}
