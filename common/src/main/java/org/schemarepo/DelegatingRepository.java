package org.schemarepo;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by ernest.mishkin on 12/5/14.
 */
public abstract class DelegatingRepository extends BaseRepository {

  protected final Repository repo;

  protected DelegatingRepository(final Repository delegate) {
    if (delegate == null) {
      throw new IllegalArgumentException("Delegate repository required");
    }
    this.repo = delegate;
    logger.info("Constructed {}", this);
    logger.info("Wrapping {}", repo);
  }

  @Override
  protected void isValid() {
    if (repo instanceof BaseRepository) {
      ((BaseRepository)repo).isValid();
    }
  }

  @Override
  public Subject register(final String subjectName, final SubjectConfig config) {
    return repo.register(subjectName, config);
  }

  @Override
  public Subject lookup(final String subjectName) {
    return repo.lookup(subjectName);
  }

  @Override
  public Iterable<Subject> subjects() {
    return repo.subjects();
  }

  @Override
  public String getStatus() {
    return repo.getStatus();
  }

  @Override
  public Properties getConfiguration(boolean includeDefaults) {
    return repo.getConfiguration(includeDefaults);
  }

  @Override
  public void close() throws IOException {
    repo.close();
    super.close();
  }

}
