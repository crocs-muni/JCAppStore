/*
 * Jacareto Copyright (c) 2002-2005
 * Applied Computer Science Research Group, Darmstadt University of
 * Technology, Institute of Mathematics & Computer Science,
 * Ludwigsburg University of Education, and Computer Based
 * Learning Research Group, Aachen University. All rights reserved.
 *
 * Jacareto is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * Jacareto is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with Jacareto; if not, write to the Free
 * Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

package cz.muni.crocs.appletstore.util;


import org.slf4j.Logger;
import org.slf4j.event.Level;

import java.io.IOException;
import java.io.OutputStream;

/**
 * This class logs all bytes written to it as output stream with a specified logging level.
 *
 * @author <a href="mailto:cspannagel@web.de">Christian Spannagel</a>
 *          !! modified !! by Jiří Horák for self4j usage
 *
 * @version 1.0
 */
public class LogOutputStream extends OutputStream {
    /** The logger where to log the written bytes. */
    private Logger logger;

    /** The level. */
    private Level level;

    /** The internal memory for the written bytes. */
    private String mem;

    /**
     * Creates a new log output stream which logs bytes to the specified logger with the specified
     * level.
     *
     * @param logger the logger where to log the written bytes
     * @param level the level
     */
    public LogOutputStream (Logger logger, Level level) {
        setLogger (logger);
        setLevel (level);
        mem = "";
    }

    /**
     * Sets the logger where to log the bytes.
     *
     * @param logger the logger
     */
    public void setLogger (Logger logger) {
        this.logger = logger;
    }

    /**
     * Returns the logger.
     *
     * @return DOCUMENT ME!
     */
    public Logger getLogger () {
        return logger;
    }

    /**
     * Sets the logging level.
     *
     * @param level DOCUMENT ME!
     */
    public void setLevel (Level level) {
        this.level = level;
    }

    /**
     * Returns the logging level.
     *
     * @return DOCUMENT ME!
     */
    public Level getLevel () {
        return level;
    }

    /**
     * Writes a byte to the output stream. This method flushes automatically at the end of a line.
     *
     * @param b DOCUMENT ME!
     */
    public void write (int b) {
        byte[] bytes = new byte[1];
        bytes[0] = (byte) (b & 0xff);
        mem = mem + new String(bytes);

        if (mem.endsWith ("\n")) {
            mem = mem.substring (0, mem.length () - 1);
            flush ();
        }
    }

    /**
     * Flushes the output stream.
     */
    public void flush () {
        switch (level) {
            case INFO:
                logger.info(mem);
                break;
            case WARN:
                logger.warn(mem);
                break;
            case DEBUG:
                logger.debug(mem);
                break;
            case ERROR:
                logger.error(mem);
                break;
            case TRACE:
                logger.trace(mem);
                break;
            default:
        }
        mem = "";
    }

    @Override
    public void close() throws IOException {
        //do nothing
    }
}