package org.apache.poi.ss.formula.functions;

import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.ValueEval;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateDifFunc extends Fixed3ArgFunction {
    @Override
    public ValueEval evaluate(int i, int i1, ValueEval valueEval, ValueEval valueEval1, ValueEval valueEval2) {
        System.out.printf("passing the valley of death %s %s %s %s %s.",
                i, i1, valueEval.toString(), valueEval1, valueEval2);

        try {
            ValueEval v1 = OperandResolver.getSingleValue( valueEval,
                    i, i1 );
            ValueEval v2 = OperandResolver.getSingleValue( valueEval1,
                    i, i1 );
            ValueEval v3 = OperandResolver.getSingleValue( valueEval2,
                    i, i1 );

            System.out.printf("GOGOGO!!! %s %s %s \r\n",
                    OperandResolver.coerceValueToString( v1 ),
                    OperandResolver.coerceValueToInt( v2 ),
                    OperandResolver.coerceValueToString( v3 ));

            LocalDate firstDate = LocalDate.of(1900, 1, 1).plusDays(OperandResolver.coerceValueToInt( v1 ));
            LocalDate secondDate = LocalDate.of(1900, 1, 1).plusDays(OperandResolver.coerceValueToInt( v2 ));
            String timeUnit = OperandResolver.coerceValueToString( v3 ).toUpperCase();
            return new NumberEval(dateDif(Date.from(firstDate.atStartOfDay(ZoneId.systemDefault()).toInstant()),
                    Date.from(secondDate.atStartOfDay(ZoneId.systemDefault()).toInstant()),
                    timeUnit));

        } catch (EvaluationException e) {
            e.printStackTrace();
        }
        return null;

    }


    private int dateDif(Date firstDate, Date secondDate, String timeUnit) {
        int nDay = 0;
        GregorianCalendar firstArg = new GregorianCalendar();
        firstArg.setTime(firstDate);
        GregorianCalendar secondArg = new GregorianCalendar();
        secondArg.setTime(secondDate);
        if (timeUnit.equals("D")) {
            nDay = Math.abs((int) ((firstDate.getTime() - secondDate.getTime()) /
                    (24.0 * 60 * 60 * 1000)));
        }
        if (timeUnit.equals("M")) {
            if ((firstArg.get(Calendar.YEAR) == secondArg.get(Calendar.YEAR)) &&
                    (firstArg.get(Calendar.MONTH) == secondArg.get(Calendar.MONTH))) {
                nDay = 0;
            } else {
                nDay = Math.abs(((12 * (firstArg.get(Calendar.YEAR) -
                        secondArg.get(Calendar.YEAR))) + firstArg.get(Calendar.MONTH) -
                        secondArg.get(Calendar.MONTH))) - 1;
            }
        }
        if (timeUnit.equals("Y")) {
            nDay = Math.abs(firstArg.get(Calendar.YEAR) - secondArg.get(Calendar.YEAR));
        }
        if (timeUnit.equals("MD")) {
            nDay =  Math.abs(firstArg.get(Calendar.DAY_OF_MONTH) -
                    secondArg.get(Calendar.DAY_OF_MONTH));
        }
        if (timeUnit.equals("YM")) {
            nDay = Math.abs(firstArg.get(Calendar.MONTH) - secondArg.get(Calendar.MONTH));
        }
        if (timeUnit.equals("YD")) {
            nDay =  Math.abs(firstArg.get(Calendar.DAY_OF_YEAR) -
                    secondArg.get(Calendar.DAY_OF_YEAR));
        }

        return nDay;
    }

    public static void main(String [] args) {
        LocalDate d = LocalDate.of(1900, 1, 1).plusDays(42005);
        System.out.println(d.toString());
    }
}
