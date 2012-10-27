/*
 * Errai-MVP, inspired by GWT-Platform for the Errai Framework
 * Copyright : Cedric Hauber (cedbossneo) 2012.
 */

package org.jboss.errai.mvp.client.events;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import org.jboss.errai.mvp.client.places.PlaceRequest;

/**
 *
 * This event is fired by the {@link PlaceManager} whenever a new place is
 * requested, either by history navigation or directly.
 * <p />
 * <b>Important!</b> You should never fire that event directly. Instead, build a
 * {@link PlaceRequest} and pass it to one of the following methods:
 * <ul>
 * <li>{@link PlaceManager#revealPlace(PlaceRequest)}</li>
 * <li>{@link PlaceManager#revealRelativePlace(PlaceRequest)}</li>
 * <li>{@link PlaceManager#revealRelativePlace(PlaceRequest, int)}</li>
 * </ul>
 *
 * @author David Peterson
 * @author Philippe Beaudoin
 *
 */
public class PlaceRequestInternalEvent extends GwtEvent<PlaceRequestInternalHandler> {

  private static Type<PlaceRequestInternalHandler> TYPE;

  /**
   * Fires a {@link org.jboss.errai.mvp.client.events.PlaceRequestInternalEvent}
   * into a source that has access to an {@com.google.web.bindery.event.shared.EventBus}.
   * <p />
   * <b>Important!</b> You should not fire that event directly, see
   * {@link org.jboss.errai.mvp.client.events.PlaceRequestInternalEvent} for more details.
   *
   * @param source The source that fires this event ({@link com.google.gwt.event.shared.HasHandlers}).
   * @param request The request.
   * @param updateBrowserHistory {@code true} If the browser URL should be updated, {@code false}
   *          otherwise.
   */
  public static void fire(HasHandlers source, PlaceRequest request, boolean updateBrowserHistory) {
    source.fireEvent(new PlaceRequestInternalEvent(request, updateBrowserHistory));
  }

  public static Type<PlaceRequestInternalHandler> getType() {
    if (TYPE == null) {
      TYPE = new Type<PlaceRequestInternalHandler>();
    }
    return TYPE;
  }

  private boolean authorized = true;

  /**
   * The handled flag can let others know when the event has been handled.
   * Handlers should call {@link setHandled()} as soon as they figure they are
   * be responsible for this event. Handlers should not process this event if
   * {@link isHandled()} return {@code true}.
   */
  private boolean handled;

  private final PlaceRequest request;
  private final boolean updateBrowserHistory;

  public PlaceRequestInternalEvent(PlaceRequest request, boolean updateBrowserHistory) {
    this.request = request;
    this.updateBrowserHistory = updateBrowserHistory;
  }

  @Override
  public Type<PlaceRequestInternalHandler> getAssociatedType() {
    return getType();
  }

  public PlaceRequest getRequest() {
    return request;
  }

  /**
   * Checks if the user was authorized to see the page.
   *
   * @return {@code true} if the user was authorized. {@code false} otherwise.
   */
  public boolean isAuthorized() {
    return authorized;
  }

  /**
   * Checks if the event was handled. If it was, then it should not be processed
   * further.
   *
   * @return {@code true} if the event was handled. {@code false} otherwise.
   */
  public boolean isHandled() {
    return handled;
  }

  public boolean shouldUpdateBrowserHistory() {
    return updateBrowserHistory;
  }

  /**
   * Indicates that the event was handled and that other contentHandlers should not
   * process it.
   */
  public void setHandled() {
    handled = true;
  }

  /**
   * Indicates that the event was handled but that the user was not authorized
   * to view the request page.
   */
  public void setUnauthorized() {
    authorized = false;
  }

  @Override
  protected void dispatch(PlaceRequestInternalHandler handler) {
    handler.onPlaceRequest(this);
  }
}
