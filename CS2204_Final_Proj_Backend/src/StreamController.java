import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by theLa on Feb-08-2017.
 */
public final class StreamController {
    private final PrintWriter out;
    private final BufferedReader in;
    private final Socket sock;
    public StreamController(Socket sock)
    {
        PrintWriter outTmp = null;
        BufferedReader inTmp = null;

        this.sock = sock;

        try {
            outTmp = new PrintWriter(sock.getOutputStream(), true);
            inTmp = new BufferedReader(new InputStreamReader(sock.getInputStream()));
        } catch (IOException ex) {
            System.out.println("Unable to get streams from socket: " + ex.toString());
        } finally {
            out = outTmp;
            in = inTmp;
        }
    }

    public PrintWriter getWriter()
    {
        return out;
    }

    public PrintWriter getOut()
    {
        return out;
    }

    public BufferedReader getIn()
    {
        return in;
    }

    public String readLine()
    {
        String ret = null;
        try {
            ret = in.readLine();
        } catch (IOException ex) {
            System.out.println("Unable to read from socket stream . . .");
        }

        return ret;
    }

    public int read()
    {
        int ret;

        try {
            ret = in.read();
        } catch (IOException ex) {
            System.out.println("Unable to read from socket stream . . .");
            ret = Integer.MIN_VALUE;
        }

        return ret;
    }

    public void writeLine(String out)
    {
        this.out.println(out);
    }

    public void write(String out)
    {
        this.out.print(out);
    }

    public void close() {
        out.close();
        try {
            in.close();
            sock.close();
        } catch (IOException ex) {
            System.out.println("Socket close failed: " + ex.toString());
        }
    }
}
