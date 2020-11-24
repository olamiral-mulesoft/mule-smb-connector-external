/**
 * (c) 2003-2020 MuleSoft, Inc. The software in this package is published under the terms of the Commercial Free Software license V.1 a copy of which has been included with this distribution in the LICENSE.md file.
 */
package org.mule.extension.smb;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mule.extension.smb.AllureConstants.SmbFeature.SMB_EXTENSION;
import static org.mule.tck.probe.PollingProber.check;

import org.mule.extension.file.common.api.FileAttributes;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.api.message.Message;
import org.mule.runtime.api.util.Reference;
import org.mule.runtime.core.api.event.CoreEvent;
import org.mule.runtime.core.api.processor.Processor;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import io.qameta.allure.Feature;
import org.junit.Test;

@Feature(SMB_EXTENSION)
public class SmbDirectoryListenerReconnectionFunctionalTestCase extends CommonSmbConnectorTestCase {

  private static final String MATCHERLESS_LISTENER_FOLDER_NAME = "matcherless";
  private static final String WATCH_FILE = "watchme.txt";
  private static final String WATCH_CONTENT = "who watches the watchmen?";
  private static final int PROBER_TIMEOUT = 10000;
  private static final int PROBER_DELAY = 1000;

  private static List<Message> RECEIVED_MESSAGES;

  public static class TestProcessor implements Processor {

    @Override
    public CoreEvent process(CoreEvent event) throws MuleException {
      RECEIVED_MESSAGES.add(event.getMessage());
      return event;
    }
  }

  public SmbDirectoryListenerReconnectionFunctionalTestCase(String name, SmbTestHarness testHarness, String smbConfigFile) {
    super(name, testHarness, smbConfigFile);
  }

  @Override
  protected String getConfigFile() {
    return "smb-directory-listener-reconnection-config.xml";
  }

  @Override
  protected void doSetUpBeforeMuleContextCreation() throws Exception {
    super.doSetUpBeforeMuleContextCreation();
    testHarness.makeDir(MATCHERLESS_LISTENER_FOLDER_NAME);

    RECEIVED_MESSAGES = new CopyOnWriteArrayList<>();
  }

  @Test
  public void testListenerReadsFilesAfterReconnection() throws Exception {
    URI file = new URI(MATCHERLESS_LISTENER_FOLDER_NAME + "/" + WATCH_FILE);
    testHarness.write(file.getPath(), WATCH_CONTENT);
    assertPoll(file, WATCH_CONTENT);

    testHarness.getSmbServer().stop();

    Thread.sleep(50);
    RECEIVED_MESSAGES.clear();
    testHarness.setUpServer();

    assertPoll(file, WATCH_CONTENT);
  }

  private void assertPoll(URI file, Object expectedContent) {
    Message message = expect(file);
    String payload = toString(message.getPayload().getValue());
    assertThat(payload, equalTo(expectedContent));
  }

  private Message expect(URI file) {
    Reference<Message> messageHolder = new Reference<>();
    check(PROBER_TIMEOUT, PROBER_DELAY, () -> {
      getPicked(file).ifPresent(messageHolder::set);
      return messageHolder.get() != null;
    });

    return messageHolder.get();
  }

  private Optional<Message> getPicked(URI file) {
    return RECEIVED_MESSAGES.stream()
        .filter(message -> {
          FileAttributes attributes = (FileAttributes) message.getAttributes().getValue();
          return attributes.getPath().contains(file.getPath());
        })
        .findFirst();
  }

}

