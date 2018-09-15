package com.mewna.catnip.entity;

/**
 * @author natanbc
 * @since 9/14/18
 */
public interface VoiceRegion extends Entity {
    String id();
    
    String name();
    
    boolean vip();
    
    boolean optimal();
    
    boolean deprecated();
    
    boolean custom();
}
