package models;

import org.codehaus.jackson.node.ObjectNode;

/**
 * Classes that can be 'serialized' to Json.
 * 
 * @author marcus
 */
public interface JSonable {
    
    public ObjectNode toObjectNode();

}
