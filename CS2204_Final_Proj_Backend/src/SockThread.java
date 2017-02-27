/**
 * Created by theLa on Feb-08-2017.
 */

import java.net.Socket;
import java.net.SocketException;
import java.util.*;
public class SockThread extends Thread {
    private final Socket sock; // SOCKET

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
        LinkedList<ProcessObject> processList;
        StreamController st;

        st = new StreamController(sock);
        processList = getPObjectsFromStream(st);

        new PriorityPreemptiveScheduler(processList, st).schedule();
    }

    public LinkedList<ProcessObject> getPObjectsFromStream(StreamController st)
    {
        LinkedList<ProcessObject> ret;

        // Do code here
        StringBuilder raw = new StringBuilder(100);
        StringBuilder post = new StringBuilder();

        System.out.println("Accepted client: " + sock.getRemoteSocketAddress());

        String buff; // string buffer;

        int conLen = 0;

        while (!(buff = st.readLine()).equals("")) {
            raw.append(buff);
            if (buff.startsWith(conHeader)) {
                conLen = Integer.parseInt(buff.substring(conHeader.length()));
            }
            System.out.print(buff);
        }

        for(int i=0; i<conLen; i++) {
            post.append((char)st.read());
        }

        System.out.println(post.toString());

        ret = parsePObjectsFromPostString(post.toString());

        ////
        // -> do the code here
        //Timer t1 = new Timer();

        //t1.schedule(new SchedulerTask(pList, st), 0, 1);

        // get requested page
        // reqPage = sb.substring(sb.indexOf("GET ") + 4, sb.indexOf("HTTP/")-1);


        return ret;
    }

    public LinkedList<ProcessObject> parsePObjectsFromPostString(String res)
    {
        LinkedList<ProcessObject> ret = new LinkedList<>();
        String[] resS;

        if (res.length() != 0) {
            resS= res.split("}");

            for (String s : resS) {
                ret.add(new ProcessObject(s));
            }
        }

        return ret;
    }
}
