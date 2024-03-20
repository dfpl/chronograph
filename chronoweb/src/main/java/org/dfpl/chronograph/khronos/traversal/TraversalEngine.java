package org.dfpl.chronograph.khronos.traversal;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.bson.BsonArray;
import org.bson.BsonDateTime;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.Document;
import org.dfpl.chronograph.common.EdgeEvent;
import org.dfpl.chronograph.common.Event;
import org.dfpl.chronograph.common.LoopFunction;
import org.dfpl.chronograph.common.RetainEventBinaryOperator;
import org.dfpl.chronograph.common.Step;
import org.dfpl.chronograph.common.TemporalRelation;
import org.dfpl.chronograph.common.VertexEvent;
import org.dfpl.chronograph.khronos.manipulation.memory.MChronoGraph;
import org.dfpl.chronograph.khronos.manipulation.persistent.PChronoGraph;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.GremlinFluentPipeline;
import com.tinkerpop.gremlin.GremlinPipeline;
import com.tinkerpop.gremlin.LoopBundle;

/**
 * Copyright (C) 2020 Jaewook Byun
 *
 * This project is a temporal graph traversal platform, ChronoGraph, which
 * especially enables efficient information diffusion analysis over time.
 * 
 * @author Jaewook Byun, Ph.D., Assistant Professor, Sejong University,
 *         jwbyun@sejong.ac.kr
 * 
 *         Associate Director, Auto-ID Labs, KAIST, bjw0829@kaist.ac.kr
 */
@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
public class TraversalEngine implements GremlinFluentPipeline {

	// Stream for graph traversal
	private Stream stream;

	// Manage traversal steps for Loop using Java reflection
	private List<Step> stepList;
	private Map<String, Integer> stepIndex;

	// Loop count starts with 0
	private int loopCount;

	// Class of stream element
	private Class elementClass;
	private Class collectionElementClass;

	private final Graph g;

	private boolean isParallel;

	public static String className = TraversalEngine.class.getName();

	private boolean isInnerTraversal = false;

	/**
	 * Initialize TraversalEngine
	 * 
	 * Path management is not supported. If needed, use PathEnabledTraversalEngine
	 * 
	 * @param g
	 * @param starts       of single Element (i.e., Graph, Vertex, Edge,
	 *                     VertexEvent, EdgeEvent) or Collection<Element>
	 * @param elementClass either of Graph.class, Vertex.class, Edge.class,
	 *                     VertexEvent.class, EdgeEvent.class
	 */
	public TraversalEngine(Graph g, Object starts, Class elementClass, boolean isParallel) {
		// Initialize Stream and Path
		if (starts instanceof Graph || starts instanceof Vertex || starts instanceof Edge
				|| starts instanceof VertexEvent || starts instanceof EdgeEvent) {
			stream = Stream.of(starts);
		} else if (starts instanceof Collection) {
			stream = ((Collection) starts).stream();
		}
		if (isParallel)
			stream.parallel();

		stepList = new ArrayList<Step>();
		stepIndex = new HashMap<String, Integer>();
		this.loopCount = 0;
		this.elementClass = elementClass;
		collectionElementClass = null;
		this.isParallel = isParallel;
		this.g = g;
		this.isInnerTraversal = false;
	}

	/**
	 * a private constructor used by loop Then, innerLoop and invoke would be used
	 * 
	 * @param g
	 * @param starts
	 * @param stepList     immutable
	 * @param stepIndex    immutable
	 * @param loopCount
	 * @param elementClass
	 * @param isParallel
	 */
	private TraversalEngine(Graph g, List starts, List<Step> stepList, Map<String, Integer> stepIndex, int loopCount,
			Class elementClass, boolean isParallel) {
		// Initialize Stream and Path
		stream = starts.stream();
		if (isParallel)
			stream.parallel();
		this.stepList = stepList;
		this.stepIndex = stepIndex;
		this.loopCount = loopCount;
		this.elementClass = elementClass;
		this.collectionElementClass = null;
		this.g = g;
		this.isInnerTraversal = true;
	}

	private <T> Stream<T> iterableToStream(Iterable<T> iterable) {
		if (g instanceof MChronoGraph) {
			if (isParallel)
				return ((Collection<T>) iterable).parallelStream();
			else
				return ((Collection<T>) iterable).stream();
		} else {
			ArrayList<T> collected = new ArrayList<T>();
			for (T t : iterable) {
				collected.add(t);
			}
			if (isParallel)
				return collected.parallelStream();
			else
				return collected.stream();
		}
	}

	///////////////////////////
	///// TRANSFORM PIPES ////
	//////////////////////////

	@Override
	public TraversalEngine V() {
		// Check Input element class
		checkInputElementClass(Graph.class);

		// Pipeline Update
		stream = stream.flatMap(g -> {
			return iterableToStream(((Graph) g).getVertices());
		});

		// Set Class
		elementClass = Vertex.class;

		// Step Update
		if (!isInnerTraversal) {
			Class[] args = new Class[0];
			Step step = new Step(className, "V", args);
			stepList.add(step);
		}
		return this;
	}

	@Override
	public TraversalEngine V(String key, Object value) {
		// Check Input element class
		checkInputElementClass(Graph.class);

		// Pipeline Update
		stream = stream.flatMap(g -> {
			return iterableToStream(((Graph) g).getVertices(key, value));
		});

		// Set Class
		elementClass = Vertex.class;

		// Step Update
		if (!isInnerTraversal) {
			Class[] args = new Class[2];
			args[0] = String.class;
			args[1] = Object.class;
			Step step = new Step(className, "V", args, key, value);
			stepList.add(step);
		}
		return this;
	}

	@Override
	public TraversalEngine E() {
		// Check Input element class
		checkInputElementClass(Graph.class);

		// Pipeline Update
		stream = stream.flatMap(g -> {
			return iterableToStream(((Graph) g).getEdges());
		});

		// Set Class
		elementClass = Edge.class;

		// Step Update
		if (!isInnerTraversal) {
			Class[] args = {};
			Step step = new Step(className, "E", args);
			stepList.add(step);
		}
		return this;

	}

	@Override
	public TraversalEngine E(String key, Object value) {
		// Check Input element class
		checkInputElementClass(Graph.class);

		// Pipeline Update
		stream = stream.flatMap(g -> {
			return iterableToStream(((Graph) g).getEdges(key, value));
		});

		// Set Class
		elementClass = Edge.class;

		// Step Update
		if (!isInnerTraversal) {
			Class[] args = new Class[2];
			args[0] = String.class;
			args[1] = Object.class;
			Step step = new Step(className, "E", args, key, value);
			stepList.add(step);
		}
		return this;
	}

	@Override
	public TraversalEngine id() {
		// Check Input element class
		checkInputElementClass(Vertex.class, Edge.class);

		// Pipeline Update
		stream = stream.map(e -> {
			return e.toString();
		});

		// Set Class
		elementClass = String.class;

		// Step Update
		if (!isInnerTraversal) {
			Class[] args = {};
			Step step = new Step(className, "id", args);
			stepList.add(step);
		}
		return this;
	}

	@Override
	public TraversalEngine element(final Class<? extends Element> elementClass) {
		// Check Input element class
		checkInputElementClass(String.class);

		// Pipeline Update
		stream = stream.map(id -> {
			if (elementClass == Vertex.class) {
				return g.getVertex((String) id);
			} else if (elementClass == Edge.class) {
				return g.getEdge((String) id);
			}
			return null;
		});

		// Set Class
		this.elementClass = elementClass;

		// Step Update
		if (!isInnerTraversal) {
			Class[] args = { Class.class };
			Step step = new Step(className, "element", args, elementClass);
			stepList.add(step);
		}
		return this;
	}

	@Override
	public TraversalEngine outE(List<String> labels) {
		// Check Input element class
		checkInputElementClass(Vertex.class);

		// Pipeline Update
		stream = stream.flatMap(v -> {
			return iterableToStream(((Vertex) g).getEdges(Direction.OUT, labels));
		});

		// Set Class
		elementClass = Edge.class;

		// Step Update
		if (!isInnerTraversal) {
			Class[] args = new Class[1];
			args[0] = List.class;
			Step step = new Step(className, "outE", args, labels);
			stepList.add(step);
		}
		return this;
	}

	@Override
	public TraversalEngine inE(List<String> labels) {
		// Check Input element class
		checkInputElementClass(Vertex.class);

		// Pipeline Update
		stream = stream.flatMap(v -> {
			return iterableToStream(((Vertex) g).getEdges(Direction.IN, labels));
		});

		// Set Class
		elementClass = Edge.class;

		// Step Update
		if (!isInnerTraversal) {
			Class[] args = new Class[1];
			args[0] = List.class;
			Step step = new Step(className, "inE", args, labels);
			stepList.add(step);
		}
		return this;
	}

	@Override
	public TraversalEngine out(List<String> labels) {
		// Check Input element class
		checkInputElementClass(Vertex.class);

		// Pipeline Update
		stream = stream.flatMap(v -> {
			return iterableToStream(((Vertex) g).getVertices(Direction.OUT, labels));
		});

		// Step Update
		if (!isInnerTraversal) {
			Class[] args = new Class[1];
			args[0] = List.class;
			Step step = new Step(className, "out", args, labels);
			stepList.add(step);
		}
		return this;
	}

	@Override
	public TraversalEngine in(List<String> labels) {
		// Check Input element class
		checkInputElementClass(Vertex.class);

		// Pipeline Update
		stream = stream.flatMap(v -> {
			return iterableToStream(((Vertex) g).getVertices(Direction.IN, labels));
		});

		// Step Update
		if (!isInnerTraversal) {
			Class[] args = new Class[1];
			args[0] = List.class;
			Step step = new Step(className, "in", args, labels);
			stepList.add(step);
		}
		return this;
	}

	@Override
	public TraversalEngine outV() {
		// Check Input element class
		checkInputElementClass(Edge.class);

		// Pipeline Update
		stream = stream.map(e -> {
			return ((Edge) e).getVertex(Direction.OUT);
		});

		// Set Class
		elementClass = Vertex.class;

		// Step Update
		if (!isInnerTraversal) {
			Class[] args = {};
			Step step = new Step(className, "outV", args);
			stepList.add(step);
		}
		return this;
	}

	@Override
	public TraversalEngine inV() {
		// Check Input element class
		checkInputElementClass(Edge.class);

		// Pipeline Update
		stream = stream.map(e -> {
			return ((Edge) e).getVertex(Direction.IN);
		});

		// Set Class
		elementClass = Vertex.class;

		// Step Update
		if (!isInnerTraversal) {
			Class[] args = {};
			Step step = new Step(className, "inV", args);
			stepList.add(step);
		}
		return this;
	}

	@Override
	public TraversalEngine map(Function function, Class<?> elementClass, Class<?> collectionElementClass) {
		stream = stream.map(entry -> {
			return function.apply(entry);
		});

		// Set the class of element and collection
		this.elementClass = elementClass;
		this.collectionElementClass = collectionElementClass;

		// Step Update
		if (!isInnerTraversal) {
			Class[] args = { Function.class, Class.class, Class.class };
			Step step = new Step(className, "map", args, function, elementClass, collectionElementClass);
			stepList.add(step);
		}
		return this;
	}

	@Override
	public TraversalEngine flatMap(Function function, Class<?> elementClass) {
		stream = stream.flatMap(entry -> {
			if (isParallel)
				return ((Collection) function.apply(entry)).parallelStream();
			else
				return ((Collection) function.apply(entry)).stream();
		});

		// Set the class of element and collection
		this.elementClass = elementClass;
		this.collectionElementClass = null;

		// Step Update
		if (!isInnerTraversal) {
			Class[] args = { Function.class, Class.class };
			Step step = new Step(className, "flatMap", args, function, elementClass);
			stepList.add(step);
		}
		return this;
	}

	public TraversalEngine gather() {
		if (elementClass != List.class) {
			// Pipeline Update
			List intermediate = (List) stream.collect(Collectors.toList());
			if (isParallel)
				stream = List.of(intermediate).parallelStream();
			else
				stream = List.of(intermediate).stream();
			// Set Class
			collectionElementClass = elementClass;
			elementClass = List.class;
		}

		// Step Update
		if (!isInnerTraversal) {
			Class[] args = {};
			Step step = new Step(className, "gather", args);
			stepList.add(step);
		}
		return this;
	}

	public TraversalEngine scatter() {
		// Check Input element class
		if (elementClass == List.class) {
			stream = stream.flatMap(e -> ((List) e).parallelStream());
			// Set Class
			elementClass = collectionElementClass;
			collectionElementClass = null;
		}

		// Step Update
		if (!isInnerTraversal) {
			Class[] args = {};
			Step step = new Step(className, "scatter", args);
			stepList.add(step);
		}
		return this;
	}

	////////////////////
	/// FILTER PIPES ///
	////////////////////

	@Override
	public TraversalEngine has(String key) {
		// Check Input element class
		checkInputElementClass(Vertex.class, Edge.class);

		// Pipeline Update
		stream = stream.filter(e -> ((Element) e).getProperty(key) != null);

		// Step Update
		if (!isInnerTraversal) {
			Class[] args = { String.class };
			Step step = new Step(className, "has", args, key);
			stepList.add(step);
		}
		return this;
	}

	@Override
	public TraversalEngine has(String key, Object value) {
		// Check Input element class
		checkInputElementClass(Vertex.class, Edge.class);

		// Pipeline Update
		stream = stream.filter(e -> {
			Object v = ((Element) e).getProperty(key);
			if (v == null || !v.equals(value))
				return false;
			return true;
		});

		// Step Update
		if (!isInnerTraversal) {
			Class[] args = { String.class, Object.class };
			Step step = new Step(className, "has", args, key, value);
			stepList.add(step);
		}
		return this;
	}

	@Override
	public <E> TraversalEngine filter(Predicate<E> predicate) {
		// Pipeline Update
		stream = stream.filter(e -> predicate.test((E) e));

		// Step Update
		if (!isInnerTraversal) {
			Class[] args = { Predicate.class };
			Step step = new Step(className, "filter", args, predicate);
			stepList.add(step);
		}
		return this;
	}

	@Override
	public TraversalEngine dedupEdgeLabel() {
		// Check Input element class
		checkInputElementClass(Edge.class);

		// Pipeline Update
		Map<String, Edge> map = (Map<String, Edge>) stream.collect(Collectors.toMap(new Function<Edge, String>() {
			@Override
			public String apply(Edge t) {
				String[] arr = t.getId().split("\\|");
				return arr[0] + "|" + arr[2];
			}
		}, new Function<Edge, Edge>() {
			@Override
			public Edge apply(Edge t) {
				return t;
			}
		}, (e1, e2) -> e1, HashMap<String, Edge>::new));
		if (isParallel)
			stream = map.values().parallelStream();
		else
			stream = map.values().stream();

		// Step Update
		if (!isInnerTraversal) {
			Class[] args = {};
			Step step = new Step(className, "dedupEdgeLabel", args);
			stepList.add(step);
		}

		return this;
	}

	@Override
	public TraversalEngine dedup() {
		// Pipeline Update
		stream = stream.distinct();

		// Step Update
		if (!isInnerTraversal) {
			Class[] args = {};
			Step step = new Step(className, "dedup", args);
			stepList.add(step);
		}
		return this;
	}

	@Override
	public TraversalEngine random(Double bias) {
		// Pipeline Update
		stream = stream.filter(e -> bias >= new Random().nextDouble());

		// Step Update
		if (!isInnerTraversal) {
			Class[] args = { Double.class };
			Step step = new Step(className, "random", args, bias);
			stepList.add(step);
		}
		return this;
	}

	@Override
	public <E> TraversalEngine sort(Comparator<E> comparator) {
		// Pipeline Update
		stream = stream.sorted(comparator);

		// Step Update
		if (!isInnerTraversal) {
			Class[] args = { Comparator.class };
			Step step = new Step(className, "sort", args, comparator);
			stepList.add(step);
		}
		return this;
	}

	@Override
	public TraversalEngine limit(Long maxSize) {
		// Pipeline Update
		stream = stream.limit(maxSize);

		// Step Update
		if (!isInnerTraversal) {
			Class[] args = { Long.class };
			Step step = new Step(className, "limit", args, maxSize);
			stepList.add(step);
		}
		return this;
	}

	/////////////////////////
	/// SIDE-EFFECT PIPES ///
	/////////////////////////

	@Override
	public <E> TraversalEngine sideEffect(Collection<E> collection) {
		stream = stream.map(e -> {
			collection.add((E) e);
			return e;
		});

		// Step Update
		if (!isInnerTraversal) {
			Class[] args = { Collection.class };
			Step step = new Step(className, "sideEffect", args, collection);
			stepList.add(step);
		}
		return this;
	}

	@Override
	public <E> TraversalEngine sideEffect(Consumer<E> function) {
		stream = stream.map(e -> {
			function.accept((E) e);
			return e;
		});

		// Step Update
		if (!isInnerTraversal) {
			Class[] args = { Consumer.class };
			Step step = new Step(className, "sideEffect", args, function);
			stepList.add(step);
		}
		return this;
	}

	////////////////////
	/// BRANCH PIPES ///
	////////////////////

	@Override
	public <E> TraversalEngine ifThenElseMap(Predicate<E> ifPredicate, Function<E, ?> thenFunction,
			Function<E, ?> elseFunction, Class<?> elementClass, Class<?> collectionElementClass) {
		// Pipeline Update
		stream = stream.map(element -> {
			if ((boolean) ifPredicate.test((E) element)) {
				return thenFunction.apply((E) element);
			} else {
				return elseFunction.apply((E) element);
			}
		});

		this.elementClass = elementClass;
		this.collectionElementClass = collectionElementClass;

		// Step Update
		if (!isInnerTraversal) {
			Class[] args = { Predicate.class, Function.class, Function.class, Class.class, Class.class };
			Step step = new Step(className, "ifThenElseMap", args, ifPredicate, thenFunction, elseFunction,
					elementClass, collectionElementClass);
			stepList.add(step);
		}
		return this;
	}

	@Override
	public <I> TraversalEngine ifThenElseFlatMap(Predicate<I> ifPredicate, Function<I, Collection<?>> thenFunction,
			Function<I, Collection<?>> elseFunction, Class<?> elementClass) {
		// Pipeline Update
		stream = stream.flatMap(element -> {
			if ((boolean) ifPredicate.test((I) element)) {
				if (isParallel)
					return thenFunction.apply((I) element).parallelStream();
				else
					return thenFunction.apply((I) element).stream();
			} else {
				if (isParallel)
					return elseFunction.apply((I) element).parallelStream();
				else
					return elseFunction.apply((I) element).stream();
			}
		});

		this.elementClass = elementClass;
		this.collectionElementClass = null;

		// Step Update
		if (!isInnerTraversal) {
			Class[] args = { Predicate.class, Function.class, Function.class, Class.class };
			Step step = new Step(className, "ifThenElseFlatMap", args, ifPredicate, thenFunction, elseFunction,
					elementClass);
			stepList.add(step);
		}
		return this;
	}

	/**
	 * Label current step
	 * 
	 * @param name
	 * @return
	 */
	public TraversalEngine as(String pointer) {
		// Step Update
		if (!isInnerTraversal) {
			Class[] args = { String.class };
			Step step = new Step(className, "as", args, pointer);
			stepList.add(step);

			this.stepIndex.put(pointer, stepList.indexOf(step));
		} else {
			throw new UnsupportedOperationException();
		}
		return this;
	}

	@Override
	public <E> TraversalEngine loop(String pointer, Predicate<LoopBundle<E>> whilePredicate) {
		// Pipeline Update
		int lastStepIdx = stepList.size();
		List traversalSet;
		if (elementClass == List.class) {
			traversalSet = stream.flatMap(list -> ((List) list).stream()).toList();
			elementClass = collectionElementClass;
		} else {
			traversalSet = stream.toList();
		}

		if (whilePredicate.test(new LoopBundle(traversalSet, null, this.loopCount))) {
			Integer backStepIdx = stepIndex.get(pointer);
			if (backStepIdx == null || (lastStepIdx - (backStepIdx) + 1) < 0) {
				stream = makeStream(traversalSet, isParallel);
			} else {
				TraversalEngine innerPipeline = new TraversalEngine(g, traversalSet, this.stepList, this.stepIndex,
						this.loopCount + 1, elementClass, isParallel);
				List loopSteps = stepList.subList(backStepIdx + 1, lastStepIdx);
				for (Object stepObject : loopSteps) {
					Step step = (Step) stepObject;
					step.setInstance(innerPipeline);
				}
				innerPipeline = innerPipeline.invoke(loopSteps);
				innerPipeline = innerPipeline.innerLoop(loopSteps, whilePredicate);

				stream = makeStream(innerPipeline.toList(), isParallel);
			}

		} else {
			stream = traversalSet.stream();
			if (isParallel)
				stream.parallel();
		}

		// Step Update
		if (!isInnerTraversal) {
			Class[] args = { String.class, Predicate.class };
			Step step = new Step(className, "loop", args, pointer, whilePredicate);
			stepList.add(step);
		}
		return this;
	}

	public <E> TraversalEngine innerLoop(List<Step> stepList, Predicate<LoopBundle<E>> whilePredicate) {
		// Inner Pipeline Update
		List traversalSet;
		if (elementClass == List.class) {
			traversalSet = stream.flatMap(list -> ((List) list).stream()).toList();
			elementClass = collectionElementClass;
		} else {
			traversalSet = stream.toList();
		}

		if (whilePredicate.test(new LoopBundle(traversalSet, null, this.loopCount))) {

			TraversalEngine innerPipeline = new TraversalEngine(g, traversalSet, this.stepList, this.stepIndex,
					this.loopCount + 1, elementClass, isParallel);
			for (Object stepObject : stepList) {
				Step step = (Step) stepObject;
				step.setInstance(innerPipeline);
			}
			innerPipeline = innerPipeline.invoke(stepList);
			innerPipeline = innerPipeline.innerLoop(stepList, whilePredicate);

			stream = makeStream(innerPipeline.toList(), isParallel);

		} else {
			stream = traversalSet.stream();
			if (isParallel)
				stream.parallel();
		}

		return this;
	}

	public TraversalEngine invoke(List<Step> stepList) {
		for (Step step : stepList) {
			step.invoke();
		}
		return this;
	}

	///////////////////////
	/// UTILITY METHODS ///
	///////////////////////

	/**
	 * Compute the stream and return the results as List
	 * 
	 * @return
	 */
	public <I> List<I> toList() {
		try {
			return (List<I>) stream.collect(Collectors.toList());
		} catch (ClassCastException e) {
			throw e;
		}
	}

	/**
	 * TODO
	 */

	@Override
	public <I, T> Map<T, List<I>> groupBy(Function<I, T> classifier) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <I, T> Map<T, Long> groupCount(Function<I, T> classifier) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<?> reduce(BinaryOperator<?> reducer) {
		// TODO Auto-generated method stub
		return null;
	}

	public TraversalEngine toEvent(Long timestamp) {

		// Pipeline Update
		stream = stream.map(element -> {
			if (element instanceof Vertex) {
				// return ((Vertex) element).getEvent(new Time(timestamp, timestamp));
				return null;
			} else if (element instanceof Edge) {
				// return ((Edge) element).getEvent(timestamp, timestamp);
				return null;
			}

			return null;
		});

		// Set Class
		if (elementClass == Vertex.class)
			elementClass = VertexEvent.class;
		else if (elementClass == Edge.class)
			elementClass = EdgeEvent.class;

		// Step Update
		if (!isInnerTraversal) {
			Class[] args = new Class[1];
			args[0] = Long.class;
			Step step = new Step(className, "toEvent", args, timestamp);
			stepList.add(step);
		}
		return this;
	}

	/**
	 * Traversers move from Graph to Stream<EdgeEvent> using G.getEdgeEventSet()
	 * 
	 * <Lazy> , Greedy
	 * 
	 * @return TraversalEngine
	 */
	public TraversalEngine e() {
		// Check Input element class
		checkInputElementClass(Graph.class);

		// Pipeline Update
		stream = stream.flatMap(g -> {
			// return ((Graph) g).getEdgeEventSet().parallelStream();
			return null;
		});

		// Set Class
		elementClass = EdgeEvent.class;

		// Step Update
		if (!isInnerTraversal) {
			Class[] args = {};
			Step step = new Step(className, "e", args);
			stepList.add(step);
		}
		return this;
	}

	////////////////////////////////////////
	///// Temporal Traversal Language /////
	//////////////////////////////////////

	//////////////////////
	/// UTILITY PIPES ///
	//////////////////////

	private Stream makeStream(Object e, boolean isParallel) {
		Stream s;
		if (e instanceof Stream)
			s = (Stream) e;
		else if (e instanceof Collection)
			s = ((Collection) e).stream();
		else {
			s = Stream.of(e);
		}
		if (isParallel)
			s.parallel();
		return s;
	}

	private Stream getStream(Map intermediate) {
		if (elementClass == List.class) {
			ArrayList next = new ArrayList();
			next.addAll(intermediate.values());
			return next.parallelStream();
		} else {
			Set next = (Set) intermediate.values().parallelStream().flatMap(e -> {
				if (e instanceof Collection) {
					return ((Collection) e).parallelStream();
				} else {
					return Stream.of(e);
				}
			}).collect(Collectors.toSet());
			return next.parallelStream();
		}
	}

	private void checkInputElementClass(Class correctClass) {
		if (elementClass != correctClass)
			throw new UnsupportedOperationException(
					"Current stream element class " + elementClass + " should be " + correctClass);
	}

	private void checkInvalidInputElementClass(Class wrongClass) {
		if (elementClass == wrongClass)
			throw new UnsupportedOperationException(
					"Current stream element class " + elementClass + " is not available");
	}

	private void checkInputElementClass(Class... correctClasses) {
		boolean isMatched = false;
		for (Class correct : correctClasses) {
			if (elementClass == correct) {
				isMatched = true;
				break;
			}
		}
		if (isMatched == false) {
			throw new UnsupportedOperationException(
					"Current stream element class " + elementClass + " should be one of " + correctClasses);
		}
	}

	@Override
	public TraversalEngine v(Long t) {
		// Check Input element class
		checkInputElementClass(Vertex.class);

		// Pipeline Update
		stream = stream.map(v -> {
			return ((Vertex) v).getEvent(t);
		});

		// Set Class
		elementClass = VertexEvent.class;

		// Step Update
		if (!isInnerTraversal) {
			Class[] args = { Long.class };
			Step step = new Step(className, "v", args, t);
			stepList.add(step);
		}
		return this;
	}

	@Override
	public TraversalEngine oute(TemporalRelation tr, String label) {
		// Check Input element class
		checkInputElementClass(VertexEvent.class);

		// Pipeline Update
		stream = stream.flatMap(ve -> {
			return iterableToStream(((VertexEvent) g).getVertexEvents(Direction.OUT, tr, label));
		});

		// Step Update
		if (!isInnerTraversal) {
			Class[] args = { TemporalRelation.class, String.class };
			Step step = new Step(className, "oute", args, tr, label);
			stepList.add(step);
		}
		return this;
	}

	@Override
	public TraversalEngine ine(TemporalRelation tr, String label) {
		// Check Input element class
		checkInputElementClass(VertexEvent.class);

		// Pipeline Update
		stream = stream.flatMap(ve -> {
			return iterableToStream(((VertexEvent) g).getVertexEvents(Direction.IN, tr, label));
		});

		// Step Update
		if (!isInnerTraversal) {
			Class[] args = { TemporalRelation.class, String.class };
			Step step = new Step(className, "ine", args, tr, label);
			stepList.add(step);
		}
		return this;
	}

	@Override
	public TraversalEngine outEe(TemporalRelation tr, String label) {
		// Check Input element class
		checkInputElementClass(VertexEvent.class);

		// Pipeline Update
		stream = stream.flatMap(ve -> {
			return iterableToStream(((VertexEvent) ve).getEdgeEvents(Direction.OUT, tr, label));
		});

		// Set Class
		elementClass = EdgeEvent.class;

		// Step Update
		if (!isInnerTraversal) {
			Class[] args = { TemporalRelation.class, String.class };
			Step step = new Step(className, "outEe", args, tr, label);
			stepList.add(step);
		}
		return this;
	}

	@Override
	public TraversalEngine inEe(TemporalRelation tr, String label) {
		// Check Input element class
		checkInputElementClass(VertexEvent.class);

		// Pipeline Update
		stream = stream.flatMap(ve -> {
			return iterableToStream(((VertexEvent) ve).getEdgeEvents(Direction.IN, tr, label));
		});

		// Set Class
		elementClass = EdgeEvent.class;

		// Step Update
		if (!isInnerTraversal) {
			Class[] args = { TemporalRelation.class, String.class };
			Step step = new Step(className, "inEe", args, tr, label);
			stepList.add(step);
		}
		return this;
	}

	@Override
	public TraversalEngine outVe() {
		// Check Input element class
		checkInputElementClass(EdgeEvent.class);

		// Pipeline Update
		stream = stream.map(ee -> {
			return ((EdgeEvent) ee).getVertexEvent(Direction.OUT);
		});

		// Set Class
		elementClass = VertexEvent.class;

		// Step Update
		if (!isInnerTraversal) {
			Class[] args = {};
			Step step = new Step(className, "outVe", args);
			stepList.add(step);
		}
		return this;
	}

	@Override
	public TraversalEngine inVe() {
		// Check Input element class
		checkInputElementClass(EdgeEvent.class);

		// Pipeline Update
		stream = stream.map(ee -> {
			return ((EdgeEvent) ee).getVertexEvent(Direction.IN);
		});

		// Set Class
		elementClass = VertexEvent.class;

		// Step Update
		if (!isInnerTraversal) {
			Class[] args = {};
			Step step = new Step(className, "inVe", args);
			stepList.add(step);
		}
		return this;
	}

	@Override
	public <K> TraversalEngine retainEvent(Function<Event, K> keyMapper, Object gamma,
			RetainEventBinaryOperator<Event> retainEventBinaryOperator) {
		// Check Input element class
		checkInputElementClass(VertexEvent.class, EdgeEvent.class);

		// Pipeline Update
		Map<Element, Event> retainedEventPerElement = (Map<Element, Event>) stream
				.collect(Collectors.toMap(keyMapper, new Function<Object, Event>() {
					@Override
					public Event apply(Object obj) {
						return (Event) obj;
					}
				}, (e1, e2) -> retainEventBinaryOperator.apply(gamma, e1, e2)));

		if (isParallel)
			stream = retainedEventPerElement.values().parallelStream();
		else
			stream = retainedEventPerElement.values().stream();

		// Step Update
		if (!isInnerTraversal) {
			Class[] args = { Function.class, Object.class, RetainEventBinaryOperator.class };
			Step step = new Step(className, "retainEvent", args, keyMapper, gamma, retainEventBinaryOperator);
			stepList.add(step);
		}
		return this;
	}

}
