package org.apache.poi.ss.formula.functions;

import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.functions.Fixed3ArgFunction;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateDifFunc extends Fixed3ArgFunction {
    @Override
    public ValueEval evaluate(int i, int i1, ValueEval valueEval, ValueEval valueEval1, ValueEval valueEval2) {

        try {
            ValueEval v1 = OperandResolver.getSingleValue( valueEval,
                    i, i1 );
            ValueEval v2 = OperandResolver.getSingleValue( valueEval1,
                    i, i1 );
            ValueEval v3 = OperandResolver.getSingleValue( valueEval2,
                    i, i1 );

            // start date and end date are both inclusive, exclude them out so -2
            LocalDate firstDate = LocalDate.of(1900, 1, 1).plusDays(OperandResolver.coerceValueToInt( v1 ) - 2);
            LocalDate secondDate = LocalDate.of(1900, 1, 1).plusDays(OperandResolver.coerceValueToInt( v2 ) - 2);
            String timeUnit = OperandResolver.coerceValueToString( v3 ).toUpperCase();
            return new NumberEval(dateDif(firstDate, secondDate, timeUnit));

        } catch (EvaluationException e) {
            e.printStackTrace();
        }
        return null;

    }


    private int dateDif(LocalDate fromDate, LocalDate toDate, String timeUnit) {
        Date legacyFromDate = Date.from(fromDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date legacyToDate = Date.from(toDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        Period between = Period.between(fromDate, toDate);

        int nDay = 0;
        GregorianCalendar firstArg = new GregorianCalendar();
        firstArg.setTime(legacyFromDate);
        GregorianCalendar secondArg = new GregorianCalendar();
        secondArg.setTime(legacyToDate);
        if (timeUnit.equals("D")) {
            nDay = Math.abs((int) ((legacyFromDate.getTime() - legacyToDate.getTime()) /
                    (24.0 * 60 * 60 * 1000)));
        }
        if (timeUnit.equals("M")) {
            nDay = Math.abs((int) between.get(ChronoUnit.YEARS)) * 12 +
                    Math.abs((int) between.get(ChronoUnit.MONTHS));
        }
        if (timeUnit.equals("Y")) {
            nDay = Math.abs(firstArg.get(Calendar.YEAR) - secondArg.get(Calendar.YEAR));
        }
        if (timeUnit.equals("MD")) {
            nDay = Math.abs((int) between.get(ChronoUnit.DAYS));
        }
        if (timeUnit.equals("YM")) {
            nDay = Math.abs((int) between.get(ChronoUnit.MONTHS));
        }
        if (timeUnit.equals("YD")) {
            nDay = Math.abs((int) between.get(ChronoUnit.YEARS));
        }

        return nDay;
    }
}
