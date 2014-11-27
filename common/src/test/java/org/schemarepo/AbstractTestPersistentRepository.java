package org.schemarepo;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * This extension of {@link AbstractTestRepository} includes tests that close
 * a schema repository and re-open it, to make sure persistence works correctly.
 */
public abstract class AbstractTestPersistentRepository<R extends Repository>
        extends AbstractTestRepository<R> {
  @Test
  public void testWriteCloseRead() throws SchemaValidationException, IOException {
    try {
      repo.register("sub1", null).register("sc1");
      repo.register("sub2", null).register("sc2");
      repo.register("sub2", null).register("sc3");
    } finally {
      repo.close();
    }
    // Calling close() and createRepository() is like bouncing the repo, to ensure its state persists.
    repo = createRepository();
    try {
      Subject s1 = repo.lookup("sub1");
      Assert.assertNotNull(s1);
      Subject s2 = repo.lookup("sub2");
      Assert.assertNotNull(s2);

      SchemaEntry e1 = s1.lookupBySchema("sc1");
      Assert.assertNotNull(e1);
      Assert.assertEquals("sc1", e1.getSchema());

      SchemaEntry e2 = s2.lookupBySchema("sc2");
      Assert.assertNotNull(e2);
      Assert.assertEquals("sc2", e2.getSchema());

      SchemaEntry e3 = s2.lookupBySchema("sc3");
      Assert.assertNotNull(e3);
      Assert.assertEquals("sc3", e3.getSchema());
    } finally {
      repo.close();
    }
  }

  @Test
  public void testWriteCloseReadMultiLineSchema() throws SchemaValidationException, IOException {
    String endOfLine = System.getProperty("line.separator");

    String multiLineSchema1 = "first line" + endOfLine + "second line";
    String multiLineSchema2 = "first line" + endOfLine + "second line" + endOfLine;

    try {
      repo.register("sub1", null).register(multiLineSchema1);
      repo.register("sub1", null).register(multiLineSchema2);
    } finally {
      repo.close();
    }
    // Calling close() and createRepository() is like bouncing the repo, to ensure its state persists.
    repo = createRepository();
    try {
      Subject s1 = repo.lookup("sub1");
      Assert.assertNotNull(s1);

      SchemaEntry schemaEntry1ById = s1.lookupById("0");
      Assert.assertNotNull(schemaEntry1ById);
      Assert.assertEquals(multiLineSchema1, schemaEntry1ById.getSchema());

      SchemaEntry schemaEntry1BySchema = s1.lookupBySchema(multiLineSchema1);
      Assert.assertNotNull(schemaEntry1BySchema);
      Assert.assertEquals(multiLineSchema1, schemaEntry1BySchema.getSchema());

      SchemaEntry schemaEntry2ById = s1.lookupById("1");
      Assert.assertNotNull(schemaEntry2ById);
      Assert.assertEquals(multiLineSchema2, schemaEntry2ById.getSchema());

      SchemaEntry schemaEntry2BySchema = s1.lookupBySchema(multiLineSchema2);
      Assert.assertNotNull(schemaEntry2BySchema);
      Assert.assertEquals(multiLineSchema2, schemaEntry2BySchema.getSchema());

    } finally {
      repo.close();
    }
  }
}
