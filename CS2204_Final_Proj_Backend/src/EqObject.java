/**
 * Created by theLa on Feb-13-2017.
 */
public class EqObject
{
    private final long op1;
    private final long op2;
    private final long result;
    private final OperationType op;

    public EqObject(long op1, long op2, OperationType op)
    {
        this.op1 = op1;
        this.op2 = op2;
        this.op = op;

        switch (op)
        {
            case ADDITION:
                this.result = op1 + op2; break;
            case SUBTRACTION:
                this.result = op1 - op2; break;
            case DIVISION:
                this.result = op1 / op2; break;
            case MULTIPLICATION:
                this.result = op1 * op2; break;
            default:
                this.result = 0;
        }
    }

    public long getOp1()
    {
        return op1;
    }

    public long getOp2()
    {
        return op2;
    }

    public OperationType getOperation()
    {
        return op;
    }

    public long getResult()
    {
        return result;
    }

    private char getOpChar()
    {
        char ret = 0;
        switch (op) {
            case ADDITION:
                ret = '+'; break;
            case SUBTRACTION:
                ret = '-'; break;
            case DIVISION:
                ret = '/'; break;
            case MULTIPLICATION:
                ret = '*'; break;
        }

        return ret;
    }

    public String getEq(String separator)
    {
        return String.valueOf(op1) + separator + getOpChar() + separator +String.valueOf(op2);
    }
}
