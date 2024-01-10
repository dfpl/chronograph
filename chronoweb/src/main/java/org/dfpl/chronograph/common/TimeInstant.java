package org.dfpl.chronograph.common;

public class TimeInstant {

	public static boolean checkTemporalRelation(Long t1, Long t2, TemporalRelation tr) {
		return switch (tr) {
		case isBefore -> t1 < t2;
		case isAfter -> t1 > t2;
		case cotemporal -> t1 == t2;
		default -> false;
		};
	}

	public static TemporalRelation getTemporalRelation(Long t1, Long t2) {
		if (t1 < t2)
			return TemporalRelation.isAfter;
		if (t1 > t2)
			return TemporalRelation.isBefore;
		if (t1 == t2)
			return TemporalRelation.cotemporal;

		return null;
	}
}
