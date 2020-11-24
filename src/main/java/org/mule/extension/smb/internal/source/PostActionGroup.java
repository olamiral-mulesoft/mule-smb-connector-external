/**
 * (c) 2003-2020 MuleSoft, Inc. The software in this package is published under the terms of the Commercial Free Software license V.1 a copy of which has been included with this distribution in the LICENSE.md file.
 */
package org.mule.extension.smb.internal.source;

/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

import static org.mule.runtime.api.meta.model.display.PathModel.Location.EXTERNAL;
import static org.mule.runtime.api.meta.model.display.PathModel.Type.DIRECTORY;

import org.mule.extension.file.common.api.source.AbstractPostActionGroup;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.Path;

/**
 * Groups post processing action parameters
 *
 * @since 1.1
 */
public class PostActionGroup extends AbstractPostActionGroup {

  /**
   * Whether each file should be deleted after processing or not
   */
  @Parameter
  @Optional(defaultValue = "false")
  private boolean autoDelete = false;

  /**
   * If provided, each processed file will be moved to a directory pointed by this path.
   */
  @Parameter
  @Optional
  @Path(type = DIRECTORY, location = EXTERNAL)
  private String moveToDirectory;

  /**
   * This parameter works in tandem with {@code moveToDirectory}. Use this parameter to enter the name under which the file should
   * be moved. Do not set this parameter if {@code moveToDirectory} hasn't been set as well.
   */
  @Parameter
  @Optional
  private String renameTo;

  /**
   * Whether any of the post actions ({@code autoDelete} and {@code moveToDirectory}) should also be applied in case the file
   * failed to be processed. If set to {@code false}, no failed files will be moved nor deleted.
   */
  @Parameter
  @Optional(defaultValue = "true")
  private boolean applyPostActionWhenFailed = true;


  public PostActionGroup() {}

  public PostActionGroup(boolean autoDelete, String moveToDirectory, String renameTo, boolean applyPostActionWhenFailed) {
    this.autoDelete = autoDelete;
    this.moveToDirectory = moveToDirectory;
    this.renameTo = renameTo;
    this.applyPostActionWhenFailed = applyPostActionWhenFailed;
  }

  public boolean isAutoDelete() {
    return autoDelete;
  }

  public String getMoveToDirectory() {
    return moveToDirectory;
  }

  public String getRenameTo() {
    return renameTo;
  }

  public boolean isApplyPostActionWhenFailed() {
    return applyPostActionWhenFailed;
  }

}
