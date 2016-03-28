/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *    
 * Linking this library statically or dynamically with other modules 
 * is making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole
 * combination.
 *    
 * As a special exception, the copyright holders of this library give 
 * you permission to link this library with independent modules to 
 * produce an executable, regardless of the license terms of these 
 * independent modules, and to copy and distribute the resulting 
 * executable under terms of your choice, provided that you also meet, 
 * for each linked independent module, the terms and conditions of the 
 * license of that module.  An independent module is a module which 
 * is not derived from or based on this library.  If you modify this 
 * library, you may extend this exception to your version of the 
 * library, but you are not obligated to do so.  If you do not wish 
 * to do so, delete this exception statement from your version.
 *
 * Project: github.com/rickyepoderi/wbxml-stream
 * 
 */
package es.rickyepoderi.wbxml.definition;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * 
 * <p>Java representation for the IANA charset. The charset in Java does not
 * have the MIB number so it is needed a class that joins Java Charset 
 * with IANA one.</p>
 * 
 * <p>The IANA charset as defined in the following 
 * <a href="http://www.iana.org/assignments/character-sets">link</a>.
 * Not all the charsets in IANA are defined in Java, the following link lists
 * all <a href="http://docs.oracle.com/javase/1.5.0/docs/guide/intl/encoding.doc.html">the charsets defined in JavaSe (version 5)</a>
 * </p>
 * 
 * <p>Not all the charsets are defined here but the idea is simple, just add 
 * the ones you need. ;-)</p>
 *
 * @author ricky
 */
public enum IanaCharset {
    UNKNOWN("UNKNOWN", 0, new String[] {}),
    ANSI_X3_4_1968("ANSI_X3.4-1968", 3L, new String[] {"US-ASCII", "iso-ir-6", "ANSI_X3.4-1986", "ISO_646.irv:1991", "ASCII", "ISO646-US", "us", "IBM367", "cp367", "csASCII"}),
    ISO_8859_1_1987("ISO_8859-1:1987", 4L, new String[] {"ISO-8859-1", "iso-ir-100", "ISO_8859-1", "latin1", "l1", "IBM819", "CP819", "csISOLatin1"}),
    ISO_8859_2_1987("ISO_8859-2:1987", 5L, new String[] {"ISO-8859-2", "iso-ir-101", "ISO_8859-2", "latin2", "l2", "csISOLatin2"}),
    ISO_8859_3_1988("ISO_8859-3:1988", 6L, new String[] {"ISO-8859-3", "iso-ir-109", "ISO_8859-3", "latin3", "l3", "csISOLatin3"}),
    ISO_8859_4_1988("ISO_8859-4:1988", 7L, new String[] {"ISO-8859-4", "iso-ir-110", "ISO_8859-4", "latin4", "l4", "csISOLatin4"}),
    ISO_8859_5_1988("ISO_8859-5:1988", 8L, new String[] {"ISO-8859-5"," iso-ir-144", "ISO_8859-5", "cyrillic", "csISOLatinCyrillic"}),
    ISO_8859_6_1987("ISO_8859-6:1987", 9L, new String[] {"ISO-8859-6", "iso-ir-127", "ISO_8859-6", "ECMA-114", "ASMO-708", "arabic", "csISOLatinArabic"}),
    ISO_8859_7_1987("ISO_8859-7:1987", 10L, new String[] {"ISO-8859-7", "iso-ir-126", "ISO_8859-7", "ELOT_928", "ECMA-118", "greek", "greek8", "csISOLatinGreek"}),
    ISO_8859_8_1988("ISO_8859-8:1988", 11L, new String[] {"ISO-8859-8", "iso-ir-138", "ISO_8859-8", "hebrew", "csISOLatinHebrew"}),
    ISO_8859_9_1989("ISO_8859-9:1989", 12L, new String[]{"ISO-8859-9", "iso-ir-148", "ISO_8859-9", "latin5", "l5", "csISOLatin5"}),
    ISO_8859_10("ISO-8859-10", 13L, new String[]{"iso-ir-157", "l6", "ISO_8859-10:1992", "csISOLatin6", "latin6"}),
    Shift_JIS("Shift_JIS", 17L, new String[] {"MS_Kanji", "csShiftJIS"}),
    UTF_8("UTF-8", 106L, new String[] {}),
    Big5("Big5", 2026L, new String[] {"csBig5"}),
    ISO_10646_UCS_2("ISO-10646-UCS-2", 1000L, new String[] {"csUnicode"}),
    UTF_16("UTF-16", 1015L, new String[] {});
    
    /**
     * Logger for the class
     */
    protected static final Logger log = Logger.getLogger(IanaCharset.class.getName());
    
    /**
     * Name of the charset.
     */
    private String name = null;
    
    /**
     * mib identifier of the IANA charset.
     */
    private long mibEnum = 0L;
    
    /**
     * Alias of the charset.
     */
    private String[] alias;
    
    /**
     * Java equivalent charset if defined (it defaults to ASCII).
     */
    private Charset charset;
    
    /**
     * static map to search a charset using the name or alias.
     */
    static private final Map<String, IanaCharset> charsetAliasMap = new HashMap<String, IanaCharset>();
    
    /**
     * static map to search the charset using the mib identifier.
     */
    static private final Map<Long, IanaCharset> charsetMibMap = new HashMap<Long, IanaCharset>();
    
    /**
     * Provate method for the enumeration.
     * @param name The name of the charset
     * @param mibEnum The mib identifier
     * @param alias The list of alias names
     */
    private IanaCharset(String name, long mibEnum, String[] alias) {
        this.name = name;
        this.mibEnum = mibEnum;
        this.alias = alias;
        this.charset = null;
    }

    /**
     * Getter for the alias array.
     * @return The alias of the charset
     */
    public String[] getAlias() {
        return alias;
    }

    /**
     * Getter for the MIB identifier.
     * @return The MIB id
     */
    public long getMibEnum() {
        return mibEnum;
    }

    /**
     * Getter for the charset name.
     * @return The name of the charset
     */
    public String getName() {
        return name;
    }
    
    /**
     * Statuc method to get a charset using the name or any alias.
     * @param alias The name or the alias of the charset to retrieve.
     * @return The associated charset or UNKNOWN.
     */
    static public IanaCharset getIanaCharset(String alias) {
        IanaCharset charset = charsetAliasMap.get(alias);
        if (charset == null) {
            charset = UNKNOWN;
        }
        return charset;
    }
    
    /**
     * Static method to get a charset using the MIB.
     * @param mib The MIB identifier of teh charset
     * @return The charset associated to this mIB or UNKNOWN
     */
    static public IanaCharset getIanaCharset(long mib) {
        IanaCharset charset = charsetMibMap.get(mib);
        if (charset == null) {
            charset = UNKNOWN;
        }
        return charset;
    }
    
    /**
     * Method to return a Java charset using the name. It is used to 
     * catch the associated exception if the charset is not defined in Java.
     * @param name The name of the charset
     * @return The Java charset or null
     */
    static protected Charset getCharsetName(String name) {
        try {
            return Charset.forName(name);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Method that search the Java associated Charset to the IANA one. 
     * This method tries to locate the Java charset using the name and all the
     * alias of the charset, if found that Java Charset is returned, if not, it
     * defaults to ASCII.
     * @return The Java associated charset
     */
    public Charset getCharset() {
        if (charset == null) {
            charset = getCharsetName(this.name);
            if (charset == null) {
                for (String a : this.alias) {
                    charset = getCharsetName(a);
                    if (charset != null) {
                        break;
                    }
                }
            }
            if (charset == null) {
                // defaults to ASCII
                if (mibEnum != 0) {
                    log.log(Level.WARNING, "Iana charset '{0}' has no Java equivalence", this.toString());
                }
                charset = getCharsetName("ASCII");
            }
        }
        return charset;
    }
    
    static {
        for (IanaCharset c: IanaCharset.values()) {
            charsetMibMap.put(c.getMibEnum(), c);
            charsetAliasMap.put(c.getName(), c);
            for (String alias: c.alias) {
                charsetAliasMap.put(alias, c);
            }
        }
    }
}
