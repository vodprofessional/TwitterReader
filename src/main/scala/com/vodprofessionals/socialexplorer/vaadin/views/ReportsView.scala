package com.vodprofessionals.socialexplorer.vaadin.views

import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import com.vaadin.ui.{Label, VerticalLayout}

/**
 *
 */
class ReportsView extends VerticalLayout with View {
  val label = new Label("Reports here")
  addComponent(label)

  override def enter(event: ViewChangeEvent): Unit = {

  }
}
