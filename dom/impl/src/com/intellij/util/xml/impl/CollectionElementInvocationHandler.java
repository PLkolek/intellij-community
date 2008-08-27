/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.intellij.util.xml.impl;

import com.intellij.openapi.util.Factory;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlElement;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.events.ElementChangedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.List;

/**
 * @author peter
 */
public class CollectionElementInvocationHandler extends DomInvocationHandler<AbstractDomChildDescriptionImpl>{
  private final String myTagQName;

  public CollectionElementInvocationHandler(final Type type, @NotNull final XmlTag tag,
                                            final AbstractCollectionChildDescription description,
                                            final DomInvocationHandler parent) {
    super(type, new PhysicalDomParentStrategy(tag), description.createEvaluatedXmlName(parent, tag), (AbstractDomChildDescriptionImpl)description, parent.getManager(), true);
    myTagQName = tag.getName();
  }

  protected Type narrowType(@NotNull final Type nominalType) {
    return getManager().getTypeChooserManager().getTypeChooser(nominalType).chooseType(getXmlTag());
  }

  protected final XmlTag setEmptyXmlTag() {
    throw new UnsupportedOperationException("CollectionElementInvocationHandler.setXmlTag() shouldn't be called;" +
                                            "\nparent=" + getParent() + ";\n" +
                                            "xmlElementName=" + getXmlElementName());
  }

  @Override
  public boolean isValid() {
    if (!super.isValid()) return false;
    final XmlTag tag = getXmlTag();
    if (tag == null || !myTagQName.equals(tag.getName())) return false;
    return true;
  }

  public final void undefineInternal() {
    final DomElement parent = getParent();
    final XmlTag tag = getXmlTag();
    if (tag == null) return;

    getManager().cacheHandler(tag, null);
    deleteTag(tag);
    getManager().fireEvent(new ElementChangedEvent(parent));
  }

  public DomElement createPathStableCopy() {
    final AbstractDomChildDescriptionImpl description = getChildDescription();
    final DomElement parent = getParent();
    assert parent != null;
    final DomElement parentCopy = parent.createStableCopy();
    final int index = description.getValues(parent).indexOf(getProxy());
    return getManager().createStableValue(new Factory<DomElement>() {
      @Nullable
      public DomElement create() {
        if (parentCopy.isValid()) {
          final List<? extends DomElement> list = description.getValues(parentCopy);
          if (list.size() > index) {
            return list.get(index);
          }
        }
        return null;
      }
    });
  }

  @Override
  public int hashCode() {
    final XmlElement element = getXmlElement();
    return element == null ? super.hashCode() : element.hashCode();
  }
}
