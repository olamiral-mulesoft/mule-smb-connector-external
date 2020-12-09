/**
 * (c) 2003-2020 MuleSoft, Inc. The software in this package is published under the terms of the Commercial Free Software license V.1 a copy of which has been included with this distribution in the LICENSE.md file.
 */
package org.mule.extension.smb;

import static org.mockito.Mockito.when;
import static org.mule.extension.file.common.api.matcher.MatchPolicy.INCLUDE;
import static org.mule.extension.file.common.api.matcher.MatchPolicy.REQUIRE;
import static org.mule.extension.smb.AllureConstants.SmbFeature.SMB_EXTENSION;
import org.mule.extension.smb.api.SmbFileAttributes;
import org.mule.extension.smb.api.SmbFileMatcher;
import org.mule.test.extension.file.common.FileMatcherContractTestCase;

import java.time.LocalDateTime;

import io.qameta.allure.Feature;
import org.junit.Before;
import org.junit.Test;

@Feature(SMB_EXTENSION)
public class SmbFileMatcherTestCase
    extends FileMatcherContractTestCase<SmbFileMatcher, SmbFileAttributes> {

  private static final LocalDateTime TIMESTAMP = LocalDateTime.of(1983, 4, 20, 21, 15);

  @Override
  protected SmbFileMatcher createPredicateBuilder() {
    return new SmbFileMatcher();
  }

  @Override
  protected Class<SmbFileAttributes> getFileAttributesClass() {
    return SmbFileAttributes.class;
  }

  @Before
  @Override
  public void before() {
    super.before();
    when(attributes.getLastModified()).thenReturn(TIMESTAMP);
  }

  @Test
  public void matchesAll() {
    builder.setFilenamePattern("glob:*.{java, js}").setPathPattern("glob:**.{java, js}")
        .setTimestampSince(LocalDateTime.of(1980, 1, 1, 0, 0))
        .setTimestampUntil(LocalDateTime.of(1990, 1, 1, 0, 0))
        .setRegularFiles(REQUIRE)
        .setDirectories(INCLUDE)
        .setSymLinks(INCLUDE)
        .setMinSize(1L)
        .setMaxSize(1024L);

    assertMatch();
  }

  @Test
  public void timestampSince() {
    builder.setTimestampSince(LocalDateTime.of(1980, 1, 1, 0, 0));
    assertMatch();
  }

  @Test
  public void timestampUntil() {
    builder.setTimestampUntil(LocalDateTime.of(1990, 1, 1, 0, 0));
    assertMatch();
  }

  @Test
  public void rejectTimestampSince() {
    builder.setTimestampSince(LocalDateTime.of(1984, 1, 1, 0, 0));
    assertReject();
  }

  @Test
  public void rejectTimestampUntil() {
    builder.setTimestampUntil(LocalDateTime.of(1982, 4, 2, 0, 0));
    assertReject();
  }
}
