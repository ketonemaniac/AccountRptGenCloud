package org.apache.poi.ss.formula.functions;

import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.ptg.IntPtg;
import org.apache.poi.ss.formula.ptg.StringPtg;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class DateDifFuncTest {

    @Test
    public void testEvaluateMonths() {
        DateDifFunc func = new DateDifFunc();
        ValueEval dateFrom = new NumberEval(new IntPtg(42655));   // 2016-10-12
        ValueEval dateTo = new NumberEval(new IntPtg(43191));   // 2018-04-01
        ValueEval dateUnit = new StringEval(new StringPtg("m"));
        NumberEval result = (NumberEval) func.evaluate(0,0, dateFrom, dateTo, dateUnit);
        Assertions.assertThat(result.getNumberValue()).isEqualTo(17);
    }

    @Test
    public void testEvaluateMonthsExactlyOneYear() {
        DateDifFunc func = new DateDifFunc();
        ValueEval dateFrom = new NumberEval(new IntPtg(42826));   // 2017-04-01
        ValueEval dateTo = new NumberEval(new IntPtg(43191));   // 2018-04-01
        ValueEval dateUnit = new StringEval(new StringPtg("m"));
        NumberEval result = (NumberEval) func.evaluate(0,0, dateFrom, dateTo, dateUnit);
        Assertions.assertThat(result.getNumberValue()).isEqualTo(12);
    }

    @Test
    public void testEvaluateModDays() {
        DateDifFunc func = new DateDifFunc();
        ValueEval dateFrom = new NumberEval(new IntPtg(42655));   // 2016-10-12
        ValueEval dateTo = new NumberEval(new IntPtg(43191));   // 2018-04-01
        ValueEval dateUnit = new StringEval(new StringPtg("md"));
        NumberEval result = (NumberEval) func.evaluate(0,0, dateFrom, dateTo, dateUnit);
        // should be 20 days instead of 11 days (11 = just looking at the day portion)
        Assertions.assertThat(result.getNumberValue()).isEqualTo(20);
    }

    @Test
    public void testEvaluateModMonths() {
        DateDifFunc func = new DateDifFunc();
        ValueEval dateFrom = new NumberEval(new IntPtg(42655));   // 2016-10-12
        ValueEval dateTo = new NumberEval(new IntPtg(43191));   // 2018-04-01
        ValueEval dateUnit = new StringEval(new StringPtg("ym"));
        NumberEval result = (NumberEval) func.evaluate(0,0, dateFrom, dateTo, dateUnit);
        // should be 20 days instead of 11 days (11 = just looking at the day portion)
        Assertions.assertThat(result.getNumberValue()).isEqualTo(5);
    }
}
