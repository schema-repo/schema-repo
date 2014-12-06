package org.schemarepo;

/**
 * Created by ernest.mishkin on 12/5/14.
 */
public abstract class AbstractSubjectCachingValidatingRepository extends BaseRepository {

  protected final InMemorySubjectCache subjects = new InMemorySubjectCache();
  protected final ValidatorFactory validators;

  protected AbstractSubjectCachingValidatingRepository(final ValidatorFactory validators) {
    this.validators = validators != null ? validators : ValidatorFactory.EMPTY;
  }

  protected abstract Subject createSubject(final String subjectName, final SubjectConfig config);

  protected Subject createAndCacheSubject(final String subjectName, final SubjectConfig config) {
    final Subject subject = createSubject(subjectName, config);
    final Subject cachedSubject = subjects.add(Subject.validatingSubject(subject, validators));
    if (cachedSubject == subject) {
      logger.debug("Created subject {}", subjectName);
    } else {
      logger.debug("Subject {} already exists, reusing", subjectName);
    }
    return cachedSubject;
  }

  @Override
  public synchronized Subject register(final String subjectName, final SubjectConfig config) {
    isValid();
    Subject subject = subjects.lookup(subjectName);
    if (null == subject) {
      subject = createAndCacheSubject(subjectName, config);
    } else {
      logger.debug("Subject {} already exists, reusing", subjectName);
    }
    return subject;
  }

  @Override
  public synchronized Subject lookup(final String subjectName) {
    isValid();
    return subjects.lookup(subjectName);
  }

  @Override
  public synchronized Iterable<Subject> subjects() {
    isValid();
    return subjects.values();
  }


}
