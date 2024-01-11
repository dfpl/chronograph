package org.dfpl.chronograph.common;

/**
 * The in-memory implementation of temporal graph database.
 *
 * @author Jaewook Byun, Ph.D., Assistant Professor, DFPL, Department of
 *         Software, Sejong University
 * 
 * @author Haifa Gaza, Ph.D., Student, DFPL, Sejong University
 * 
 *         Gaza, Haifa, and Jaewook Byun. "Kairos: Enabling prompt monitoring of
 *         information diffusion over temporal networks." IEEE Transactions on
 *         Knowledge and Data Engineering (2023).
 * 
 *         Byun, Jaewook. "Enabling time-centric computation for efficient
 *         temporal graph traversals from multiple sources." IEEE Transactions
 *         on Knowledge and Data Engineering (2020).
 * 
 *         Byun, Jaewook, Sungpil Woo, and Daeyoung Kim. "Chronograph: Enabling
 *         temporal graph traversals for efficient information diffusion
 *         analysis over time." IEEE Transactions on Knowledge and Data
 *         Engineering 32.3 (2019): 424-437.
 * 
 */
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
