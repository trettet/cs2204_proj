/**
 * Created by theLa on Feb-09-2017.
 */
public class GanttChartObj {
    private final String pName;
    private final long start;
    private final long end;
    private double width;
    private boolean isWidthSet;

    public GanttChartObj(String pName, long start, long end)
    {
        this.pName = pName;
        this.start = start;
        this.end = end;
        this.isWidthSet = false;
    }

    public void setWidth(double width)
    {
        if (!isWidthSet) {
            this.width = width;
            isWidthSet = true;
        } else {
            System.out.print("oops");
        }
        System.out.print("Width set to :" + width);
    }

    public double getWidth()
    {
        return width;
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
