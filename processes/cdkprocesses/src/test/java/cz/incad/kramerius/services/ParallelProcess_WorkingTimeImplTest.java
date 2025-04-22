package cz.incad.kramerius.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;


public class ParallelProcess_WorkingTimeImplTest {
    
    /** Simple case */
    @Test
    public void testIsWorkingTimeSimple() {
            
        // 22:19 - window is 08:20 - 23:00 -> true 
        Pair<LocalDate,LocalDateTime> p0 = dateTime(2023, Month.OCTOBER, 6, 22, 19, 35);
        Assert.assertTrue(ParallelProcessImpl.isWorkingTimeImpl("08:20", "23:00", p0.getLeft(), p0.getRight()));

        // 07:52 - window is 08:20 - 23:00 -> false 
        Pair<LocalDate,LocalDateTime> p1 = dateTime(2023, Month.OCTOBER, 6, 7, 52, 35);
        Assert.assertFalse(ParallelProcessImpl.isWorkingTimeImpl("08:20", "23:00", p1.getLeft(), p1.getRight()));

        // 08:25 - window is 08:20 - 23:00 -> true 
        Pair<LocalDate,LocalDateTime> p2 = dateTime(2023, Month.OCTOBER, 7, 8, 25, 35);
        Assert.assertTrue(ParallelProcessImpl.isWorkingTimeImpl("08:20", "23:00", p2.getLeft(), p2.getRight()));
        
        // 23:01 - window is 08:20 - 23:00 -> false 
        Pair<LocalDate,LocalDateTime> p3 = dateTime(2023, Month.OCTOBER, 7, 23, 01, 35);
        Assert.assertFalse(ParallelProcessImpl.isWorkingTimeImpl("08:20", "23:00", p3.getLeft(), p3.getRight()));

        // 22:59 - window is 08:20 - 23:00 -> true 
        Pair<LocalDate,LocalDateTime> p4 = dateTime(2023, Month.OCTOBER, 7, 22, 59, 35);
        Assert.assertTrue(ParallelProcessImpl.isWorkingTimeImpl("08:20", "23:00", p4.getLeft(), p4.getRight()));
    }
    
    /** Over midnight case */
    @Test
    public void testIsWorkingTimeMidnight() {
            
        // 22:19 - window is 22:20 - 05:00 -> false 
        Pair<LocalDate,LocalDateTime> p0 = dateTime(2023, Month.OCTOBER, 6, 22, 19, 35);
        Assert.assertFalse(ParallelProcessImpl.isWorkingTimeImpl("22:20", "05:00", p0.getLeft(), p0.getRight()));

        // 23:52 - window is 22:20 - 05:00 -> true 
        Pair<LocalDate,LocalDateTime> p1 = dateTime(2023, Month.OCTOBER, 6, 23, 52, 35);
        Assert.assertTrue(ParallelProcessImpl.isWorkingTimeImpl("22:20", "05:00", p1.getLeft(), p1.getRight()));

        // 00:00 - window is 22:20 - 05:00 -> true 
        Pair<LocalDate,LocalDateTime> p2 = dateTime(2023, Month.OCTOBER, 7, 00, 00, 35);
        Assert.assertTrue(ParallelProcessImpl.isWorkingTimeImpl("22:20", "05:00", p2.getLeft(), p2.getRight()));
        
        // 04:59 - window is 22:20 - 05:00 -> true 
        Pair<LocalDate,LocalDateTime> p3 = dateTime(2023, Month.OCTOBER, 7, 04, 59, 59);
        Assert.assertTrue(ParallelProcessImpl.isWorkingTimeImpl("22:20", "05:00", p3.getLeft(), p3.getRight()));

        // 05:00 - window is 22:20 - 05:00 -> false 
        Pair<LocalDate,LocalDateTime> p4 = dateTime(2023, Month.OCTOBER, 7, 05, 00, 00);
        Assert.assertFalse(ParallelProcessImpl.isWorkingTimeImpl("22:20", "05:00", p4.getLeft(), p4.getRight()));

    }

    /** Waiting time calculation; simple case */
    @Test
    public void testWaitingTimeSimple() throws InterruptedException {
        // 22:20 - window is 8:20 - 23:00 -> wait for next day; 10 hours 
        Pair<LocalDate,LocalDateTime> p0 = dateTime(2023, Month.OCTOBER, 6, 22, 20, 00);
        long wait0 = ParallelProcessImpl.waitUntilStartWorkingTimeImpl("08:20", "23:00", p0.getLeft(), p0.getRight());
        long toMidnight = 6000000L;
        long afterMidnght = 28800000L+1200000L;
        Assert.assertTrue(wait0 == (toMidnight+afterMidnght));
        Assert.assertTrue(ParallelProcessImpl.isWorkingTimeImpl("08:20", "23:00", p0.getLeft(), p0.getRight()));

        // 07:20 -  window is 8:20 - 23:00 -> 1 hour 
        Pair<LocalDate,LocalDateTime> p1 = dateTime(2023, Month.OCTOBER, 6, 07, 20, 00);
        long wait1 = ParallelProcessImpl.waitUntilStartWorkingTimeImpl("08:20", "23:00", p1.getLeft(), p1.getRight());
        Assert.assertTrue(wait1 == 3600000L);
        Assert.assertFalse(ParallelProcessImpl.isWorkingTimeImpl("08:20", "23:00", p1.getLeft(), p1.getRight()));

        
        // 03:20 -  window is 8:20 - 23:00 -> 5 hour 
        Pair<LocalDate,LocalDateTime> p2 = dateTime(2023, Month.OCTOBER, 6, 03, 20, 00);
        long wait2 = ParallelProcessImpl.waitUntilStartWorkingTimeImpl("08:20", "23:00", p2.getLeft(), p2.getRight());
        Assert.assertTrue(wait2 == 18000000L);
        Assert.assertFalse(ParallelProcessImpl.isWorkingTimeImpl("08:20", "23:00", p2.getLeft(), p2.getRight()));

    }
    
    @Test
    public void testWaitingTimeMidnight() throws InterruptedException {
        // 22:19:35  neni v intervalu a ceka 25 sekund
        Pair<LocalDate,LocalDateTime> p0 = dateTime(2023, Month.OCTOBER, 6, 22, 19, 35);
        long wait1 = ParallelProcessImpl.waitUntilStartWorkingTimeImpl("22:20", "05:00", p0.getLeft(), p0.getRight());
        Assert.assertTrue(wait1 == 25000);

        // 16:20:00  neni v intervalu a ceka 6 hodin
        Pair<LocalDate,LocalDateTime> p1 = dateTime(2023, Month.OCTOBER, 6, 16, 20, 00);
        long wait2 = ParallelProcessImpl.waitUntilStartWorkingTimeImpl("22:20", "05:00", p1.getLeft(), p1.getRight());
        Assert.assertTrue(wait2 == 21600000);

        
        // 22:30 - je v intervalu; tedy pracuje a pokud ceka, pak ceka dalsi kolo 23 hodin; 50 minut
        Pair<LocalDate,LocalDateTime> p2 = dateTime(2023, Month.OCTOBER, 6, 22, 30, 00);
        long wait3 = ParallelProcessImpl.waitUntilStartWorkingTimeImpl("22:20", "05:00", p2.getLeft(), p2.getRight());
        long twentythree = 82800000L;
        long fifty = 3000000L;
        Assert.assertTrue(wait3 == (twentythree + fifty));
    }
    
    
    private Pair<LocalDate, LocalDateTime>dateTime(int year, Month month, int dayOfMonth, int hour, int minutes, int seconds) {
        LocalDate date = LocalDate.of(year, month, dayOfMonth);
        LocalDateTime dateTime = LocalDateTime.of(year, month, dayOfMonth, hour, minutes, seconds);
        return Pair.of(date, dateTime);
    }
}
