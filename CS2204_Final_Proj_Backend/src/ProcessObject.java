import java.io.StringReader;

/**
 * Created by theLa on Feb-09-2017.
 */
public final class ProcessObject {
    private final String pName;
    private final int arrival, burst, priority;
    private int remBurst;

    public ProcessObject(String pName, int arrival, int burst, int priority)
    {
        this.pName = pName;
        this.arrival = arrival;
        this.burst = this.remBurst = burst;
        this.priority = priority;
    }

    public ProcessObject(String res)
    {
        int i, y;
        StringBuilder[] parsed = new StringBuilder[4];
        if (res != null) {
            for (int z = 0; z < parsed.length; z++) {
                parsed[z] = new StringBuilder();
            } // init Stringbuilder


            for (y = 0, i = 1; i != res.length(); i++) {
                if (res.charAt(i) != '|') {
                    parsed[y].append(res.charAt(i));
                } else {
                    y++;
                }
            }

            this.pName = parsed[0].toString();
            this.arrival = Integer.parseInt(parsed[1].toString());
            this.burst = this.remBurst = Integer.parseInt(parsed[2].toString());
            this.priority = Integer.parseInt(parsed[3].toString());
        } else {
            this.pName = "(idle)";
            this.arrival = this.burst = this.remBurst = this.priority = -1;
        }

    }

    public String getPName()
    {
        return  pName;
    }

    public int getArrival()
    {
        return arrival;
    }

    public int getBurst()
    {
        return burst;
    }

    public int getPriority()
    {
        return priority;
    }

    public int getRemBurst()
    {
        return remBurst;
    }

    public void process()
    {
        remBurst--;
    }
}
