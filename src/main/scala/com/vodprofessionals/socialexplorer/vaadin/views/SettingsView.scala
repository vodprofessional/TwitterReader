package com.vodprofessionals.socialexplorer.vaadin.views

import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import com.vaadin.ui.{Label, CssLayout}
import com.vaadin.navigator.View


/**
 *
 */
class SettingsView extends CssLayout with View {
  val label = new Label("Settings")
  addComponent(label)

  override def enter(event: ViewChangeEvent): Unit = {}
}
