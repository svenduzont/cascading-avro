/*
 * Copyright (c) 2012 MaxPoint Interactive, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.maxpoint.cascading.avro;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.avro.Schema;
import org.apache.avro.util.Utf8;
import org.apache.hadoop.io.BytesWritable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import cascading.flow.local.LocalFlowProcess;
import cascading.tap.local.FileTap;
import cascading.tuple.Fields;
import cascading.tuple.Tuple;
import cascading.tuple.TupleEntry;
import cascading.tuple.TupleEntryCollector;
import cascading.tuple.TupleEntryIterator;

/**
 * Class LocalAvroSchemeTest
 */
public class LocalAvroSchemeTest {
    @Rule
    public final TemporaryFolder tempDir = new TemporaryFolder();

    @Test
    public void testRoundTrip() throws Exception {
        @SuppressWarnings("deprecation")
        final Schema schema = Schema.parse(getClass().getResourceAsStream(
                "test1.avsc"));
        final LocalAvroScheme scheme = new LocalAvroScheme(schema);

        final Fields fields = new Fields("aBoolean", "anInt", "aLong",
                "aDouble", "aFloat", "aBytes", "aFixed", "aNull", "aString",
                "aList", "aMap");

        final File root = tempDir.newFile("dummy");
        final FileTap tap = new FileTap(scheme, root.getAbsolutePath());
        LocalFlowProcess writeProcess = new LocalFlowProcess(new Properties());
        final TupleEntryCollector collector = tap.openForWrite(writeProcess);
        List<String> aList = new ArrayList<String>();
        Map<String, Integer> aMap = new HashMap<String, Integer>();
        aMap.put("one", 1);
        aMap.put("two", 2);

        aList.add("0");
        aList.add("1");
        write(scheme, collector, new TupleEntry(fields, new Tuple(false, 1, 2L,
                3.0, 4.0F, new BytesWritable(new byte[] { 1, 2, 3 }),
                new BytesWritable(new byte[16]), null, "test-string", aList,
                aMap)));
        write(scheme, collector, new TupleEntry(fields, new Tuple(false, 1, 2L,
                3.0, 4.0F, new BytesWritable(new byte[0]), new BytesWritable(
                        new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4,
                                5, 6 }), null, null, aList, aMap)));
        collector.close();

        LocalFlowProcess readProcess = new LocalFlowProcess(new Properties());
        final TupleEntryIterator iterator = tap.openForRead(readProcess);
        assertTrue(iterator.hasNext());
        final TupleEntry readEntry1 = iterator.next();

        assertEquals(false, readEntry1.getBoolean("aBoolean"));
        assertEquals(1, readEntry1.getInteger("anInt"));
        assertEquals(2L, readEntry1.getLong("aLong"));
        assertEquals(3.0, readEntry1.getDouble("aDouble"), 0.01);
        assertEquals(4.0F, readEntry1.getFloat("aFloat"), 0.01);
        assertEquals("test-string", readEntry1.get("aString"));
        assertEquals(new BytesWritable(new byte[] { 1, 2, 3 }),
                readEntry1.getObject("aBytes"));
        assertEquals(new BytesWritable(new byte[16]),
                readEntry1.getObject("aFixed"));
        assertEquals("0", ((List) readEntry1.getObject("aList")).get(0)
                .toString());
        assertEquals(1,
                ((Map) readEntry1.getObject("aMap")).get(new Utf8("one")));
        assertTrue(iterator.hasNext());
        final TupleEntry readEntry2 = iterator.next();

        assertNull(readEntry2.get("aString"));
    }

    @Test
    public void testRoundTrip2() throws Exception {
        @SuppressWarnings("deprecation")
        final Schema schema = Schema.parse(getClass().getResourceAsStream(
                "test1.avsc"));
        LocalAvroScheme scheme = new LocalAvroScheme(schema);

        final Fields fields = new Fields("aBoolean", "anInt", "aLong",
                "aDouble", "aFloat", "aBytes", "aFixed", "aNull", "aString",
                "aList", "aMap");

        final File root = tempDir.newFile("dummy");
        FileTap tap = new FileTap(scheme, root.getAbsolutePath());
        LocalFlowProcess writeProcess = new LocalFlowProcess(new Properties());
        final TupleEntryCollector collector = tap.openForWrite(writeProcess);
        List<String> aList = new ArrayList<String>();
        Map<String, Integer> aMap = new HashMap<String, Integer>();
        aMap.put("one", 1);
        aMap.put("two", 2);

        aList.add("0");
        aList.add("1");
        write(scheme, collector, new TupleEntry(fields, new Tuple(false, 1, 2L,
                3.0, 4.0F, new BytesWritable(new byte[] { 1, 2, 3 }),
                new BytesWritable(new byte[16]), null, "test-string", aList,
                aMap)));
        write(scheme, collector, new TupleEntry(fields, new Tuple(false, 1, 2L,
                3.0, 4.0F, new BytesWritable(new byte[0]), new BytesWritable(
                        new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4,
                                5, 6 }), null, null, aList, aMap)));
        collector.close();

        LocalFlowProcess readProcess = new LocalFlowProcess(new Properties());
        scheme = new LocalAvroScheme(schema);
        tap = new FileTap(scheme, root.getAbsolutePath());
        final TupleEntryIterator iterator = tap.openForRead(readProcess);
        assertTrue(iterator.hasNext());
        final TupleEntry readEntry1 = iterator.next();

        assertEquals(false, readEntry1.getBoolean("aBoolean"));
        assertEquals(1, readEntry1.getInteger("anInt"));
        assertEquals(2L, readEntry1.getLong("aLong"));
        assertEquals(3.0, readEntry1.getDouble("aDouble"), 0.01);
        assertEquals(4.0F, readEntry1.getFloat("aFloat"), 0.01);
        assertEquals("test-string", readEntry1.get("aString"));
        assertEquals(new BytesWritable(new byte[] { 1, 2, 3 }),
                readEntry1.getObject("aBytes"));
        assertEquals(new BytesWritable(new byte[16]),
                readEntry1.getObject("aFixed"));
        assertEquals("0", ((List) readEntry1.getObject("aList")).get(0)
                .toString());
        assertEquals(1,
                ((Map) readEntry1.getObject("aMap")).get(new Utf8("one")));
        assertTrue(iterator.hasNext());
        final TupleEntry readEntry2 = iterator.next();

        assertNull(readEntry2.get("aString"));
    }

    @Test
    public void listOrMapInsideListTest() throws Exception {
        final Schema schema = Schema.parse(getClass().getResourceAsStream(
                "test4.avsc"));
        final LocalAvroScheme scheme = new LocalAvroScheme(schema);

        final Fields fields = new Fields("aListOfListOfInt", "aListOfMapToLong");

        final FileTap FileTap = new FileTap(scheme, tempDir.newFile("dummy")
                .toString());
        LocalFlowProcess writeProcess = new LocalFlowProcess(new Properties());
        final TupleEntryCollector collector = FileTap
                .openForWrite(writeProcess);

        List<Map<String, Long>> aListOfMapToLong = new ArrayList<Map<String, Long>>();
        Map<String, Long> aMapToLong = new HashMap<String, Long>();
        aMapToLong.put("one", 1L);
        aMapToLong.put("two", 2L);
        aListOfMapToLong.add(aMapToLong);

        List<List<Integer>> aListOfListOfInt = new ArrayList<List<Integer>>();
        List<Integer> aListOfInt = new LinkedList<Integer>();
        aListOfInt.add(0);
        aListOfInt.add(1);
        aListOfListOfInt.add(aListOfInt);

        write(scheme, collector, new TupleEntry(fields, new Tuple(
                aListOfListOfInt, aListOfMapToLong)));
        collector.close();

        LocalFlowProcess readProcess = new LocalFlowProcess(new Properties());
        final TupleEntryIterator iterator = FileTap.openForRead(readProcess);
        assertTrue(iterator.hasNext());
        final TupleEntry readEntry1 = iterator.next();

        List<Integer> outListOfInt = (List) ((List) readEntry1
                .getObject("aListOfListOfInt")).get(0);
        Map<Utf8, Long> outMapToLong = (Map) ((List) readEntry1
                .getObject("aListOfMapToLong")).get(0);

        assertEquals(Integer.valueOf(0), outListOfInt.get(0));
        assertEquals(Integer.valueOf(1), outListOfInt.get(1));
        assertEquals(Long.valueOf(1L), outMapToLong.get(new Utf8("one")));
        assertEquals(Long.valueOf(2L), outMapToLong.get(new Utf8("two")));
        assertTrue(!iterator.hasNext());

    }

    @Test
    public void listOrMapInsideMapTest() throws Exception {
        final Schema schema = Schema.parse(getClass().getResourceAsStream(
                "test3.avsc"));
        final LocalAvroScheme scheme = new LocalAvroScheme(schema);

        final Fields fields = new Fields("aMapToListOfInt", "aMapToMapToLong");

        final FileTap FileTap = new FileTap(scheme, tempDir.newFile("dummy")
                .toString());
        LocalFlowProcess writeProcess = new LocalFlowProcess(new Properties());
        final TupleEntryCollector collector = FileTap
                .openForWrite(writeProcess);

        Map<String, Map<String, Long>> aMapToMapToLong = new HashMap<String, Map<String, Long>>();
        Map<String, Long> aMapToLong = new HashMap<String, Long>();
        aMapToLong.put("one", 1L);
        aMapToLong.put("two", 2L);
        aMapToMapToLong.put("key", aMapToLong);

        Map<String, List<Integer>> aMapToListOfInt = new HashMap<String, List<Integer>>();
        List<Integer> aListOfInt = new LinkedList<Integer>();
        aListOfInt.add(0);
        aListOfInt.add(1);
        aMapToListOfInt.put("key", aListOfInt);

        write(scheme, collector, new TupleEntry(fields, new Tuple(
                aMapToListOfInt, aMapToMapToLong)));
        collector.close();

        LocalFlowProcess readProcess = new LocalFlowProcess(new Properties());
        final TupleEntryIterator iterator = FileTap.openForRead(readProcess);
        assertTrue(iterator.hasNext());
        final TupleEntry readEntry1 = iterator.next();

        List<Integer> outListOfInt = (List) ((Map) readEntry1
                .getObject("aMapToListOfInt")).get(new Utf8("key"));
        Map<Utf8, Long> outMapToLong = (Map) ((Map) readEntry1
                .getObject("aMapToMapToLong")).get(new Utf8("key"));

        assertEquals(Integer.valueOf(0), outListOfInt.get(0));
        assertEquals(Integer.valueOf(1), outListOfInt.get(1));
        assertEquals(Long.valueOf(1L), outMapToLong.get(new Utf8("one")));
        assertEquals(Long.valueOf(2L), outMapToLong.get(new Utf8("two")));
        assertTrue(!iterator.hasNext());

    }

    private void write(LocalAvroScheme scheme, TupleEntryCollector collector,
            TupleEntry te) {
        collector.add(te.selectTuple(scheme.getSinkFields()));
    }

    @Test
    public void testSerialization() throws Exception {
        final Schema schema = Schema.parse(getClass().getResourceAsStream(
                "test1.avsc"));
        final LocalAvroScheme expected = new LocalAvroScheme(schema);

        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bytes);
        oos.writeObject(expected);
        oos.close();

        final ObjectInputStream iis = new ObjectInputStream(
                new ByteArrayInputStream(bytes.toByteArray()));
        final LocalAvroScheme actual = (LocalAvroScheme) iis.readObject();

        assertEquals(expected, actual);
    }
}
