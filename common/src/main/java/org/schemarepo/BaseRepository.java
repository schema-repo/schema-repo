package org.schemarepo;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ernest.mishkin on 12/5/14.
 */
public abstract class BaseRepository implements Repository {

  protected static final String ACTIVE_STATUS = "ACTIVE";

  protected Logger logger = LoggerFactory.getLogger(getClass());

  protected boolean closed;

  /**
   * Asserts the repository is in a valid state (for ex. not closed).
   * @throws java.lang.IllegalStateException if the repository is NOT in a valid state
   */
  protected void isValid() {
    if (closed) {
      throw new IllegalStateException("LocalFileSystemRepository is closed");
    }
  }

  @Override
  public void close() throws IOException {
    logger.info("Closing {}", this);
  }

  @Override
  public String getStatus() {
    return ACTIVE_STATUS;
  }

  @Override
  public Properties getConfiguration(boolean includeDefaults) {
    return null;
  }

}
