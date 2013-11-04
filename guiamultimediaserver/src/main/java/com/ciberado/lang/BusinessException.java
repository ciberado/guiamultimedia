/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ciberado.lang;

/**
 *
 * @author ciberado
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public BusinessException(Throwable cause) {
        super(cause);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException() {
    }
    
}
