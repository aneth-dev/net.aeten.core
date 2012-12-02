package net.aeten.core.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import net.aeten.core.Platform;

public class NativeLibraryLoader {
	static {
		for (File file: getTempDir ().listFiles ()) {
			file.delete ();
		}
	}

	public static File loadNativeLibrary (	Class <?> loadinClass,
														String lname) {
		String libname = System.mapLibraryName (lname);
		String resourceName = getNativeResourcePath () + "/" + libname;
		URL url = loadinClass.getResource (resourceName);
		boolean unpacked = false;

		// Add an ugly hack for OpenJDK (soylatte) - JNI libs use the usual *.dylib extension 
		if (url == null && Platform.isMac () && resourceName.endsWith (".dylib")) {
			resourceName = resourceName.substring (0, resourceName.lastIndexOf (".dylib")) + ".jnilib";
			url = loadinClass.getResource (resourceName);
		}
		if (url == null) {
			throw new UnsatisfiedLinkError ("resource " + resourceName + " not found");
		}

		File lib = null;
		if (url.getProtocol ().toLowerCase ().equals ("file")) {
			try {
				lib = new File (new URI (url.toString ()));
			} catch (URISyntaxException e) {
				lib = new File (url.getPath ());
			}
			if (!lib.exists ()) {
				throw new Error ("File URL " + url + " could not be properly decoded");
			}
		} else {
			InputStream is = loadinClass.getResourceAsStream (resourceName);
			if (is == null) {
				throw new Error ("Can't obtain InputStream (" + resourceName + ")");
			}

			FileOutputStream fos = null;
			try {
				/* Suffix is required on windows, or library fails to load Let Java pick the suffix,
				 * except on windows, to avoid problems with Web Start.
				 */
				File dir = getTempDir ();
				lib = new File (dir, lname + (Platform.isWindows ()? ".dll": ""));
				if (lib.exists ()) {
					return lib;
				}
				lib.deleteOnExit ();
				fos = new FileOutputStream (lib);
				int count;
				byte[] buf = new byte[1024];
				while ((count = is.read (buf, 0, buf.length)) > 0) {
					fos.write (buf, 0, count);
				}
				unpacked = true;
			} catch (IOException exception) {
				throw new Error ("Failed to create temporary file for library: ", exception);
			} finally {
				try {
					is.close ();
				} catch (IOException exception) {}
				if (fos != null) {
					try {
						fos.close ();
					} catch (IOException exception) {}
				}
			}
		}
		System.load (lib.getAbsolutePath ());
		/* Attempt to delete immediately once jnidispatch is successfully loaded.
		 * This avoids the complexity of trying to do so on "exit",
		 * which point can vary under different circumstances
		 * (native compilation, dynamically loaded modules, normal application, etc).
		 */
		if (unpacked) {
			deleteNativeLibrary (lib.getAbsolutePath ());
		}
		return lib;
	}

	public static String getNativeResourcePath () {
		String osPrefix;
		switch (Platform.OS) {
		case WINDOWS:
			osPrefix = "win32-" + Platform.ARCH;
			break;
		case WINDOWSCE:
			osPrefix = "w32ce-" + Platform.ARCH;
			break;
		case MAC:
			osPrefix = "darwin";
			break;
		case LINUX:
			osPrefix = "linux-" + Platform.ARCH;
			break;
		case SOLARIS:
			osPrefix = "sunos-" + Platform.ARCH;
			break;
		default:
			osPrefix = Platform.OS.toString ();
			int space = osPrefix.indexOf (" ");
			if (space != -1) {
				osPrefix = osPrefix.substring (0, space);
			}
			osPrefix += "-" + Platform.ARCH;
			break;
		}
		return osPrefix;
	}

	/** Remove any automatically unpacked native library.

	This will fail on windows, which disallows removal of any file that is
	still in use, so an alternative is required in that case.  Mark
	the file that could not be deleted, and attempt to delete any
	temporaries on next startup.

	Do NOT force the class loader to unload the native library, since
	that introduces issues with cleaning up any extant JNA bits
	(e.g. Memory) which may still need use of the library before shutdown.
	*/
	static boolean deleteNativeLibrary (String path) {
		File flib = new File (path);
		if (flib.delete ()) {
			return true;
		}

		// Couldn't delete it, mark for later deletion
		markTemporaryFile (flib);

		return false;
	}

	/** Perform cleanup of automatically unpacked native shared library.
	 */
	static void markTemporaryFile (File file) {
		// If we can't force an unload/delete, flag the file for later 
		// deletion
		try {
			File marker = new File (file.getParentFile (), file.getName () + ".x");
			marker.createNewFile ();
		} catch (IOException e) {
			e.printStackTrace ();
		}
	}

	static File getTempDir () {
		File tmp = new File (System.getProperty ("java.io.tmpdir"));
		File nativeTmp = new File (tmp, "java-native-" + System.getProperty ("user.name"));
		nativeTmp.mkdirs ();
		return nativeTmp.exists ()? nativeTmp: tmp;
	}

}
