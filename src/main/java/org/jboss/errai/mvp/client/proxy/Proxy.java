/*
 * Errai-MVP, inspired by GWT-Platform for the Errai Framework
 * Copyright : Cedric Hauber (cedbossneo) 2012.
 */

package org.jboss.errai.mvp.client.proxy;

import com.google.gwt.event.shared.HasHandlers;
import com.google.web.bindery.event.shared.EventBus;
import org.jboss.errai.mvp.client.events.NotifyingAsyncCallback;
import org.jboss.errai.mvp.client.presenters.Presenter;

/**
 * The interface for light-weight singleton classes that listens for events
 * before the full {@link Presenter} is instantiated. This include, among
 * others, the presenter's specific {@link org.jboss.errai.mvp.client.events.RevealContentEvent} that needs the
 * presenter to reveal itself.
 * <p />
 * The relationship between a presenter and its proxy is two-way.
 * <p />
 * {@link Presenter} subclasses will usually define their own interface called
 * MyProxy and be derived from this one.
 *
 * @param <P> The type of the {@link Presenter} associated with this proxy.
 *
 * @author Philippe Beaudoin
 */
public interface Proxy<P extends Presenter<?>> extends HasHandlers {

  /**
   * Makes it possible to access the {@link com.google.web.bindery.event.shared.EventBus} object associated with
   * that proxy.
   *
   * @return The {@link com.google.web.bindery.event.shared.EventBus} associated with that proxy.
   */
  EventBus getEventBus();

  /**
   * Get the associated {@link Presenter}. The presenter can only be obtained in
   * an asynchronous manner to support code splitting when needed. To access the
   * presenter, pass a callback.
   *
   * @param callback The callback in which the {@link Presenter} will be passed
   *          as a parameter.
   */
  void getPresenter(NotifyingAsyncCallback<P> callback);
}