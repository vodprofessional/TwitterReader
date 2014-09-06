package com.vodprofessionals.socialexplorer.vaadin.views

import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import com.vaadin.ui.{UI, Label, CssLayout}
import com.vaadin.navigator.View


/**
 *
 */
class SettingsView extends CssLayout with View {
  UI.getCurrent().getPage().setTitle("Social Explorer Settings")

  val label = new Label("Settings")
  addComponent(label)

  override def enter(event: ViewChangeEvent): Unit = {}
}
