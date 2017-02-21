/**
 * Created by theLa on Feb-08-2017.
 */
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.stream.Collectors;

public class SockThread extends Thread {
    private final Socket sock;
    public SockThread(Socket sock)
    {
        this.sock = sock;
        try {
            sock.setSoTimeout(10000);
        } catch (SocketException ex) {
            System.out.println("Unable to set socket timeout: " + ex.toString());
        }
    }

    private final static String conHeader = "Content-Length: ";

    @Override
    public void run()
    {
        // Do code here
        StringBuilder raw = new StringBuilder(100);
        StringBuilder post = new StringBuilder();

        System.out.println("Accepted client: " + sock.getRemoteSocketAddress());
        IOStreams st = new IOStreams(sock);
        String buff; // reqPage;
        int conLen = 0;

        while (!(buff = st.readLine()).equals("")) {
            raw.append(buff);
            if (buff.startsWith(conHeader)) {
                conLen = Integer.parseInt(buff.substring(conHeader.length()));
            }
        }

        for(int i=0; i<conLen; i++) {
            post.append((char)st.read());
        }

        System.out.println(post.toString());

        LinkedList<ProcessObject> pList = getPObjects(post.toString());
        pList.sort(new byTimeComparator());

        ////
// -> do the code here
        Timer t1 = new Timer();

        t1.schedule(new SchedulerTask(pList, st), 0, 1);




        ///



        // get requested page
        // reqPage = sb.substring(sb.indexOf("GET ") + 4, sb.indexOf("HTTP/")-1);
    }

    public class SchedulerTask extends TimerTask
    {
        private long time;
        private final LinkedList<ProcessObject> pList, orgPList;
        private final PriorityQueue<ProcessObject> readyQueue;
        private final GanttChartController ganttMaster;
        private final IOStreams st;

        private ProcessObject currProc;

        private long started;

        public SchedulerTask(LinkedList<ProcessObject> pList, IOStreams st)
        {
            this.orgPList = new LinkedList<>();
            this.pList = pList;
            this.orgPList.addAll(pList);
            this.time = -1;
            this.readyQueue = new PriorityQueue<>(new byPriorityComparator());
            this.ganttMaster = new GanttChartController();
            this.st = st;
        }

        @Override
        public void run()
        {
            ProcessObject tmp;
            time++;

            while (pList.size() != 0 && pList.peek().getArrival() == time) {
                readyQueue.add(pList.remove());
            }

            if (currProc == null) {
                if (readyQueue.size() > 0) {
                    started = time;
                    currProc = readyQueue.remove();
                } else {
                    started = time;
                    currProc = new ProcessObject(null);
                }
            } else {
                if (currProc.getRemBurst() >= 0) {
                    currProc.process();
                    if (currProc.getRemBurst() == 0) {
                        // over
                        ganttMaster.addProcess(currProc.getPName(), started, time);
                        started = time;
                        if (readyQueue.size() > 0) {
                            currProc = readyQueue.remove();
                        } else {
                            currProc = new ProcessObject(null);
                        }
                    } else if (readyQueue.size() > 0 && currProc.getPriority() > readyQueue.peek().getPriority()) {
                        // preempt
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

            if (currProc.getRemBurst() == -1 && readyQueue.size() == 0 && pList.size() ==0) {
                double avgWT, avgTA;
                int i;
                TableObject newElem;
                LinkedList<TableObject> taList = new LinkedList<>();
                LinkedList<TableObject> wtList = new LinkedList<>();
                ArrayList<GanttChartObj> tmpList;
                ListIterator<GanttChartObj> travTmp;
                GanttChartObj buff, buffPrev;

                avgWT = avgTA = 0.0;

                st.write("HTTP/1.1 200 OK\r\n");
                st.write("Content-Type: text/plain\r\n");
                st.write("\r\n");

                st.write("<br><h1>Gantt Chart</h1><br><br>");
                st.write("<div class='row'><div class='col-xs-12'><table class='table table-bordered table-hover'><tr>");

                for(String pName : ganttMaster.getPNameList()) {
                    st.write("<td class='success'><h4 class='text-center'><strong>" + pName + "</strong></h4></td>");
                }

                st.write("</tr><tr>");

                long[] ganttNums = ganttMaster.getNums();

                for(i=0; i<ganttNums.length-2; i++){
                    st.write("<td class='danger'>"+ ganttNums[i]+"</td>");
                }
                st.write("<td class='danger'>"+ ganttNums[i] + "<div class='pull-right'>" + ganttNums[i+1] +"</div></td>");

                st.write("</tr></table></div></div>");

                for(ProcessObject trav : orgPList) {
                    newElem = new TableObject(trav.getPName());

                    tmpList = ganttMaster.getGanttListByName(trav.getPName());
                    travTmp = tmpList.listIterator();

                    newElem.addEquation(travTmp.next().getStart(), trav.getArrival());

                    while(travTmp.hasNext()) {
                        buff = travTmp.next();
                        travTmp.previous();
                        buffPrev = travTmp.previous();
                        travTmp.next(); travTmp.next();

                        newElem.addEquation(buff.getStart(), buffPrev.getEnd());
                    }

                    wtList.add(newElem);
                    taList.add(new TableObject(trav.getPName(), ganttMaster.getLastOccurrence(trav.getPName()).getEnd(), trav.getArrival()));
                }

                st.write("<div class='row'><div class='col-md-6'><h3>Turnaround Time</h3>");
                st.write("<table class='table table-striped'>");

                for(TableObject trav: taList) {
                    st.write("<tr><td colspan='2'>"+ trav.getPName()+" = "+ trav.getFirstEqStr(" ")+" = "+ trav.getFirstResult()+"ms</td> </tr>");
                    avgTA += trav.getFirstResult();
                }
                avgTA /= orgPList.size();

                st.write("<tr><td colspan='2'><strong>Average Turnaround Time = "+String.format("%.2fms", avgTA)+"</strong></td></tr>");
                st.write("</table></div>");

                st.write("<div class='col-md-6'><h3>Waiting Time</h3>");
                st.write("<table class='table table-striped'>");

                for(TableObject trav : wtList) {
                    st.write("<tr><td colspan='2'>"+ trav.getPName()+" = ");
                    i=0;
                    for(String eqTrav : trav.getAllEqStr(" ")) {
                        st.write("&nbsp("+ eqTrav +")"+ (++i < trav.getEqCount()? "&nbsp+" : "" )+"");
                    }
                    st.write(" = "+ trav.getResults()+"ms</td></tr>");
                    avgWT += (double)trav.getResults();
                }
                avgWT /= orgPList.size();

                st.write("<tr><td colspan='2'><strong>Average Waiting Time = "+String.format("%.2fms", avgWT)+"</td></tr>");
                st.write("</table></div></div>");

                st.close();

                System.out.println("DONE!");

                cancel();
            }
        }
    }

    public LinkedList<ProcessObject> getPObjects(String res)
    {
        LinkedList<ProcessObject> ret = new LinkedList<>();

        String[] resS = res.split("}");

        for(String s : resS) {
            ret.add(new ProcessObject(s));
        }

        return ret;
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
