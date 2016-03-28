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

/**
 *
 * <p>WBXML says nothing about namespaces but the languages uses them a lot
 * (for example SyncML). The idea is any language can or cannot define
 * namespaces and if it uses them all the tags and attributes must be 
 * prefixed by the namespace prefix.</p>
 * 
 * <p>In the definition properties file a namespace is defined
 * in a single key:<p>/
 * 
 * <ul>
 * <li>wbxml.namespaces.{prefix}={namespaceURI}</li>
 * </ul>
 * 
 * <p>All the rest of elements that are namespace aware (tags and attributes)
 * should be prefixed if namespaces are used in the language. The definition
 * object is just a pair prefix-namespaceURI</p>
 * 
 * @author ricky
 */
public class WbXmlNamespaceDef {
    
    /**
     * The prefix of the namespece
     */
    private String prefix = null;
    
    /**
     * The namespaceURI
     */
    private String namespace = null;
    
    /**
     * Constructor via both properties: prefix and namespacveURI
     * @param prefix The prefix
     * @param namespace The namespace URI
     */
    protected WbXmlNamespaceDef(String prefix, String namespace) {
        this.prefix = prefix;
        this.namespace = namespace;
    }
    
    /**
     * Getter for the prefix
     * @return The prefix of the namespace definition
     */
    public String getPrefix() {
        return this.prefix;
    }

    /**
     * Setter for the prefix
     * @param prefix The new prefix
     */
    protected void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Getter for the namespace URI
     * @return The namespace URI
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Setter for the namespace URI
     * @param namespace The new namespace URI
     */
    protected void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    
    /**
     * String representation
     * @return The string representation
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.getClass().getName());
        sb.append(System.getProperty("line.separator"));
        sb.append("prefix: ");
        sb.append(prefix);
        sb.append(System.getProperty("line.separator"));
        sb.append("namespace: ");
        sb.append(namespace);
        sb.append(System.getProperty("line.separator"));
        return sb.toString();
    }
}
