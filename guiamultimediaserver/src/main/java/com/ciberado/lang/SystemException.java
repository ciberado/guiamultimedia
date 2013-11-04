/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ciberado.lang;

/**
 *
 * @author ciberado
 */
public class SystemException extends RuntimeException {

    public SystemException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public SystemException(Throwable cause) {
        super(cause);
    }

    public SystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public SystemException(String message) {
        super(message);
    }

    public SystemException() {
    }
    
}
