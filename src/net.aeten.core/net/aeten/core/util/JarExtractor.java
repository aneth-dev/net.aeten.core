package net.aeten.core.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class JarExtractor {

	public static File extract (	Class <?> loadinClass,
											String resourceRelativePath) {
		return extract (loadinClass, resourceRelativePath, getTempDir (loadinClass.getName ()));
	}

	public static File extract (	Class <?> loadinClass,
											String resourceRelativePath,
											File outputDirectory) {

		URL url = loadinClass.getResource (resourceRelativePath);
		if (url == null) {
			throw new UnsatisfiedLinkError ("JarExtractor (" + resourceRelativePath + ") not found in resource path");
		}

		File file = null;
		if (url.getProtocol ().toLowerCase ().equals ("file")) {
			try {
				file = new File (new URI (url.toString ()));
			} catch (URISyntaxException e) {
				file = new File (url.getPath ());
			}
			if (!file.exists ()) {
				throw new Error ("File URL " + url + " could not be properly decoded");
			}
		} else {
			InputStream is = loadinClass.getResourceAsStream (resourceRelativePath);
			if (is == null) {
				throw new Error ("Can't obtain InputStream (" + resourceRelativePath + ")");
			}

			FileOutputStream fos = null;
			try {
				/* Suffix is required on windows, or library fails to load Let Java pick the suffix,
				 * except on windows, to avoid problems with Web Start.
				 */
				file = new File (outputDirectory, resourceRelativePath);
				if (file.exists ()) {
					return file;
				}
				file.getParentFile ().mkdirs ();
				fos = new FileOutputStream (file);
				int count;
				byte[] buf = new byte[1024];
				while ((count = is.read (buf, 0, buf.length)) > 0) {
					fos.write (buf, 0, count);
				}
			} catch (IOException exception) {
				throw new Error ("Failed to extract file for", exception);
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
		return file;
	}

	static File getTempDir (String fileName) {
		File tmpdir = new File (System.getProperty ("java.io.tmpdir"));
		File filetmp = new File (tmpdir, fileName + "-" + System.getProperty ("user.name"));
		filetmp.mkdirs ();
		return filetmp.exists ()? filetmp: tmpdir;
	}

}
