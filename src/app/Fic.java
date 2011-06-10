package app;

import java.io.IOException;
import java.util.Arrays;
import java.util.ListIterator;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import lib.Compressor;
import lib.Decompressor;

/**
 * Command line utility to compress an image using
 * fractal image compression methods. 
 * 
 * @author c00kiemon5ter
 */
public class Fic {

	/**
	 * the system logger
	 */
	private static final Logger LOGGER = Logger.getLogger("debugger");
	/**
	 * Output control - Verbose and Debug messages
	 */
	private static boolean DEBUG, VERBOSE;

	/**
	 * Do some work, start the app, read the args, validate state and run.
	 * 
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		Fic fic = new Fic();
		fic.parseCli(args);
		fic.validateProperties();
		fic.createAndRunTask();
	}
	/**
	 * the properties table holds the configuration settings
	 */
	private Properties properties;

	/**
	 * The fic object is an instance of the application. 
	 * The constructor initializes the properties table
	 * with the default values.
	 */
	public Fic() {
		this.properties = new Properties() {{
			setProperty(Option.OUTPUT.toString(), "output.fic");
			setProperty(Option.QUALITY.toString(), "80");
			setProperty(Option.VERBOSE.toString(), Boolean.FALSE.toString());
			setProperty(Option.DEBUG.toString(), Boolean.FALSE.toString());
		}};
	}

	/**
	 * Run the command with the appropriate options
	 */
	private void createAndRunTask() {
		Runnable task = null;
		// TODO: call the lib -- process properties
		switch (Command.valueOf(properties.getProperty(Command.ID))) {
			case COMPRESS:
				if (VERBOSE) {
					LOGGER.log(Level.INFO, ":: Initializing compress process..");
				}
				task = new Compressor();
				break;
			case DECOMPRESS:
				if (VERBOSE) {
					LOGGER.log(Level.INFO, ":: Initializing decompress process..");
				}
				task = new Decompressor();
				break;
		}

		assert task != null : "==> ERROR[null]: No task initialized";
		if (DEBUG) {
			LOGGER.log(Level.INFO, ":: Initialized task. Starting thread execution..");
		}
		// FIXME: for now just run the tests
		//ExecutorService executor = Executors.newSingleThreadExecutor();
		//executor.execute(task);
		new TestTask(properties).run();
	}

	/**
	 * Validate properties attributes
	 */
	private void validateProperties() {
		DEBUG = Boolean.parseBoolean(properties.getProperty(Option.DEBUG.toString()));
		if (DEBUG) {
			VERBOSE = true;
			String logfile = properties.getProperty(Option.LOG.toString());
			if (logfile != null) {
				try {
					LOGGER.addHandler(new FileHandler(logfile));
				} catch (IOException ex) {
					LOGGER.log(Level.WARNING, String.format("==> ERROR[io]: Cannot write logfile: %s", logfile));
				} catch (SecurityException ex) {
					LOGGER.log(Level.WARNING, String.format("==> ERROR[sec]: Cannot write logfile: %s", logfile));
				}
			}
		} else {
			VERBOSE = Boolean.parseBoolean(properties.getProperty(Option.VERBOSE.toString()));
		}
		
		if (DEBUG) {
			LOGGER.log(Level.INFO, String.format(":: Validating: %s ..", Command.ID));
		}
		if (properties.getProperty(Command.ID) == null) {
			usage();
			System.err.println(Error.REQUIRED_ARG_NOT_FOUND.description(Command.ID));
			System.exit(Error.REQUIRED_ARG_NOT_FOUND.errcode());
		}

		if (DEBUG) {
			LOGGER.log(Level.INFO, String.format(":: Validating: %s ..", Option.INPUT));
		}
		if (properties.getProperty(Option.INPUT.toString()) == null) {
			usage();
			System.err.println(Error.REQUIRED_ARG_NOT_FOUND.description(Option.INPUT.option()));
			System.exit(Error.REQUIRED_ARG_NOT_FOUND.errcode());
		}

		if (DEBUG) {
			LOGGER.log(Level.INFO, String.format(":: Validating: %s ..", Option.QUALITY));
		}
		String qualitystr = properties.getProperty(Option.QUALITY.toString());
		try {
			Integer.parseInt(qualitystr);
		} catch (NumberFormatException nfe) {
			usage();
			System.err.println(Error.QUALITY_FORMAT.description(qualitystr));
			System.exit(Error.QUALITY_FORMAT.errcode());
		}
	}

	/**
	 * Parse command line options 
	 */
	private void parseCli(final String[] args) {
		if (args.length == 0) {
			usage();
			System.err.println(Error.ARG_COUNT.description(args.length));
			System.exit(Error.ARG_COUNT.errcode());
		}

		ListIterator<String> iterator = Arrays.asList(args).listIterator();
		while (iterator.hasNext()) {
			String clielement = iterator.next();

			if (Option.HELP.option().equals(clielement)) {
				usage();
				System.exit(0);
			} else if (Command.COMPRESS.option().equals(clielement)) {
				properties.setProperty(Command.ID, Command.COMPRESS.toString());
			} else if (Command.DECOMPRESS.option().equals(clielement)) {
				properties.setProperty(Command.ID, Command.DECOMPRESS.toString());
			} else if (Option.INPUT.option().equals(clielement)) {
				if (iterator.hasNext() && (clielement = iterator.next()).charAt(0) != '-') {
					properties.setProperty(Option.INPUT.toString(), clielement);
				} else {
					usage();
					System.err.println(Error.MISSING_ARG.description(Option.INPUT.option()));
					System.exit(Error.MISSING_ARG.errcode());
				}
			} else if (Option.OUTPUT.option().equals(clielement)) {
				if (iterator.hasNext() && (clielement = iterator.next()).charAt(0) != '-') {
					properties.setProperty(Option.OUTPUT.toString(), clielement);
				} else {
					usage();
					System.err.println(Error.MISSING_ARG.description(Option.OUTPUT.option()));
					System.exit(Error.MISSING_ARG.errcode());
				}
			} else if (Option.QUALITY.option().equals(clielement)) {
				if (iterator.hasNext() && (clielement = iterator.next()).charAt(0) != '-') {
					properties.setProperty(Option.QUALITY.toString(), clielement);
				} else {
					usage();
					System.err.println(Error.MISSING_ARG.description(Option.QUALITY.option()));
					System.exit(Error.MISSING_ARG.errcode());
				}
			} else if (Option.VERBOSE.option().equals(clielement)) {
				properties.setProperty(Option.VERBOSE.toString(), Boolean.TRUE.toString());
			} else if (Option.DEBUG.option().equals(clielement)) {
				properties.setProperty(Option.DEBUG.toString(), Boolean.TRUE.toString());
			} else if (Option.LOG.option().equals(clielement)) {
				if (iterator.hasNext() && (clielement = iterator.next()).charAt(0) != '-') {
					properties.setProperty(Option.LOG.toString(), clielement);
				} else {
					usage();
					System.err.println(Error.MISSING_ARG.description(Option.LOG.option()));
					System.exit(Error.MISSING_ARG.errcode());
				}
			} else {
				usage();
				System.err.println(Error.UNKNOWN_ARG.description(clielement));
				System.exit(Error.UNKNOWN_ARG.errcode());
			}
		}
	}

	/**
	 * Build and display a usage message
	 */
	private void usage() {
		String headerformat = "usage: java -jar %s.jar <%s> [%s] %s <input-file>\n";
		String cmdformat = "\t\t%s\t%s\n";
		String optformat = "\t\t%s\t\t%s\n";
		StringBuilder helpmsg = new StringBuilder();
		helpmsg.append(String.format(headerformat, Fic.class.getSimpleName(),
									 Command.class.getSimpleName(),
									 Option.class.getSimpleName(),
									 Option.INPUT.option()));
		helpmsg.append("\n\tCommands:\n");
		for (Command cmd : Command.values()) {
			helpmsg.append(String.format(cmdformat, cmd.option(), cmd.description()));
		}
		helpmsg.append("\n\tOptions:\n");
		for (Option opt : Option.values()) {
			helpmsg.append(String.format(optformat, opt.option(), opt.description()));
		}
		System.out.println(helpmsg.toString());
	}
}