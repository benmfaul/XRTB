// Copyright OpenX Limited 2010. All Rights Reserved.
package com.xrtb.exchanges.openx;

/**
 * An exception class for OpenX encryption exceptions
 */

public class SsRtbDecryptingException extends Exception {
  private static final long serialVersionUID = 1L;

  public SsRtbDecryptingException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
