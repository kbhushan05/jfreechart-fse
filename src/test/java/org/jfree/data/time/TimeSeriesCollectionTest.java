/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2012, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates. 
 * Other names may be trademarks of their respective owners.]
 *
 * ------------------------------
 * TimeSeriesCollectionTests.java
 * ------------------------------
 * (C) Copyright 2003-2012, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 01-May-2003 : Version 1 (DG);
 * 04-Dec-2003 : Added a test for the getSurroundingItems() method (DG);
 * 08-May-2007 : Added testIndexOf() method (DG);
 * 18-May-2009 : Added testFindDomainBounds() (DG);
 * 08-Jan-2012 : Added testBug3445507() (DG);
 *
 */

package org.jfree.data.time;

import org.jfree.data.Range;
import org.jfree.data.general.DatasetUtilities;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * A collection of test cases for the {@link TimeSeriesCollection} class.
 */
public class TimeSeriesCollectionTest  {





    /**
     * Some tests for the equals() method.
     */
    @Test
    public void testEquals() {
        TimeSeriesCollection c1 = new TimeSeriesCollection();
        TimeSeriesCollection c2 = new TimeSeriesCollection();

        TimeSeries s1 = new TimeSeries("Series 1");
        TimeSeries s2 = new TimeSeries("Series 2");

        // newly created collections should be equal
        boolean b1 = c1.equals(c2);
        assertTrue("b1", b1);

        // add series to collection 1, should be not equal
        c1.addSeries(s1);
        c1.addSeries(s2);
        boolean b2 = c1.equals(c2);
        assertFalse("b2", b2);

        // now add the same series to collection 2 to make them equal again...
        c2.addSeries(s1);
        c2.addSeries(s2);
        boolean b3 = c1.equals(c2);
        assertTrue("b3", b3);

        // now remove series 2 from collection 2
        c2.removeSeries(s2);
        boolean b4 = c1.equals(c2);
        assertFalse("b4", b4);

        // now remove series 2 from collection 1 to make them equal again
        c1.removeSeries(s2);
        boolean b5 = c1.equals(c2);
        assertTrue("b5", b5);
    }

    /**
     * Tests the remove series method.
     */
    @Test
    public void testRemoveSeries() {
        TimeSeriesCollection c1 = new TimeSeriesCollection();

        TimeSeries s1 = new TimeSeries("Series 1");
        TimeSeries s2 = new TimeSeries("Series 2");
        TimeSeries s3 = new TimeSeries("Series 3");
        TimeSeries s4 = new TimeSeries("Series 4");

        c1.addSeries(s1);
        c1.addSeries(s2);
        c1.addSeries(s3);
        c1.addSeries(s4);

        c1.removeSeries(s3);

        TimeSeries s = c1.getSeries(2);
        boolean b1 = s.equals(s4);
        assertTrue(b1);
    }

    /**
     * Some checks for the {@link TimeSeriesCollection#removeSeries(int)}
     * method.
     */
    @Test
    public void testRemoveSeries_int() {
        TimeSeriesCollection c1 = new TimeSeriesCollection();
        TimeSeries s1 = new TimeSeries("Series 1");
        TimeSeries s2 = new TimeSeries("Series 2");
        TimeSeries s3 = new TimeSeries("Series 3");
        TimeSeries s4 = new TimeSeries("Series 4");
        c1.addSeries(s1);
        c1.addSeries(s2);
        c1.addSeries(s3);
        c1.addSeries(s4);
        c1.removeSeries(2);
        assertEquals(c1.getSeries(2), s4);
        c1.removeSeries(0);
        assertEquals(c1.getSeries(0), s2);
        assertEquals(2, c1.getSeriesCount());
    }

    /**
     * Test the getSurroundingItems() method to ensure it is returning the
     * values we expect.
     */
    @Test
    public void testGetSurroundingItems() {
        TimeSeries series = new TimeSeries("Series 1");
        TimeSeriesCollection collection = new TimeSeriesCollection(series);
        collection.setXPosition(TimePeriodAnchor.MIDDLE);

        // for a series with no data, we expect {-1, -1}...
        int[] result = collection.getSurroundingItems(0, 1000L);
        assertSame(result[0], -1);
        assertSame(result[1], -1);

        // now test with a single value in the series...
        Day today = new Day();
        long start1 = today.getFirstMillisecond();
        long middle1 = today.getMiddleMillisecond();
        long end1 = today.getLastMillisecond();

        series.add(today, 99.9);
        result = collection.getSurroundingItems(0, start1);
        assertSame(result[0], -1);
        assertSame(result[1], 0);

        result = collection.getSurroundingItems(0, middle1);
        assertSame(result[0], 0);
        assertSame(result[1], 0);

        result = collection.getSurroundingItems(0, end1);
        assertSame(result[0], 0);
        assertSame(result[1], -1);

        // now add a second value to the series...
        Day tomorrow = (Day) today.next();
        long start2 = tomorrow.getFirstMillisecond();
        long middle2 = tomorrow.getMiddleMillisecond();
        long end2 = tomorrow.getLastMillisecond();

        series.add(tomorrow, 199.9);
        result = collection.getSurroundingItems(0, start2);
        assertSame(result[0], 0);
        assertSame(result[1], 1);

        result = collection.getSurroundingItems(0, middle2);
        assertSame(result[0], 1);
        assertSame(result[1], 1);

        result = collection.getSurroundingItems(0, end2);
        assertSame(result[0], 1);
        assertSame(result[1], -1);

        // now add a third value to the series...
        Day yesterday = (Day) today.previous();
        long start3 = yesterday.getFirstMillisecond();
        long middle3 = yesterday.getMiddleMillisecond();
        long end3 = yesterday.getLastMillisecond();

        series.add(yesterday, 1.23);
        result = collection.getSurroundingItems(0, start3);
        assertSame(result[0], -1);
        assertSame(result[1], 0);

        result = collection.getSurroundingItems(0, middle3);
        assertSame(result[0], 0);
        assertSame(result[1], 0);

        result = collection.getSurroundingItems(0, end3);
        assertSame(result[0], 0);
        assertSame(result[1], 1);
    }

    /**
     * Serialize an instance, restore it, and check for equality.
     */
    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        TimeSeriesCollection c1 = new TimeSeriesCollection(createSeries());

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(buffer);
            out.writeObject(c1);
            out.close();

            ObjectInput in = new ObjectInputStream(
                    new ByteArrayInputStream(buffer.toByteArray()));
        TimeSeriesCollection c2 = (TimeSeriesCollection) in.readObject();
            in.close();

        assertEquals(c1, c2);
    }

    /**
     * Creates a time series for testing.
     *
     * @return A time series.
     */
    private TimeSeries createSeries() {
        RegularTimePeriod t = new Day();
        TimeSeries series = new TimeSeries("Test");
        series.add(t, 1.0);
        t = t.next();
        series.add(t, 2.0);
        t = t.next();
        series.add(t, null);
        t = t.next();
        series.add(t, 4.0);
        return series;
    }

    /**
     * A test for bug report 1170825.
     */
    @Test
    public void test1170825() {
        TimeSeries s1 = new TimeSeries("Series1");
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(s1);
        try {
            /* TimeSeries s = */ dataset.getSeries(1);
            fail("Should have thrown an IllegalArgumentException on index out of bounds");
        }
        catch (IllegalArgumentException e) {
            assertEquals("The 'series' argument is out of bounds (1).", e.getMessage());
        }

    }

    /**
     * Some tests for the indexOf() method.
     */
    @Test
    public void testIndexOf() {
        TimeSeries s1 = new TimeSeries("S1");
        TimeSeries s2 = new TimeSeries("S2");
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        assertEquals(-1, dataset.indexOf(s1));
        assertEquals(-1, dataset.indexOf(s2));

        dataset.addSeries(s1);
        assertEquals(0, dataset.indexOf(s1));
        assertEquals(-1, dataset.indexOf(s2));

        dataset.addSeries(s2);
        assertEquals(0, dataset.indexOf(s1));
        assertEquals(1, dataset.indexOf(s2));

        dataset.removeSeries(s1);
        assertEquals(-1, dataset.indexOf(s1));
        assertEquals(0, dataset.indexOf(s2));

        TimeSeries s2b = new TimeSeries("S2");
        assertEquals(0, dataset.indexOf(s2b));
    }

    private static final double EPSILON = 0.0000000001;

    /**
     * This method provides a check for the bounds calculated using the
     * {@link DatasetUtilities#findDomainBounds(org.jfree.data.xy.XYDataset,
     * java.util.List, boolean)} method.
     */
    @Test
    public void testFindDomainBounds() {
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        List<String> visibleSeriesKeys = new java.util.ArrayList<String>();
        Range r = DatasetUtilities.findDomainBounds(dataset, visibleSeriesKeys,
                true);
        assertNull(r);

        TimeSeries s1 = new TimeSeries("S1");
        dataset.addSeries(s1);
        visibleSeriesKeys.add("S1");
        r = DatasetUtilities.findDomainBounds(dataset, visibleSeriesKeys, true);
        assertNull(r);

        // store the current time zone
        TimeZone saved = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Paris"));

        s1.add(new Year(2008), 8.0);
        r = DatasetUtilities.findDomainBounds(dataset, visibleSeriesKeys, true);
        assertEquals(1199145600000.0, r.getLowerBound(), EPSILON);
        assertEquals(1230767999999.0, r.getUpperBound(), EPSILON);

        TimeSeries s2 = new TimeSeries("S2");
        dataset.addSeries(s2);
        s2.add(new Year(2009), 9.0);
        s2.add(new Year(2010), 10.0);
        r = DatasetUtilities.findDomainBounds(dataset, visibleSeriesKeys, true);
        assertEquals(1199145600000.0, r.getLowerBound(), EPSILON);
        assertEquals(1230767999999.0, r.getUpperBound(), EPSILON);

        visibleSeriesKeys.add("S2");
        r = DatasetUtilities.findDomainBounds(dataset, visibleSeriesKeys, true);
        assertEquals(1199145600000.0, r.getLowerBound(), EPSILON);
        assertEquals(1293839999999.0, r.getUpperBound(), EPSILON);

        // restore the default time zone
        TimeZone.setDefault(saved);
    }

    /**
     * Basic checks for cloning.
     */
    @Test
    public void testCloning() throws CloneNotSupportedException {
        TimeSeries s1 = new TimeSeries("Series");
        s1.add(new Year(2009), 1.1);
        TimeSeriesCollection c1 = new TimeSeriesCollection();
        c1.addSeries(s1);
        TimeSeriesCollection c2 = (TimeSeriesCollection) c1.clone();
        assertNotSame(c1, c2);
        assertSame(c1.getClass(), c2.getClass());
        assertEquals(c1, c2);

        // check independence
        s1.setDescription("XYZ");
        assertFalse(c1.equals(c2));
        c2.getSeries(0).setDescription("XYZ");
        assertEquals(c1, c2);
    }

    /**
     * A test to cover bug 3445507.
     */
    @Test
    public void testBug3445507() {
        TimeSeries s1 = new TimeSeries("S1");
        s1.add(new Year(2011), null);
        s1.add(new Year(2012), null);

        TimeSeries s2 = new TimeSeries("S2");
        s2.add(new Year(2011), 5.0);
        s2.add(new Year(2012), 6.0);

        TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(s1);
        dataset.addSeries(s2);

        List<String> keys = new ArrayList<String>();
        keys.add("S1");
        keys.add("S2");
        Range r = dataset.getRangeBounds(keys, new Range(
                Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY), false);
        assertEquals(5.0, r.getLowerBound(), EPSILON);
        assertEquals(6.0, r.getUpperBound(), EPSILON);
    }

    /**
     * Some checks for the getRangeBounds() method.
     */
    @Test
    public void testGetRangeBounds() {
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        Range r = dataset.getRangeBounds(false);
        assertNull(r);

        TimeSeries s1 = new TimeSeries("S1");
        dataset.addSeries(s1);
        r = dataset.getRangeBounds(false);
        assertTrue(Double.isNaN(r.getLowerBound()));
        assertTrue(Double.isNaN(r.getUpperBound()));

        s1.add(new Year(2012), 1.0);
        r = dataset.getRangeBounds(false);
        assertEquals(1.0, r.getLowerBound(), EPSILON);
        assertEquals(1.0, r.getUpperBound(), EPSILON);

        TimeSeries s2 = new TimeSeries("S2");
        dataset.addSeries(s2);
        r = dataset.getRangeBounds(false);
        assertEquals(1.0, r.getLowerBound(), EPSILON);
        assertEquals(1.0, r.getUpperBound(), EPSILON);
    }

}
