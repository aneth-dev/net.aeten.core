package net.aeten.core;

/**
 *
 * @author Thomas Pérennou
 */
public class ConfigurationException extends Exception {
    private static final long serialVersionUID = 2207797241241980028L;
    private final String configuration;

	public ConfigurationException(String configuration) {
		super("configuration=\"" + configuration + "\"");
		this.configuration = configuration;
	}

	public ConfigurationException(String configuration, String message) {
		super(message + " (configuration=\"" + configuration + "\")");
		this.configuration = configuration;
	}

	public ConfigurationException(String configuration, Throwable cause) {
		super("configuration=\"" + configuration + "\"", cause);
		this.configuration = configuration;
	}

	public ConfigurationException(String configuration, String message, Throwable cause) {
		super(message + " (configuration=\"" + configuration + "\")", cause);
		this.configuration = configuration;
	}

	public String getConfiguration() {
	    return configuration;
    }

}
