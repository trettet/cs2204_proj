import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import com.google.gson.Gson;

/**
 * Created by theLa on Feb-27-2017.
 */
public class PriorityPreemptiveScheduler
{
    private final LinkedList<ProcessObject> pList, orgPList;
    private final GanttChartController ganttMaster;
    private final StreamController st;


    private long started;

    public PriorityPreemptiveScheduler(LinkedList<ProcessObject> pList, StreamController st)
    {
        pList.sort(new byTimeComparator());
        this.orgPList = new LinkedList<>();
        this.pList = pList;
        this.orgPList.addAll(pList); // clone ProcessList for LABELS!
        this.ganttMaster = new GanttChartController();
        this.st = st;
    }

    public void schedule()
    {
        ProcessObject tmp;
        ProcessObject currProc = null;
        PriorityQueue<ProcessObject> readyQueue = new PriorityQueue<>(new byPriorityComparator());
        long time;

        time = -1;

        do {
            time++;

            fetchAllReadyProcesses(readyQueue, time);

            if (currProc == null) {
                if (readyQueue.size() > 0) {
                    started = time;
                    currProc = readyQueue.remove();
                } else {
                    started = time;
                    currProc = new ProcessObject(null); // adds an "EMPTY" space in the gantt chart
                }
            } else {
                if (currProc.getRemBurst() > 0) {
                    currProc.process();
                    if (currProc.getRemBurst() == 0) {
                        // process done!
                        ganttMaster.addProcess(currProc.getPName(), started, time);
                        started = time;
                        if (readyQueue.size() > 0) {
                            currProc = readyQueue.remove();
                        } else {
                            currProc = new ProcessObject(null);
                        }
                    } else if (readyQueue.size() > 0 && currProc.getPriority() > readyQueue.peek().getPriority()) {
                        // preempt process
                        tmp = readyQueue.remove();
                        readyQueue.add(currProc);
                        ganttMaster.addProcess(currProc.getPName(), started, time);

                        started = time;
                        currProc = tmp;
                    }
                } else if (readyQueue.size() > 0) {
                    ganttMaster.addProcess(currProc.getPName(), started, time);
                    started = time;
                    currProc = readyQueue.remove();
                }
            }

        } while (currProc.getRemBurst() != -1 || pList.size() != 0 || readyQueue.size() != 0);

        echoResults();

        //cancel();
    }

    private int fetchAllReadyProcesses(PriorityQueue<ProcessObject> readyQueue, long time)//pList should be here but meh
    {
        int i;
        i = 0;
        while (pList.size() != 0 && pList.peek().getArrival() == time) {
            i++;
            readyQueue.add(pList.remove());
        }

        return i;
    }

    private class ResultObject implements Serializable
    {
        private String ganttChart;
        private String taTable;
        private String wtTable;
        private String extraHTML;

        public ResultObject()
        {
            ganttChart = taTable = wtTable = extraHTML = "";
        }
        public void setGanttChart(String in) { ganttChart = in; }
        public String getGanttChart() {return ganttChart;}
        public void setTaTable(String in) { taTable = in; }
        public String getTaTable() { return  taTable; }
        public void setWtTable(String in) {wtTable = in;}
        public String getWtTable() { return  wtTable; }
        public void setExtraHTML(String in) { extraHTML = in;}
        public String getExtraHTML() {return extraHTML;}
    }

    private void echoResults()
    {
        int i;
        TableObject newElem;
        LinkedList<TableObject> taList = new LinkedList<>();
        LinkedList<TableObject> wtList = new LinkedList<>();
        ArrayList<GanttChartObj> tmpList;
        ListIterator<GanttChartObj> travTmp;
        GanttChartObj buff, buffPrev;
        Gson GSBuilder = new Gson();
        StringBuilder sbBuff = new StringBuilder(300);
        ResultObject resObj = new ResultObject();

        double avgWT, avgTA;
        avgWT = avgTA = 0.0;

        st.write("HTTP/1.1 200 OK\r\n");
        st.write("Content-Type: text/plain\r\n");
        st.write("\r\n");

        // response here

        double delay = 0.8;
        double curr = (0.8 * -1)+1.2;
        if (ganttMaster.getSize() > 0) {
            sbBuff.append("<h2>Gantt Chart</h2>");
            sbBuff.append("<div class='row'><div class='col-xs-12'><div id='gantt-chart-process'>");

            for (GanttChartObj ganttObj : ganttMaster.getList()) {
                sbBuff.append("<span class='process' style='width:"+ ganttObj.getWidth()+"%'><span class='background' style='animation-delay:"+(curr=curr + delay) +"s'><h4 class='text-center' style='animation-delay:"+(curr+0.3)+"s'><strong>" + ganttObj.getPName() + "</strong></h4></span></span>");
            }

            sbBuff.append("</span></div><div id='gantt-chart-nos' class='animated fadeInUp' style='animation-delay: "+(curr+((delay-0.6)*ganttMaster.getLast()))+"s'>");

            long[] ganttNums = ganttMaster.getNums();
            int ganttNumsLength = ganttNums.length - 2;
            for (i = 0; i < ganttNumsLength; i++) {
                sbBuff.append("<span class='gantt-no' style='width:"+ganttMaster.getWidthByIndex(i)+"%'>" + ganttNums[i] + "</span>");
            }

            sbBuff.append("<span class='gantt-no' style='width:"+ganttMaster.getWidthByIndex(i)+"%'>" + ganttNums[i] + "<div style='float:right'>"+ganttNums[i+1]+"</div></span>");

            sbBuff.append("</div></div></div>");

            resObj.setGanttChart(sbBuff.toString());
            sbBuff.setLength(0);

            // POPULATE TA TABLE and WT TABLE
            for (ProcessObject trav : orgPList) {
                newElem = new TableObject(trav.getPName());

                tmpList = ganttMaster.getGanttListByName(trav.getPName());
                travTmp = tmpList.listIterator();

                newElem.addEquation(travTmp.next().getStart(), trav.getArrival());

                while (travTmp.hasNext()) {
                    buff = travTmp.next();
                    travTmp.previous();
                    buffPrev = travTmp.previous();
                    travTmp.next();
                    travTmp.next();

                    newElem.addEquation(buff.getStart(), buffPrev.getEnd());
                }

                wtList.add(newElem);
                taList.add(new TableObject(trav.getPName(), ganttMaster.getLastOccurrence(trav.getPName()).getEnd(), trav.getArrival()));
            }
            /* -------------------------- */
            // ------- TA TABLE -----------
            sbBuff.append("<div class='row'><div class='col-xs-12'><h3>Turnaround Time</h3>");
            sbBuff.append("<table class='table table-borderless'><thead>" +
                    "<th class='col-md-2'>Process</th>" +
                    "<th>Equation</th>" +
                    "<th>Result</th></thead>");

            for (TableObject trav : taList) {
                sbBuff.append("<tr><td>" + trav.getPName() + "</td>  <td>" + trav.getFirstEqStr(" ") + "</td>  <td>" + trav.getFirstResult() + " ms</td> </tr>");
                avgTA += trav.getFirstResult();
            }
            avgTA /= orgPList.size();

            sbBuff.append("<tr><td colspan='3'><h4>Average Turnaround Time: " + String.format("%.2f ms", avgTA) + "</h3></td></tr>");
            sbBuff.append("</table></div>");

            resObj.setTaTable(sbBuff.toString());
            sbBuff.setLength(0);
            // ------- END OF TA TABLE ----------- \\

            sbBuff.append("<div class='col-xs-12'><h3>Waiting Time</h3>");
            sbBuff.append("<table class='table table-borderless'><thead>" +
                    "<th class='col-md-2'>Process</th>" +
                    "<th>Equation(s)</th>" +
                    "<th>Result</th></thead>");

            for (TableObject trav : wtList) {
                sbBuff.append("<tr><td>" + trav.getPName() + "</td><td>");
                i = 0;
                int len = trav.getEqCount();
                if (len != 1) {
                    for (String eqTrav : trav.getAllEqStr(" ")) {
                        sbBuff.append("(" + eqTrav + ")" + (++i < trav.getEqCount() ? " + " : ""));
                    }
                } else {
                    sbBuff.append(trav.getFirstEqStr(" "));
                }
                sbBuff.append("</td><td>" + trav.getResults() + " ms</td> </tr>");
                avgWT += (double) trav.getResults();
            }
            avgWT /= orgPList.size();

            sbBuff.append("<tr><td colspan='3'><h4>Average Waiting Time: " + String.format("%.2f ms", avgWT) + "</h4></td></tr>");
            sbBuff.append("</table></div></div>");

            resObj.setWtTable(sbBuff.toString());
            resObj.setExtraHTML("<script>console.log('Priority Preemptive Scheduling done . . .')</script>");
        } else {
            resObj.setExtraHTML("<script>console.log('Warning: Empty GANTT Chart!')</script>");
        }

        GSBuilder.toJson(resObj, st.getWriter());
        System.out.println("Sent JSON . . .");
        st.close();
    }

    public class byTimeComparator implements Comparator<ProcessObject>
    {
        @Override
        public int compare(ProcessObject a, ProcessObject b)
        {
            return (a.getArrival()<b.getArrival()) ? -1 : 1;
        }
    }

    public class byPriorityComparator implements Comparator<ProcessObject>
    {
        @Override
        public int compare(ProcessObject a, ProcessObject b)
        {
            return (a.getPriority()<b.getPriority()) ? -1 : 1;
        }
    }

}
