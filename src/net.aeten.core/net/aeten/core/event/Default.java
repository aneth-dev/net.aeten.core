package net.aeten.core.event;

public class Default {
	private Default() {}
	
	public static class SingleEvent {
		private SingleEvent() {}
	}
	public static class AnonymousSource {
		private AnonymousSource() {}
	}
	public static final SingleEvent SINGLE_EVENT = new SingleEvent();
	public static final AnonymousSource ANONYMOUS_SOURCE = new AnonymousSource();
	
	public class DefaultEventData<Data> extends EventData<AnonymousSource, SingleEvent> {
		private final Data data;
		public DefaultEventData(Data data) {
		    super(ANONYMOUS_SOURCE, SINGLE_EVENT);
		    this.data = data;
	    }
		public Data getData() {
		    return data;
	    }
	}

}
