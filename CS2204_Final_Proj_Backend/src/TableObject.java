import java.util.ArrayList;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Created by theLa on Feb-13-2017.
 */
public class TableObject
{
    private final String pName;
    private LinkedList<EqObject> eqList;
    private int eqCount;

    public TableObject(String pName, long op1, long op2) {
        this.pName = pName;
        this.eqList = new LinkedList<>();
        this.eqList.add(new EqObject(op1,op2,OperationType.SUBTRACTION));
        this.eqCount = 1;
    }

    public TableObject(String pName)
    {
        this.pName = pName;
        this.eqList = new LinkedList<>();
        this.eqCount = 0;
    }

    public String getPName()
    {
        return pName;
    }

    public void addEquation(EqObject equation)
    {
        this.eqList.add(equation);
        eqCount++;
    }

    public void addEquation(long op1, long op2)
    {
        this.eqList.add(new EqObject(op1, op2, OperationType.SUBTRACTION));
        eqCount++;
    }

    public String getFirstEqStr(String separ)
    {
        return eqList.getFirst().getEq(separ);
    }

    public long getFirstResult()
    {
        return eqList.getFirst().getResult();
    }

    public long getResults()
    {
        long ret = 0;

        for(EqObject trav : eqList) {
            ret += trav.getResult();
        }

        return ret;
    }

    public ArrayList<String> getAllEqStr(String separ)
    {
        return eqList.stream().map(eq -> eq.getEq(separ)).collect(Collectors.toCollection(ArrayList::new));
    }

    public int getEqCount()
    {
        return eqCount;
    }
}