/**
 * Created by theLa on Feb-09-2017.
 */
public class GanttChartObj {
    private final String pName;
    private final long start;
    private final long end;

    public GanttChartObj(String pName, long start, long end)
    {
        this.pName = pName;
        this.start = start;
        this.end = end;
    }

    public long getStart()
    {
        return start;
    }

    public long getEnd()
    {
        return end;
    }

    public String getPName()
    {
        return pName;
    }
}
