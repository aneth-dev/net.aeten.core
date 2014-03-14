package net.aeten.core.playrec;

public interface RecorderListener<T extends Record> {
	/** @param recorder the event trigger */
	void start(Recorder<T> recorder);

	/** @param recorder the event trigger */
	void stop(Recorder<T> recorder);

	/** @param recorder the event trigger */
	void pause(Recorder<T> recorder);

	/** @param recorder the event trigger */
	void resume(Recorder<T> recorder);

	public abstract class Adapter<T extends Record> implements RecorderListener<T> {
		@Override
		public void start(Recorder<T> recorder) {}

		@Override
		public void stop(Recorder<T> recorder) {}

		@Override
		public void pause(Recorder<T> recorder) {}

		@Override
		public void resume(Recorder<T> recorder) {}

	}
}
