package net.aeten.core.playrec;

import java.util.Date;

public class Record {
	private Date start;
	private Date end;

	public Record (Date start,
						Date end) {
		this.start = start;
		setEnd (end);
	}

	public Record (Date start) {
		this (start, null);
	}

	/** @return the start date of the record */
	public Date getStart () {
		return start;
	}

	/** @return the end date of the record */
	public synchronized Date getEnd () {
		return end;
	}

	/** @param start the start date of the record */
	public synchronized void setStart (Date start) {
		this.start = start;
	}

	/** @param end the end date of the record */
	public synchronized void setEnd (Date end) {
		this.end = end;
	}

}
