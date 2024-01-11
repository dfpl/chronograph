package org.dfpl.chronograph.common;

public class TimeInstant {

	public static boolean checkTemporalRelation(Long t1, Long t2, TemporalRelation tr) {
		return switch (tr) {
		case isBefore -> t1.longValue() < t2.longValue();
		case isAfter -> t1.longValue() > t2.longValue();
		case cotemporal -> t1.longValue() == t2.longValue();
		default -> false;
		};
	}

	public static TemporalRelation getTemporalRelation(Long t1, Long t2) {
		if (t1.longValue() < t2.longValue())
			return TemporalRelation.isAfter;
		if (t1.longValue() > t2.longValue())
			return TemporalRelation.isBefore;
		if (t1.longValue() == t2.longValue())
			return TemporalRelation.cotemporal;

		return null;
	}
}
