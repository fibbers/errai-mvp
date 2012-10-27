/*
 * Errai-MVP, inspired by GWT-Platform for the Errai Framework
 * Copyright : Cedric Hauber (cedbossneo) 2012.
 */

package org.jboss.errai.mvp.client.events;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import org.jboss.errai.mvp.client.presenters.Presenter;

/**
 * This event is fired by a {@link Presenter} that desires to reveal itself
 * at the root of the application. It is typically fired in the {@link Presenter#revealInParent()}
 * method.
 * <p />
 * This type of content
 * is constrained to lie within the browser window, and to resize with it. You
 * will be responsible for adding your own scrollbars as content overflow,
 * usually via {@link com.google.gwt.user.client.ui.ScrollPanel}.
 *
 * @see RevealContentEvent
 * @see RevealRootContentEvent
 *
 * @author Philippe Beaudoin
 */
public final class RevealRootLayoutContentEvent extends
        GwtEvent<RevealRootLayoutContentHandler> {

  private static final Type<RevealRootLayoutContentHandler> TYPE = new Type<RevealRootLayoutContentHandler>();

  /**
   * Fires a {@link RevealRootLayoutContentEvent}
   * into a source that has access to an {@link com.google.web.bindery.event.shared.EventBus}.
   *
   * @param source The source that fires this event ({@link com.google.gwt.event.shared.HasHandlers}).
   * @param content The {@link Presenter} that wants to set itself as root content.
   */
  public static void fire(final HasHandlers source, final Presenter<?> content) {
    source.fireEvent(new RevealRootLayoutContentEvent(content));
  }

  public static Type<RevealRootLayoutContentHandler> getType() {
    return TYPE;
  }

  private final Presenter<?> content;

  public RevealRootLayoutContentEvent(Presenter<?> content) {
    this.content = content;
  }

  @Override
  public Type<RevealRootLayoutContentHandler> getAssociatedType() {
    return getType();
  }

  public Presenter<?> getContent() {
    return content;
  }

  @Override
  protected void dispatch(RevealRootLayoutContentHandler handler) {
    handler.onRevealRootLayoutContent(this);
  }

}
