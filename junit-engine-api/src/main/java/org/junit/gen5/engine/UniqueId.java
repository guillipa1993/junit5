/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.junit.gen5.commons.JUnitException;
import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ToStringBuilder;

/**
 * {@code UniqueId} encapsulates the creation, parsing, and display of unique IDs
 * for {@link TestDescriptor TestDescriptors}.
 *
 * <p>Instances of this class have value semantics and are immutable.</p>
 *
 * @since 5.0
 */
@API(Experimental)
public class UniqueId implements Cloneable {

	private static final String ENGINE_SEGMENT_TYPE = "engine";

	/**
	 * Parse a {@code UniqueId} from the supplied string representation using the
	 * default format.
	 *
	 * @param uniqueId the string representation to parse; never {@code null} or empty
	 * @return a properly constructed {@code UniqueId}
	 * @throws JUnitException if the string cannot be parsed
	 */
	public static UniqueId parse(String uniqueId) throws JUnitException {
		Preconditions.notBlank(uniqueId, "Unique ID string must not be null or empty");
		return UniqueIdFormat.getDefault().parse(uniqueId);
	}

	/**
	 * Create an engine's unique ID from its {@code engineId} using the default
	 * format.
	 *
	 * @param engineId the engine ID; never {@code null} or empty
	 */
	public static UniqueId forEngine(String engineId) {
		Preconditions.notBlank(engineId, "engineId must not be null or empty");
		return root(ENGINE_SEGMENT_TYPE, engineId);
	}

	/**
	 * Create a root unique ID from the supplied {@code segmentType} and
	 * {@code value} using the default format.
	 *
	 * @param segmentType the segment type; never {@code null} or empty
	 * @param value the value; never {@code null} or empty
	 */
	public static UniqueId root(String segmentType, String value) {
		Preconditions.notBlank(segmentType, "segmentType must not be null or empty");
		Preconditions.notBlank(value, "value must not be null or empty");
		return new UniqueId(UniqueIdFormat.getDefault(), new Segment(segmentType, value));
	}

	private final UniqueIdFormat uniqueIdFormat;
	private final List<Segment> segments = new ArrayList<>();

	private UniqueId(UniqueIdFormat uniqueIdFormat, Segment segment) {
		this.uniqueIdFormat = uniqueIdFormat;
		this.segments.add(segment);
	}

	UniqueId(UniqueIdFormat uniqueIdFormat, List<Segment> segments) {
		this.uniqueIdFormat = uniqueIdFormat;
		this.segments.addAll(segments);
	}

	final Optional<Segment> getRoot() {
		return this.segments.size() > 0 ? Optional.of(this.segments.get(0)) : Optional.empty();
	}

	public final Optional<String> getEngineId() {
		return getRoot().filter(segment -> segment.getType().equals(ENGINE_SEGMENT_TYPE)).map(Segment::getValue);
	}

	/**
	 * Get a copy of the list of {@linkplain Segment segments} that make up this
	 * {@code UniqueId}.
	 *
	 * <p>Clients are free to modify the returned list.
	 */
	public final List<Segment> getSegments() {
		return new ArrayList<>(this.segments);
	}

	/**
	 * Construct a new {@code UniqueId} by appending a new {@link Segment}, based
	 * on the supplied {@code segmentType} and {@code value}, to the end of this
	 * {@code UniqueId}.
	 *
	 * <p>This {@code UniqueId} will not be modified.
	 *
	 * <p>Neither the {@code segmentType} nor the {@code value} may contain any
	 * of the special characters used for constructing the string representation
	 * of this {@code UniqueId}.
	 *
	 * @see #append(Segment)
	 */
	public final UniqueId append(String segmentType, String value) {
		Segment segment = new Segment(segmentType, value);
		return append(segment);
	}

	/**
	 * Construct a new {@code UniqueId} by appending the supplied {@link Segment}
	 * to the end of this {@code UniqueId}.
	 *
	 * <p>This {@code UniqueId} will not be modified.
	 *
	 * @see #append(String, String)
	 */
	public final UniqueId append(Segment segment) {
		UniqueId clone = new UniqueId(this.uniqueIdFormat, this.segments);
		clone.segments.add(segment);
		return clone;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		UniqueId that = (UniqueId) o;
		return this.segments.equals(that.segments);
	}

	@Override
	public int hashCode() {
		return this.segments.hashCode();
	}

	/**
	 * Generate the unique, formatted string representation of this {@code UniqueId}
	 * using the configured {@link UniqueIdFormat}.
	 */
	@Override
	public String toString() {
		return this.uniqueIdFormat.format(this);
	}

	/**
	 * A segment of a {@link UniqueId} comprises a <em>type</em> and a
	 * <em>value</em>.
	 */
	@API(Experimental)
	public static class Segment {

		private final String type;
		private final String value;

		/**
		 * Create a new {@code Segment} using the supplied {@code type} and
		 * {@code value}.
		 *
		 * @param type the type of the segment
		 * @param value the value of this segment
		 */
		public Segment(String type, String value) {
			this.type = type;
			this.value = value;
		}

		/**
		 * Get the type of this segment.
		 */
		public String getType() {
			return this.type;
		}

		/**
		 * Get the value of this segment.
		 */
		public String getValue() {
			return this.value;
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.type, this.value);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;

			Segment that = (Segment) o;
			return Objects.equals(this.type, that.type) && Objects.equals(this.value, that.value);
		}

		@Override
		public String toString() {
			// @formatter:off
			return new ToStringBuilder(this)
				.append("type", this.type)
				.append("value", this.value)
				.toString();
			// @formatter:on
		}

	}

}
