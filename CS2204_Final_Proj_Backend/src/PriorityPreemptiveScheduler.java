import java.math.BigDecimal;
import java.util.*;

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

    private void echoResults()
    {
        int i;
        TableObject newElem;
        LinkedList<TableObject> taList = new LinkedList<>();
        LinkedList<TableObject> wtList = new LinkedList<>();
        ArrayList<GanttChartObj> tmpList;
        ListIterator<GanttChartObj> travTmp;
        GanttChartObj buff, buffPrev;

        double avgWT, avgTA;
        avgWT = avgTA = 0.0;

        st.write("HTTP/1.1 200 OK\r\n");
        st.write("Content-Type: text/plain\r\n");
        st.write("\r\n");

        // response here
        BigDecimal delay =BigDecimal.valueOf(0.80);
        BigDecimal curr = delay.negate();
        if (ganttMaster.getSize() > 0) {
            st.write("<h2 class='wow flipInX' data-wow-delay='"+(curr=curr.add(delay))+"s'>Gantt Chart</h2>");
            st.write("<div class='row'><div class='col-xs-12'><div id='gantt-chart-process'>");

            for (GanttChartObj ganttObj : ganttMaster.getList()) {
                st.write("<span class='process' style='width:"+ ganttObj.getWidth()+"%'><span class='background' style='animation-delay:"+(curr=curr.add(delay)) +"s'><h4 class='text-center' style='animation-delay:"+curr.add(BigDecimal.valueOf(0.3))+"s'><strong>" + ganttObj.getPName() + "</strong></h4></span></span>");
            }

            st.write("</span></div><div id='gantt-chart-nos'>'");

            long[] ganttNums = ganttMaster.getNums();

            for (i = 0; i < ganttNums.length - 2; i++) {
                st.write("<span class='danger'>" + ganttNums[i] + "</span>");
            }
            long lastTime = ganttNums[i+1];
            st.write("<span class='danger'>" + ganttNums[i] + "<div class='pull-right' style='width:'>" + lastTime + "</div></span>");

            st.write("</div></div></div>");

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
            // ------- TA TABLE -----------
            delay = BigDecimal.valueOf(0.45);
            st.write("<div class='row'><div class='col-md-6'><h3  class='wow flipInX' data-wow-delay='"+(curr=curr.add(delay))+"s'>Turnaround Time</h3>");
            st.write("<table class='table table-borderless'>" +
                    "<th  class='col-md-2 wow flipInX' data-wow-delay='"+(curr=curr.add(delay))+"s'>Process</th>" +
                    "<th class='wow flipInX' data-wow-delay='"+curr+"s'>Equation</th>" +
                    "<th class='wow flipInX' data-wow-delay='"+curr+"s'>Result</th>");

            for (TableObject trav : taList) {
                st.write("<tr class='wow flipInX' data-wow-delay='"+(curr=curr.add(delay))+"s'><td>" + trav.getPName() + "</td>  <td>" + trav.getFirstEqStr(" ") + "</td>  <td>" + trav.getFirstResult() + " ms</td> </tr>");
                avgTA += trav.getFirstResult();
            }
            avgTA /= orgPList.size();

            st.write("<tr class='wow flipInX' data-wow-delay='"+(curr=curr.add(delay))+"s'><td colspan='3'><h4>Average Turnaround Time: " + String.format("%.2f ms", avgTA) + "</h3></td></tr>");
            st.write("</table></div>");

            // ------- TA TABLE -----------
            st.write("<div class='col-md-6'><h3 class='wow flipInX' data-wow-delay='"+(curr=curr.add(delay))+"s'>Waiting Time</h3>");
            st.write("<table class='table table-borderless'>" +
                    "<th class='col-md-2 wow flipInX' data-wow-delay='"+(curr=curr.add(delay))+"s'>Process</th>" +
                    "<th class='wow flipInX' data-wow-delay='"+curr+"s'>Equation(s)</th>" +
                    "<th class='wow flipInX' data-wow-delay='"+curr+"s'>Result</th>");

            for (TableObject trav : wtList) {
                st.write("<tr class='wow flipInX' data-wow-delay='"+(curr=curr.add(delay))+"s'><td>" + trav.getPName() + "</td><td>");
                i = 0;
                int len = trav.getEqCount();
                if (len != 1) {
                    for (String eqTrav : trav.getAllEqStr(" ")) {
                        st.write("(" + eqTrav + ")" + (++i < trav.getEqCount() ? " + " : ""));
                    }
                } else {
                    st.write(trav.getFirstEqStr(" "));
                }
                st.write("</td><td>" + trav.getResults() + " ms</td> </tr>");
                avgWT += (double) trav.getResults();
            }
            avgWT /= orgPList.size();

            st.write("<tr class='wow flipInX' data-wow-delay='"+(curr=curr.add(delay))+"s'><td colspan='3'><h4>Average Waiting Time: " + String.format("%.2f ms", avgWT) + "</h4></td></tr>");
            st.write("</table></div></div>");
            st.write("<script>console.log('Priority Preemptive Scheduling done . . .')</script>");
        } else {
            st.write("<script>console.log('Warning: Empty GANTT Chart!')</script>");
        }

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
