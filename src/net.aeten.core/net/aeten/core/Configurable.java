/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.aeten.core;

/**
 *
 * @author thomas
 */
public interface Configurable<T> {
    public void configure(T configuration) throws ConfigurationException;
}
