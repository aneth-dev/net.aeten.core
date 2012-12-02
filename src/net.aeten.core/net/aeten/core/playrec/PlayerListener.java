package net.aeten.core.playrec;

import java.util.Date;

public interface PlayerListener<T extends Record> {
	/**
	 * @param player the event trigger
	 **/
	void play (Player <T, ?> player);

	/** @param player the event trigger */
	void pause (Player <T, ?> player);

	/** @param player the event trigger */
	void close (Player <T, ?> player);

	/** 
	 * @param player the event trigger
	 * @param date the date of the position
	 * @param position 0<=position<=1
	 **/
	void position (Player <T, ?> player,
						Date date,
						double position);

	public abstract class Adapter<T extends Record> implements
			PlayerListener <T> {

		@Override
		public void play (Player <T, ?> player) {}

		@Override
		public void pause (Player <T, ?> player) {}

		@Override
		public void close (Player <T, ?> player) {}

		@Override
		public void position (	Player <T, ?> player,
										Date date,
										double position) {}
	}
}
