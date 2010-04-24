/*
 * Copyright 2003 - 2010 The eFaps Team
 * Copyright 2007 Matthias L. Jugel.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.webdav4vfs.data;

import org.apache.commons.vfs.FileObject;
import org.dom4j.Element;

/**
 * A DAV collection is similar to a directory.
 *
 * @author Matthias L. Jugel
 * @version $Id$
 */
public class DavCollection
    extends DavResource
{
  public static final String COLLECTION = "collection";

  public DavCollection(final FileObject object)
  {
      super(object);
  }

    @Override()
    protected boolean addResourceTypeProperty(final Element root,
                                              final boolean ignoreValue)
    {
        root.addElement(PROP_RESOURCETYPE).addElement(COLLECTION);
        return true;
    }

  /**
   * Ignore content language for collections.
   *
   * @param root the prop element to add to
   * @return true, even though nothing is added
   */
  @Override
protected boolean addGetContentLanguageProperty(Element root, boolean ignoreValue) {
    return true;
  }

  /**
   * Ignore content length for collections.
   *
   * @param root the prop element to add to
   * @return true, even though nothing is added
   */
  @Override
protected boolean addGetContentLengthProperty(Element root, boolean ignoreValue) {
    return true;
  }

  /**
   * Ignore content type for collections.
   *
   * @param root the prop element to add to
   * @return true, even though nothing is added
   */
  @Override
protected boolean addGetContentTypeProperty(Element root, boolean ignoreValue) {
    return true;
  }

  @Override
protected boolean addQuotaProperty(Element root, boolean ignoreValue) {
    root.addElement(PROP_QUOTA).addText("" + Long.MAX_VALUE);
    return true;
  }

  @Override
protected boolean addQuotaUsedProperty(Element root, boolean ignoreValue) {
    // TODO add correct handling of used quota
    root.addElement(PROP_QUOTA_USED).addText("0");
    return true;
  }

  @Override
protected boolean addQuotaAvailableBytesProperty(Element root, boolean ignoreValue) {
    // TODO add correct handling of available quota
    root.addElement(PROP_QUOTA_AVAILABLE_BYTES).addText(Long.toString(Long.MAX_VALUE));
    return true;
  }

  @Override
protected boolean addQuotaUsedBytesProperty(Element root, boolean ignoreValue) {
    // TODO add correct handling of used quota
    root.addElement(PROP_QUOTA_USED_BYTES).addText("0");
    return true;
  }
}
