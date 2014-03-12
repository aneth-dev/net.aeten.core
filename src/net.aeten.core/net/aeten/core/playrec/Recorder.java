package net.aeten.core.playrec;

public interface Recorder<T extends Record> {
	void start ();

	void stop ();

	void pause ();

	void resume ();

	T getRecord ();

	void addListener (RecorderListener <T> listener);

	void removeListener (RecorderListener <?> listener);
}
