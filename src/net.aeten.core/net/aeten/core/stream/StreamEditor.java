/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.aeten.core.stream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Thomas Pérennou
 */
public interface StreamEditor<In extends InputStream, Out extends OutputStream> {
	public void edit (In in,
							Out out) throws IOException;
}
