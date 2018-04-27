package com.example.sj.keymeasures;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    public ExampleUnitTest (){
    }

    private static final int ITERATIONS = (1<<14);
    private AppDatabase db;
    private KeyDirectionsDao keyDirectionsDao;
    private WorkSessionDao workSessionDao;
    private static final int N= 0x400;

    @Before
    public void createDb() {
        Context context= InstrumentationRegistry.getTargetContext();
        db= Room.inMemoryDatabaseBuilder(context,AppDatabase.class).build();
        keyDirectionsDao= db.keyDirectionsDao();
        workSessionDao= db.workSessionDao();
    }
    @After
    public void closeDb() throws Exception {
        db.close();
    }
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testadding_new_direction_is_correct() throws Exception {
        KeyDirections []lst= new KeyDirections[N];
        for ( int i= 0; i < N; ++i ) {
            KeyDirections keyDirections= new KeyDirections();
            keyDirections.setActivityName(String.format("activity%04d",i));
            lst[i]= keyDirections;
        }
        keyDirectionsDao.insertAll(lst);
        List<KeyDirections> res= keyDirectionsDao.getAll();
        assertEquals(res.size(),lst.length);
        Collections.sort(res);
        for ( int i= 0; i < N; ++i )
            assertEquals(res.get(i),lst[i]);
    }

    @Test
    public void testadding_sessions_is_correct() throws Exception {
        ThreadLocalRandom threadLocalRandom= ThreadLocalRandom.current();
        KeyDirections []lst= new KeyDirections[N];
        int i,j,k;
        for ( i= 0; i < N; ++i ) {
            KeyDirections keyDirections= new KeyDirections();
            keyDirections.setActivityName(String.format("activity%04d",i));
            lst[i]= keyDirections;
        }
        keyDirectionsDao.insertAll(lst);
        Map<Integer,Integer> map= new TreeMap<>();
        for ( i= 0; i < ITERATIONS; ++i ) {
            WorkSession ws= new WorkSession();
            ws.setWhen(LocalDateTime.now());
            ws.setDuration(k= 1+(threadLocalRandom.nextInt() % 60));
            ws.setAid(j= threadLocalRandom.nextInt()%N);
            workSessionDao.insertAll(ws);
            if ( !map.containsKey(j) )
                map.put(j,0);
            map.put(j,map.get(j)+k);
        }
        List<Converter01.Model01> res= workSessionDao.summarize(LocalDateTime.ofEpochSecond(0,0, ZoneOffset.UTC));
        assertEquals(res.size(),map.size());
        Collections.sort(res);
        i= 0;
        for ( Map.Entry<Integer,Integer> entry: map.entrySet() ) {
            int xx= (int)(res.get(i).timeInvested+1e-13);
            assertEquals((long)entry.getValue(),xx);
        }
    }
}