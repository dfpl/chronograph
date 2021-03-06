package org.dfpl.chronograph.crud.memory.chronoedge;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.dfpl.chronograph.common.TemporalRelation;
import org.dfpl.chronograph.crud.memory.ChronoGraph;
import org.dfpl.chronograph.crud.memory.ChronoEdgeEvent;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Time;
import com.tinkerpop.blueprints.TimeInstant;
import com.tinkerpop.blueprints.TimePeriod;
import com.tinkerpop.blueprints.Vertex;

public class EventTest {
    Graph g = new ChronoGraph();
    TemporalRelation tr;
    Vertex a;
    Vertex b;
    Edge ab;
    Time time;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        g = new ChronoGraph();
        a = g.addVertex("A");
        b = g.addVertex("B");

        ab = g.addEdge(a, b, "likes");
    }

    @After
    public void tearDown() throws Exception {
        g.removeEdge(ab);
        g.removeVertex(a);
        g.removeVertex(b);
    }

    @Test
    public void testCreateForUnallowedEvents() {
        time = new TimePeriod(5, 8);

        ab.addEvent(time);

        assertNull(ab.addEvent(new TimeInstant((5))));
        assertNull(ab.addEvent(new TimeInstant((6))));
        assertNull(ab.addEvent(new TimeInstant((8))));

        assertNull(ab.addEvent(new TimePeriod(5, 8)));
        assertNull(ab.addEvent(new TimePeriod(5, 6)));
        assertNull(ab.addEvent(new TimePeriod(6, 7)));
        assertNull(ab.addEvent(new TimePeriod(7, 8)));
    }

    @Test
    public void testCreateForMerge() {
        time = new TimePeriod(5, 9);

        ab.addEvent(new TimeInstant((5)));
        ab.addEvent(new TimeInstant((6)));

        assertNotNull(ab.addEvent(time));
        assertNull(ab.getEvent(new TimeInstant((5)), TemporalRelation.cotemporal));
        assertNull(ab.getEvent(new TimeInstant((6)), TemporalRelation.cotemporal));
    }

    @Test
    public void testCreateForExtend() {
        time = new TimePeriod(5, 9);
        ab.addEvent(time);

        // Overlap
        ab.addEvent(new TimePeriod(8, 10));
        assertNull(ab.getEvent(time, TemporalRelation.cotemporal));
        assertNotNull(ab.getEvent(new TimePeriod(5, 10), TemporalRelation.cotemporal));

        // Meet
        time = new TimePeriod(10, 11);
        ab.addEvent(time);
        assertNull(ab.getEvent(time, TemporalRelation.cotemporal));
        assertNotNull(ab.getEvent(new TimePeriod(5, 11), TemporalRelation.cotemporal));

        // Overlap
        time = new TimePeriod(4, 6);
        ab.addEvent(time);
        assertNull(ab.getEvent(time, TemporalRelation.cotemporal));
        assertNotNull(ab.getEvent(new TimePeriod(4, 11), TemporalRelation.cotemporal));

        time = new TimePeriod(3, 4);
        ab.addEvent(time);
        assertNull(ab.getEvent(time, TemporalRelation.cotemporal));
        assertNotNull(ab.getEvent(new TimePeriod(3, 11), TemporalRelation.cotemporal));
    }

    @Test
    public void testCreateAndDeleteForCotemporalGivenTimeInstants() {
        tr = TemporalRelation.cotemporal;

        // No event exists
        time = new TimeInstant(5);
        assertNull(ab.getEvent(time, tr));
        assertEquals(0, ab.getEvents(time, tr).size());

        // One cotemporal event exists
        ChronoEdgeEvent cotemporalEvent = ab.addEvent(time);
        assertEquals(cotemporalEvent, ab.getEvent(time, tr));
        assertEquals(1, ab.getEvents(time, tr).size());

        // Remove event
        ab.removeEvents(time, tr);
        assertNull(ab.getEvent(time, tr));
        assertEquals(0, ab.getEvents(time, tr).size());
    }

    @Test
    public void testCreateAndDeleteForContainsGivenTimeInstants() {
        tr = TemporalRelation.contains;
        time = new TimeInstant(5);

        // One cotemporal event exists
        ab.addEvent(time);
        assertNull(ab.getEvent(time, tr));
        assertEquals(0, ab.getEvents(time, tr).size());

        // No event is removed
        ab.removeEvents(time, tr);
        assertNull(ab.getEvent(time, tr));
    }

    @Test
    public void testCreateAndDeleteForFinishesGivenTimeInstants() {
        tr = TemporalRelation.finishes;
        time = new TimeInstant(5);

        // One cotemporal event exists
        ab.addEvent(time);
        assertNull(ab.getEvent(time, tr));
        assertEquals(0, ab.getEvents(time, tr).size());

        // No event is removed
        ab.removeEvents(time, tr);
        assertNull(ab.getEvent(time, tr));
    }

    @Test
    public void testCreateAndDeleteForIsFinishedByGivenTimeInstants() {
        tr = TemporalRelation.isFinishedBy;
        time = new TimeInstant(5);

        // One cotemporal event exists
        ab.addEvent(time);
        assertNull(ab.getEvent(time, tr));
        assertEquals(0, ab.getEvents(time, tr).size());

        // No event is removed
        ab.removeEvents(time, tr);
        assertNull(ab.getEvent(time, tr));
    }

    @Test
    public void testCreateAndDeleteForIsBeforeGivenTimePeriod() {
        tr = TemporalRelation.isBefore;
        time = new TimePeriod(9, 10);

        // No event exists
        assertNull(ab.getEvent(time, tr));
        assertEquals(0, ab.getEvents(time, tr).size());

        // Two valid events exist
        TimePeriod validTime1 = new TimePeriod(1, 2);
        ChronoEdgeEvent validEvent1 = ab.addEvent(validTime1);

        TimePeriod validTime2 = new TimePeriod(3, 4);
        ChronoEdgeEvent validEvent2 = ab.addEvent(validTime2);

        List<ChronoEdgeEvent> validEvents = new LinkedList<>(List.of(validEvent1, validEvent2));
        assertEquals(validEvent1, ab.getEvent(time, tr));
        assertEquals(2, ab.getEvents(time, tr).size());
        assertTrue(ab.getEvents(time, tr).containsAll(validEvents));

        // Remove two valid events
        ab.removeEvents(time, tr);
        assertNull(ab.getEvent(time, tr));
    }

    @Test
    public void testCreateAndDeleteForIsAfterGivenTimeInstants() {
        tr = TemporalRelation.isAfter;
        time = new TimeInstant(5);

        // No event after time 5 exists
        assertNull(ab.getEvent(time, tr));

        // Two events after time 5 exists
        Time afterTime1 = new TimeInstant(7);
        ChronoEdgeEvent afterEvent1 = ab.addEvent(afterTime1);

        Time afterTime2 = new TimeInstant(9);
        ChronoEdgeEvent afterEvent2 = ab.addEvent(afterTime2);

        assertEquals(afterEvent1, ab.getEvent(time, tr));
        assertEquals(2, ab.getEvents(time, tr).size());
        List<ChronoEdgeEvent> expectedEvents = new LinkedList<>(List.of(afterEvent1, afterEvent2));
        assertTrue(ab.getEvents(time, tr).containsAll(expectedEvents));
        assertEquals(2, ab.getEvents(time, tr).size());

        // Remove events after time 5
        ab.removeEvents(time, tr);
        assertEquals(0, ab.getEvents(time, tr).size());
    }

    @Test
    public void testCreateAndDeleteForIsAfterGivenTimePeriod() {
        tr = TemporalRelation.isAfter;
        time = new TimePeriod(9, 10);

        // Only one event exists
        ab.addEvent(time);
        assertNull(ab.getEvent(time, tr));
        assertEquals(0, ab.getEvents(time, tr).size());

        // Two valid events exist
        TimePeriod validTime1 = new TimePeriod(11, 12);
        ChronoEdgeEvent validEvent1 = ab.addEvent(validTime1);

        TimePeriod validTime2 = new TimePeriod(13, 14);
        ChronoEdgeEvent validEvent2 = ab.addEvent(validTime2);

        List<ChronoEdgeEvent> validEvents = new LinkedList<>(List.of(validEvent1, validEvent2));
        assertEquals(validEvent1, ab.getEvent(time, tr));
        assertEquals(2, ab.getEvents(time, tr).size());
        assertTrue(ab.getEvents(time, tr).containsAll(validEvents));

        // Remove two valid events
        ab.removeEvents(time, tr);
        assertNull(ab.getEvent(time, tr));
    }

    @Test
    public void testCreateAndDeleteForMeetsGivenTimePeriod() {
        tr = TemporalRelation.meets;
        time = new TimePeriod(15, 16);

        // No event exists
        assertNull(ab.getEvent(time, tr));
        assertEquals(0, ab.getEvents(time, tr).size());

        // One meets event exists
        TimePeriod validTime1 = new TimePeriod(9, 15);
        ChronoEdgeEvent validEvent1 = ab.addEvent(validTime1);

        List<ChronoEdgeEvent> validEvents = new LinkedList<>(List.of(validEvent1));
        assertEquals(validEvent1, ab.getEvent(time, tr));
        assertEquals(1, ab.getEvents(time, tr).size());
        assertTrue(ab.getEvents(time, tr).containsAll(validEvents));

        // Remove valid events
        ab.removeEvents(time, tr);
        assertNull(ab.getEvent(time, tr));
    }

    @Test
    public void testCreateAndDeleteForMeetsGivenTimeInstants() {
        tr = TemporalRelation.meets;
        time = new TimeInstant(5);

        // One cotemporal event exists
        ab.addEvent(time);
        assertNull(ab.getEvent(time, tr));
        assertEquals(0, ab.getEvents(time, tr).size());

        // No event is removed
        ab.removeEvents(time, tr);
        assertNull(ab.getEvent(time, tr));
    }

    @Test
    public void testCreateAndDeleteForIsMetByGivenTimeInstants() {
        tr = TemporalRelation.isMetBy;
        time = new TimeInstant(5);

        // One cotemporal event exists
        ab.addEvent(time);
        assertNull(ab.getEvent(time, tr));
        assertEquals(0, ab.getEvents(time, tr).size());

        // No event is removed
        ab.removeEvents(time, tr);
        assertNull(ab.getEvent(time, tr));
    }

    @Test
    public void testCreateAndDeleteForIsMetByGivenTimePeriod() {
        tr = TemporalRelation.isMetBy;
        time = new TimePeriod(9, 15);

        // No event exists
        assertNull(ab.getEvent(time, tr));
        assertEquals(0, ab.getEvents(time, tr).size());

        // One valid event exists
        TimePeriod validTime1 = new TimePeriod(15, 16);
        ChronoEdgeEvent validEvent1 = ab.addEvent(validTime1);

        List<ChronoEdgeEvent> validEvents = new LinkedList<>(List.of(validEvent1));
        assertEquals(validEvent1, ab.getEvent(time, tr));
        assertEquals(1, ab.getEvents(time, tr).size());
        assertTrue(ab.getEvents(time, tr).containsAll(validEvents));

        // Remove valid event
        ab.removeEvents(time, tr);
        assertNull(ab.getEvent(time, tr));
    }

    @Test
    public void testCreateAndDeleteForOverlapsWithGivenTimeInstants() {
        tr = TemporalRelation.overlapsWith;
        time = new TimeInstant(5);

        // One cotemporal event exists
        ab.addEvent(time);
        assertNull(ab.getEvent(time, tr));
        assertEquals(0, ab.getEvents(time, tr).size());

        // No event is removed
        ab.removeEvents(time, tr);
        assertNull(ab.getEvent(time, tr));
    }

    @Test
    public void testCreateAndDeleteForOverlapsWithGivenTimePeriod() {
        tr = TemporalRelation.overlapsWith;
        time = new TimePeriod(9, 15);

        // No event exists
        assertNull(ab.getEvent(time, tr));
        assertEquals(0, ab.getEvents(time, tr).size());

        // One valid event exists
        TimePeriod validTime1 = new TimePeriod(8, 10);
        ChronoEdgeEvent validEvent1 = ab.addEvent(validTime1);

        List<ChronoEdgeEvent> validEvents = new LinkedList<>(List.of(validEvent1));
        assertEquals(validEvent1, ab.getEvent(time, tr));
        assertEquals(1, ab.getEvents(time, tr).size());
        assertTrue(ab.getEvents(time, tr).containsAll(validEvents));

        // Remove valid event
        ab.removeEvents(time, tr);
        assertNull(ab.getEvent(time, tr));
    }

    @Test
    public void testCreateAndDeleteForIsOverlappedByGivenTimeInstants() {
        tr = TemporalRelation.isOverlappedBy;
        time = new TimeInstant(5);

        // One cotemporal event exists
        ab.addEvent(time);
        assertNull(ab.getEvent(time, tr));
        assertEquals(0, ab.getEvents(time, tr).size());

        // No event is removed
        ab.removeEvents(time, tr);
        assertNull(ab.getEvent(time, tr));
    }

    @Test
    public void testCreateAndDeleteForIsOvelappedByGivenTimePeriod() {
        tr = TemporalRelation.isOverlappedBy;
        time = new TimePeriod(8, 10);

        // No event exists
        assertNull(ab.getEvent(time, tr));
        assertEquals(0, ab.getEvents(time, tr).size());

        // One valid event exists
        TimePeriod validTime1 = new TimePeriod(9, 14);
        ChronoEdgeEvent validEvent1 = ab.addEvent(validTime1);

        List<ChronoEdgeEvent> validEvents = new LinkedList<>(List.of(validEvent1));
        assertEquals(validEvent1, ab.getEvent(time, tr));
        assertEquals(1, ab.getEvents(time, tr).size());
        assertTrue(ab.getEvents(time, tr).containsAll(validEvents));

        // Remove valid events
        ab.removeEvents(time, tr);
        assertNull(ab.getEvent(time, tr));
    }

    @Test
    public void testCreateAndDeleteForStartsGivenTimeInstants() {
        tr = TemporalRelation.starts;
        time = new TimeInstant(5);

        // One cotemporal event exists
        ab.addEvent(time);
        assertNull(ab.getEvent(time, tr));
        assertEquals(0, ab.getEvents(time, tr).size());

        // No event is removed
        ab.removeEvents(time, tr);
        assertNull(ab.getEvent(time, tr));
    }

    @Test
    public void testCreateAndDeleteForStartsGivenTimePeriod() {
        tr = TemporalRelation.starts;
        time = new TimePeriod(8, 15);

        // No event exists
        assertNull(ab.getEvent(time, tr));
        assertEquals(0, ab.getEvents(time, tr).size());

        // One valid event exists
        TimePeriod validTime1 = new TimePeriod(8, 10);
        ChronoEdgeEvent validEvent1 = ab.addEvent(validTime1);

        List<ChronoEdgeEvent> validEvents = new LinkedList<>(List.of(validEvent1));
        assertEquals(validEvent1, ab.getEvent(time, tr));
        assertEquals(1, ab.getEvents(time, tr).size());
        assertTrue(ab.getEvents(time, tr).containsAll(validEvents));

        // Remove valid events
        ab.removeEvents(time, tr);
        assertNull(ab.getEvent(time, tr));
    }

    @Test
    public void testCreateAndDeleteForIsStartedByGivenTimePeriod() {
        tr = TemporalRelation.isStartedBy;
        time = new TimePeriod(8, 10);

        // No event exists
        assertNull(ab.getEvent(time, tr));
        assertEquals(0, ab.getEvents(time, tr).size());

        // One valid event exists
        TimePeriod validTime1 = new TimePeriod(8, 15);
        ChronoEdgeEvent validEvent1 = ab.addEvent(validTime1);

        List<ChronoEdgeEvent> validEvents = new LinkedList<>(List.of(validEvent1));
        assertEquals(validEvent1, ab.getEvent(time, tr));
        assertEquals(1, ab.getEvents(time, tr).size());
        assertTrue(ab.getEvents(time, tr).containsAll(validEvents));

        // Remove valid events
        ab.removeEvents(time, tr);
        assertNull(ab.getEvent(time, tr));
    }

    @Test
    public void testCreateAndDeleteForIsStartedByGivenTimeInstants() {
        tr = TemporalRelation.isStartedBy;
        time = new TimeInstant(5);

        // One cotemporal event exists
        ab.addEvent(time);
        assertNull(ab.getEvent(time, tr));
        assertEquals(0, ab.getEvents(time, tr).size());

        // No event is removed
        ab.removeEvents(time, tr);
        assertNull(ab.getEvent(time, tr));
    }

    @Test
    public void testCreateAndDeleteForDuringGivenTimeInstants() {
        tr = TemporalRelation.during;
        time = new TimeInstant(5);

        // One cotemporal event exists
        ab.addEvent(time);
        assertNull(ab.getEvent(time, tr));
        assertEquals(0, a.getEvents(time, tr).size());

        // No event is removed
        ab.removeEvents(time, tr);
        assertNull(ab.getEvent(time, tr));
    }

    @Test
    public void testCreateAndDeleteForDuringGivenTimePeriod() {
        tr = TemporalRelation.during;
        time = new TimePeriod(5, 10);

        // No event exists
        assertNull(ab.getEvent(time, tr));
        assertEquals(0, ab.getEvents(time, tr).size());

        // Two valid event exists
        TimePeriod validTime1 = new TimePeriod(6, 9);
        ChronoEdgeEvent validEvent1 = ab.addEvent(validTime1);

        List<ChronoEdgeEvent> validEvents = new LinkedList<>(List.of(validEvent1));
        assertEquals(validEvent1, ab.getEvent(time, tr));
        assertEquals(1, ab.getEvents(time, tr).size());
        assertTrue(ab.getEvents(time, tr).containsAll(validEvents));

        // Remove valid events
        ab.removeEvents(time, tr);
        assertNull(ab.getEvent(time, tr));
    }

    @Test
    public void testCreateAndDeleteForContainsGivenTimePeriod() {
        tr = TemporalRelation.contains;
        time = new TimePeriod(4, 6);

        // No event exists
        assertNull(ab.getEvent(time, tr));
        assertEquals(0, ab.getEvents(time, tr).size());

        // Two valid event exists
        TimePeriod validTime1 = new TimePeriod(3, 7);
        ChronoEdgeEvent validEvent1 = ab.addEvent(validTime1);

        List<ChronoEdgeEvent> validEvents = new LinkedList<>(List.of(validEvent1));
        assertEquals(validEvent1, ab.getEvent(time, tr));
        assertEquals(1, ab.getEvents(time, tr).size());
        assertTrue(ab.getEvents(time, tr).containsAll(validEvents));

        // Remove valid events
        ab.removeEvents(time, tr);
        assertNull(ab.getEvent(time, tr));
    }

    @Test
    public void testCreateAndDeleteForFinishesGivenTimePeriod() {
        tr = TemporalRelation.finishes;
        time = new TimePeriod(4, 6);

        // No event exists
        assertNull(ab.getEvent(time, tr));
        assertEquals(0, ab.getEvents(time, tr).size());

        // One valid event exists
        TimePeriod validTime1 = new TimePeriod(5, 6);
        ChronoEdgeEvent validEvent1 = ab.addEvent(validTime1);

        List<ChronoEdgeEvent> validEvents = new LinkedList<>(List.of(validEvent1));
        assertEquals(validEvent1, ab.getEvent(time, tr));
        assertEquals(1, ab.getEvents(time, tr).size());
        assertTrue(ab.getEvents(time, tr).containsAll(validEvents));

        // Remove valid events
        ab.removeEvents(time, tr);
        assertNull(ab.getEvent(time, tr));
    }

    @Test
    public void testCreateAndDeleteForIsFinishedByGivenTimePeriod() {
        tr = TemporalRelation.isFinishedBy;
        time = new TimePeriod(5, 6);

        // No event exists
        assertNull(ab.getEvent(time, tr));
        assertEquals(0, ab.getEvents(time, tr).size());

        // One valid event exists
        TimePeriod validTime1 = new TimePeriod(4, 6);
        ChronoEdgeEvent validEvent1 = ab.addEvent(validTime1);

        List<ChronoEdgeEvent> validEvents = new LinkedList<>(List.of(validEvent1));
        assertEquals(validEvent1, ab.getEvent(time, tr));
        assertEquals(1, ab.getEvents(time, tr).size());
        assertTrue(ab.getEvents(time, tr).containsAll(validEvents));

        // Remove valid events
        ab.removeEvents(time, tr);
        assertNull(ab.getEvent(time, tr));
    }

    @Test
    public void testCreateAndDeleteForCotemporalGivenTimePeriod() {
        tr = TemporalRelation.cotemporal;
        time = new TimePeriod(5, 6);

        // No event exists
        assertNull(ab.getEvent(time, tr));
        assertEquals(0, ab.getEvents(time, tr).size());

        // One valid event exists
        TimePeriod validTime1 = new TimePeriod(5, 6);
        ChronoEdgeEvent validEvent1 = ab.addEvent(validTime1);

        List<ChronoEdgeEvent> validEvents = new LinkedList<>(List.of(validEvent1));
        assertEquals(validEvent1, ab.getEvent(time, tr));
        assertEquals(1, ab.getEvents(time, tr).size());
        assertTrue(ab.getEvents(time, tr).containsAll(validEvents));

        // Remove valid events
        ab.removeEvents(time, tr);
        assertNull(ab.getEvent(time, tr));
    }

    @Test
    public void testSetOrderByStart() {
        Time time5 = new TimeInstant(5);
        ChronoEdgeEvent event5 = ab.addEvent(time5);

        Time time7 = new TimeInstant(7);
        ChronoEdgeEvent event7 = ab.addEvent(time7);

        Time time9 = new TimeInstant(9);
        ChronoEdgeEvent event9 = ab.addEvent(time9);

        // Default
        assertEquals(event5, ab.getEvent(time7, TemporalRelation.isBefore));
        assertEquals(event7, ab.getEvent(time5, TemporalRelation.isAfter));

        // Set orderByStart to true when it is already true
        ab.setOrderByStart(true);
        assertEquals(event7, ab.getEvent(time5, TemporalRelation.isAfter));

        // Set orderByStart from true to false
        ab.setOrderByStart(false);
        assertEquals(event9, ab.getEvent(time5, TemporalRelation.isAfter));

        // Set orderByStart to false when it's already false
        ab.setOrderByStart(false);
        assertEquals(event9, ab.getEvent(time5, TemporalRelation.isAfter));

        // Set orderByStart from false to true
        ab.setOrderByStart(true);
        assertEquals(event7, ab.getEvent(time5, TemporalRelation.isAfter));
    }
}
