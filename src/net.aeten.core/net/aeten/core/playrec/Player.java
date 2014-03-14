package net.aeten.core.playrec;

import java.util.Date;

public interface Player<T extends Record, L extends PlayerListener<T>> {
	void play(boolean loop);

	void pause();

	void close();

	/** @param position in percent: 0<=position<=1 */
	void setPosition(double position);

	/** @param date the date */
	void setPosition(Date date);

	T getRecord();

	void addListener(L listener);

	void removeListener(PlayerListener<?> listener);
}
