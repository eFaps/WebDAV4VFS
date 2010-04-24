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

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.efaps.webdav4vfs.lock.Lock;
import org.efaps.webdav4vfs.lock.LockManager;
import org.efaps.webdav4vfs.util.Util;


/**
 * @author Matthias L. Jugel
 * @version $Id$
 */
public class DavResource
    extends AbstractDavResource
{

    public DavResource(final FileObject object)
    {
        super(object);
    }

    @Override()
    protected boolean setPropertyValue(Element root, Element propertyEl)
    {
        LogFactory.getLog(getClass()).debug(String.format("[%s].set('%s')", object.getName(), propertyEl.asXML()));

        if (!ALL_PROPERTIES.contains(propertyEl.getName())) {
          final String nameSpace = propertyEl.getNamespaceURI();
          final String attributeName = getFQName(nameSpace, propertyEl.getName());
          try {
            FileContent objectContent = object.getContent();
            final String command = propertyEl.getParent().getParent().getName();
            if (TAG_PROP_SET.equals(command)) {
              StringWriter propertyValueWriter = new StringWriter();
              propertyEl.write(propertyValueWriter);
              propertyValueWriter.close();
              objectContent.setAttribute(attributeName, propertyValueWriter.getBuffer().toString());
            } else if (TAG_PROP_REMOVE.equals(command)) {
              objectContent.setAttribute(attributeName, null);
            }
            root.addElement(propertyEl.getQName());
            return true;
          } catch (IOException e) {
            LogFactory.getLog(getClass()).error(String.format("can't store attribute property '%s' = '%s'",
                                                              attributeName,
                                                              propertyEl.asXML()), e);
          }
        }
        return false;
    }

    /**
     * Add the value for a given property to the result document. If the value
     * is missing or can not be added for some reason it will return false to
     * indicate a missing property.
     *
     * @param _root         root element for the result document fragment
     * @param _propertyName property name to query
     * @param _ignoreValue  <i>true</i> if values must be ignored
     * @return true for successful addition and false for missing data
     */
    @Override()
    protected boolean getPropertyValue(final Element _root,
                                       final String _propertyName,
                                       final boolean ignoreValue)
    {
        LogFactory.getLog(getClass()).debug(String.format("[%s].get('%s')", object.getName(), _propertyName));
        if (PROP_CREATION_DATE.equals(_propertyName)) {
          return addCreationDateProperty(_root, ignoreValue);
        } else if (PROP_DISPLAY_NAME.equals(_propertyName)) {
          return addGetDisplayNameProperty(_root, ignoreValue);
        } else if (PROP_GET_CONTENT_LANGUAGE.equals(_propertyName)) {
          return addGetContentLanguageProperty(_root, ignoreValue);
        } else if (PROP_GET_CONTENT_LENGTH.equals(_propertyName)) {
          return addGetContentLengthProperty(_root, ignoreValue);
        } else if (PROP_GET_CONTENT_TYPE.equals(_propertyName)) {
          return addGetContentTypeProperty(_root, ignoreValue);
        } else if (PROP_GET_ETAG.equals(_propertyName)) {
          return addGetETagProperty(_root, ignoreValue);
        } else if (PROP_GET_LAST_MODIFIED.equals(_propertyName)) {
          return addGetLastModifiedProperty(_root, ignoreValue);
        } else if (PROP_LOCK_DISCOVERY.equals(_propertyName)) {
          return addLockDiscoveryProperty(_root, ignoreValue);
        } else if (PROP_RESOURCETYPE.equals(_propertyName)) {
          return addResourceTypeProperty(_root, ignoreValue);
        } else if (PROP_SOURCE.equals(_propertyName)) {
          return addSourceProperty(_root, ignoreValue);
        } else if (PROP_SUPPORTED_LOCK.equals(_propertyName)) {
          return addSupportedLockProperty(_root, ignoreValue);
        } else {
          // handle non-standard properties (keep a little separate)
          if (PROP_QUOTA.equals(_propertyName)) {
            return addQuotaProperty(_root, ignoreValue);
          } else if (PROP_QUOTA_USED.equals(_propertyName)) {
            return addQuotaUsedProperty(_root, ignoreValue);
          } else if (PROP_QUOTA_AVAILABLE_BYTES.equals(_propertyName)) {
            return addQuotaAvailableBytesProperty(_root, ignoreValue);
          } else if (PROP_QUOTA_USED_BYTES.equals(_propertyName)) {
            return addQuotaUsedBytesProperty(_root, ignoreValue);
          } else {
            try {
              Object propertyValue = object.getContent().getAttribute(_propertyName);
              if (null != propertyValue) {
                if (((String) propertyValue).startsWith("<")) {
                  try {
                    Document property = DocumentHelper.parseText((String) propertyValue);
                    if (ignoreValue) {
                      property.clearContent();
                    }
                    _root.add(property.getRootElement().detach());
                    return true;
                  } catch (DocumentException e) {
                    LogFactory.getLog(getClass()).error("property value unparsable", e);
                    return false;
                  }
                } else {
                  Element el = _root.addElement(_propertyName);
                  if (!ignoreValue) {
                    el.addText((String) propertyValue);
                  }
                  return true;
                }

              }
            } catch (FileSystemException e) {
              LogFactory.getLog(this.getClass()).error(String.format("property '%s' is not supported", _propertyName), e);
            }
          }
        }
        return false;
    }

    protected boolean addCreationDateProperty(final Element root,
                                              final boolean ignoreValue)
    {
        return false;
    }

    protected boolean addGetDisplayNameProperty(Element root, boolean ignoreValue)
    {
        Element el = root.addElement(PROP_DISPLAY_NAME);
        if (!ignoreValue) {
            el.addCDATA(object.getName().getBaseName());
        }
        return true;
    }

    protected boolean addGetContentLanguageProperty(final Element root,
                                                    final boolean ignoreValue)
    {
        return false;
    }

    protected boolean addGetContentLengthProperty(final Element _root,
                                                  final boolean _ignoreValue)
    {
        try {
            final Element el = _root.addElement(PROP_GET_CONTENT_LENGTH);
            if (!_ignoreValue) {
                el.addText("" + object.getContent().getSize());
            }
            return true;
        } catch (FileSystemException e) {
            e.printStackTrace();
            return false;
        }
    }

    protected boolean addGetContentTypeProperty(final Element root,
                                                final boolean ignoreValue)
    {
        boolean ret = false;
        try {
          String contentType = object.getContent().getContentInfo().getContentType();
          if ((null != contentType) && !contentType.isEmpty()) {
              final Element el = root.addElement(PROP_GET_CONTENT_TYPE);
              if (!ignoreValue) {
                  el.addText(contentType);
              }
              ret = true;
          }
        } catch (FileSystemException e) {
            e.printStackTrace();
        }
        return ret;
    }

    protected boolean addGetETagProperty(final Element root,
                                         final boolean ignoreValue)
    {
        root.addElement(PROP_GET_ETAG, Util.getETag(object));
        return true;
    }

    protected boolean addGetLastModifiedProperty(final Element _root,
                                                 final boolean _ignoreValue)
    {
        try {
            Element el = _root.addElement(PROP_GET_LAST_MODIFIED);
            if (!_ignoreValue) {
                el.addText(Util.getDateString(object.getContent().getLastModifiedTime()));
            }
            return true;
        } catch (FileSystemException e) {
            e.printStackTrace();
            return false;
        }
    }

    protected boolean addLockDiscoveryProperty(final Element _root,
                                               final boolean _ignoreValue)
    {
        Element lockdiscoveryEl = _root.addElement(PROP_LOCK_DISCOVERY);
        try {
          List<Lock> locks = LockManager.getInstance().discoverLock(object);
          if (locks != null && !locks.isEmpty()) {
            for (Lock lock : locks) {
              if (lock != null && !_ignoreValue) {
                lock.serializeToXml(lockdiscoveryEl);
              }
            }
          }
          return true;
        } catch (FileSystemException e) {
          _root.remove(lockdiscoveryEl);
          e.printStackTrace();
          return false;
        }
    }

    protected boolean addResourceTypeProperty(final Element _root,
                                              final boolean _ignoreValue)
    {
        _root.addElement(PROP_RESOURCETYPE);
        return true;
    }

    protected boolean addSourceProperty(final Element _root,
                                        final boolean _ignoreValue)
    {
        return false;
    }

    protected boolean addSupportedLockProperty(final Element _root,
                                               final boolean _ignoreValue)
    {
        final Element supportedlockEl = _root.addElement(PROP_SUPPORTED_LOCK);
        if (!_ignoreValue) {
            Element exclLockentryEl = supportedlockEl.addElement("lockentry");
            exclLockentryEl.addElement("lockscope").addElement("exclusive");
            exclLockentryEl.addElement("locktype").addElement("write");
            Element sharedLockentryEl = supportedlockEl.addElement("lockentry");
            sharedLockentryEl.addElement("lockscope").addElement("shared");
            sharedLockentryEl.addElement("locktype").addElement("write");
        }
        return true;
    }

    protected boolean addQuotaProperty(final Element _root,
                                       final boolean _ignoreValue)
    {
        return false;
    }

    protected boolean addQuotaUsedProperty(final Element _root,
                                           final boolean _ignoreValue)
    {
        return false;
    }

    protected boolean addQuotaAvailableBytesProperty(final Element _root,
                                                     final boolean _ignoreValue)
    {
        return false;
    }

    protected boolean addQuotaUsedBytesProperty(final Element root,
                                                final boolean ignoreValue)
    {
        return false;
    }
}
